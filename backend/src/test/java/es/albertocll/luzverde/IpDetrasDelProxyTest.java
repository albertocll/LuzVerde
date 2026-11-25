package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static es.albertocll.luzverde.RegistroHttp.registrarDesde;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Detrás del proxy de Railway (FORWARD_HEADERS_STRATEGY=native) los límites cuentan la IP de
 * X-Forwarded-For. El test llega desde 127.0.0.1, que Tomcat trata como proxy de confianza,
 * igual que la IP privada del proxy de Railway.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "server.forward-headers-strategy=native",
        "luzverde.registro.max-por-ip=2"})
class IpDetrasDelProxyTest extends TestBase {

    @LocalServerPort
    int puerto;

    @Test
    void cadaClienteTieneSuPropioLimite() throws Exception {
        assertThat(registrarDesde(puerto, "198.51.100.1")).isEqualTo(202);
        assertThat(registrarDesde(puerto, "198.51.100.1")).isEqualTo(202);
        assertThat(registrarDesde(puerto, "198.51.100.1")).isEqualTo(429);

        assertThat(registrarDesde(puerto, "198.51.100.2")).isEqualTo(202);
    }

    @Test
    void noSePuedeFalsearLaIpAnadiendoOtraDelante() throws Exception {
        // El proxy añade la IP real al final. Lo de delante lo ha escrito el cliente.
        assertThat(registrarDesde(puerto, "198.51.100.3")).isEqualTo(202);
        assertThat(registrarDesde(puerto, "198.51.100.3")).isEqualTo(202);
        assertThat(registrarDesde(puerto, "203.0.113.99, 198.51.100.3")).isEqualTo(429);
    }
}
