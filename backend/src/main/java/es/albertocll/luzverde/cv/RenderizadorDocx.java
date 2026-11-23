package es.albertocll.luzverde.cv;

import es.albertocll.luzverde.cv.ContenidoCv.Apartado;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Mismo contenido que el PDF, en Word. Una columna, Arial, sin tablas ni imágenes.
 */
@Component
public class RenderizadorDocx {

    private static final String FUENTE = "Arial";

    public byte[] renderizar(ContenidoCv cv) {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            linea(doc, cv.nombre(), 18, true, false, 40);
            if (cv.titular() != null) {
                linea(doc, cv.titular(), 11, false, false, 40);
            }
            if (!cv.contacto().isEmpty()) {
                linea(doc, String.join(" | ", cv.contacto()), 9.5, false, false, 80);
            }

            if (cv.resumen() != null) {
                seccion(doc, "Perfil");
                linea(doc, cv.resumen(), 10, false, false, 0);
            }
            if (!cv.tecnologias().isEmpty()) {
                seccion(doc, "Tecnologías");
                linea(doc, String.join(", ", cv.tecnologias()), 10, false, false, 0);
            }
            apartados(doc, "Experiencia", cv.experiencias());
            apartados(doc, "Proyectos", cv.proyectos());
            apartados(doc, "Formación", cv.formacion());
            if (!cv.idiomas().isEmpty()) {
                seccion(doc, "Idiomas");
                cv.idiomas().forEach(i -> linea(doc, i, 10, false, false, 0));
            }

            doc.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se ha podido generar el DOCX", e);
        }
    }

    private static void apartados(XWPFDocument doc, String titulo, List<Apartado> apartados) {
        if (apartados.isEmpty()) {
            return;
        }
        seccion(doc, titulo);
        for (Apartado a : apartados) {
            String cabecera = a.fechas() == null ? a.titulo() : a.titulo() + " | " + a.fechas();
            XWPFParagraph p = linea(doc, cabecera, 10.5, true, false, 0);
            p.setSpacingBefore(80);
            if (a.subtitulo() != null) {
                linea(doc, a.subtitulo(), 9.5, false, true, 20);
            }
            for (String punto : a.puntos()) {
                XWPFParagraph vineta = linea(doc, "• " + punto, 10, false, false, 0);
                vineta.setIndentationLeft(200);
                vineta.setIndentationHanging(160);
            }
        }
    }

    private static void seccion(XWPFDocument doc, String titulo) {
        XWPFParagraph p = linea(doc, titulo, 12, true, false, 60);
        p.setSpacingBefore(200);
    }

    private static XWPFParagraph linea(XWPFDocument doc, String texto, double tamano, boolean negrita,
                                       boolean cursiva, int espacioDespues) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingAfter(espacioDespues);
        XWPFRun r = p.createRun();
        r.setText(texto);
        r.setFontFamily(FUENTE);
        r.setFontSize(tamano);
        r.setBold(negrita);
        r.setItalic(cursiva);
        return p;
    }
}
