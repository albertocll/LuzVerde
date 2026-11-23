package es.albertocll.luzverde.cv;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import es.albertocll.luzverde.cv.ContenidoCv.Apartado;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * PDF apto para ATS: una columna, solo texto, sin tablas, imágenes ni gráficos.
 * Usa Helvetica, la fuente estándar de PDF con las mismas métricas que Arial, y no se incrusta:
 * el texto queda seleccionable y cualquier lector lo extrae.
 */
@Component
public class RenderizadorPdf {

    private static final Font NOMBRE = fuente(18, Font.BOLD);
    private static final Font TITULAR = fuente(11, Font.NORMAL);
    private static final Font CONTACTO = fuente(9.5f, Font.NORMAL);
    private static final Font SECCION = fuente(12, Font.BOLD);
    private static final Font TITULO = fuente(10.5f, Font.BOLD);
    private static final Font NORMAL = fuente(10, Font.NORMAL);
    private static final Font DISCRETO = fuente(9.5f, Font.ITALIC);

    public byte[] renderizar(ContenidoCv cv) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 54, 54, 48, 48);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.addTitle("CV " + cv.nombre());
            documento.addAuthor(cv.nombre());
            documento.open();

            documento.add(parrafo(cv.nombre(), NOMBRE, 0, 2));
            if (cv.titular() != null) {
                documento.add(parrafo(cv.titular(), TITULAR, 0, 2));
            }
            if (!cv.contacto().isEmpty()) {
                documento.add(parrafo(String.join(" | ", cv.contacto()), CONTACTO, 0, 4));
            }

            if (cv.resumen() != null) {
                seccion(documento, "Perfil");
                documento.add(parrafo(cv.resumen(), NORMAL, 0, 0));
            }
            if (!cv.tecnologias().isEmpty()) {
                seccion(documento, "Tecnologías");
                documento.add(parrafo(String.join(", ", cv.tecnologias()), NORMAL, 0, 0));
            }
            apartados(documento, "Experiencia", cv.experiencias());
            apartados(documento, "Proyectos", cv.proyectos());
            apartados(documento, "Formación", cv.formacion());
            if (!cv.idiomas().isEmpty()) {
                seccion(documento, "Idiomas");
                for (String idioma : cv.idiomas()) {
                    documento.add(parrafo(idioma, NORMAL, 0, 0));
                }
            }
        } catch (DocumentException e) {
            throw new IllegalStateException("No se ha podido generar el PDF", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
        return salida.toByteArray();
    }

    private static void apartados(Document documento, String titulo, List<Apartado> apartados)
            throws DocumentException {
        if (apartados.isEmpty()) {
            return;
        }
        seccion(documento, titulo);
        for (Apartado a : apartados) {
            String cabecera = a.fechas() == null ? a.titulo() : a.titulo() + " | " + a.fechas();
            documento.add(parrafo(cabecera, TITULO, 4, 0));
            if (a.subtitulo() != null) {
                documento.add(parrafo(a.subtitulo(), DISCRETO, 0, 1));
            }
            for (String punto : a.puntos()) {
                Paragraph vineta = parrafo("• " + punto, NORMAL, 0, 0);
                vineta.setIndentationLeft(10);
                vineta.setFirstLineIndent(-8);
                documento.add(vineta);
            }
        }
    }

    private static void seccion(Document documento, String titulo) throws DocumentException {
        documento.add(parrafo(titulo, SECCION, 10, 3));
    }

    private static Paragraph parrafo(String texto, Font fuente, float antes, float despues) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(Element.ALIGN_LEFT);
        p.setSpacingBefore(antes);
        p.setSpacingAfter(despues);
        p.setLeading(fuente.getSize() * 1.3f);
        return p;
    }

    private static Font fuente(float tamano, int estilo) {
        return FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED, tamano, estilo);
    }
}
