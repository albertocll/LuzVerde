package es.albertocll.luzverde.cv;

import java.time.Instant;
import java.util.List;

public final class CvDtos {

    private CvDtos() {
    }

    public record CvResponse(
            Long id,
            String empresa,
            String puesto,
            List<String> huecos,
            String carta,
            Instant creadoEn,
            String urlPdf,
            String urlDocx) {

        static CvResponse de(Cv cv) {
            return new CvResponse(cv.getId(), cv.getEmpresa(), cv.getPuesto(), cv.getHuecos(), cv.getCarta(),
                    cv.getCreadoEn(), "/api/v1/cv/" + cv.getId() + "/pdf", "/api/v1/cv/" + cv.getId() + "/docx");
        }
    }
}
