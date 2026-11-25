package es.albertocll.luzverde;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static es.albertocll.luzverde.RegistroHttp.registrarDesde;
import static org.assertj.core.api.Assertions.assertThat;

/** Sin proxy, que es el valor por defecto, X-Forwarded-For se ignora y cuenta la IP de la conexión. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "server.forward-headers-strategy=none",
        "luzverde.registro.max-por-ip=2"})
class IpSinProxyTest extends TestBase {

    @LocalServerPort
    int puerto;

    @Test
    void cambiarLaCabeceraNoSaltaElLimite() throws Exception {
        assertThat(registrarDesde(puerto, "198.51.100.1")).isEqualTo(202);
        assertThat(registrarDesde(puerto, "198.51.100.2")).isEqualTo(202);
        assertThat(registrarDesde(puerto, "198.51.100.3")).isEqualTo(429);
    }
}
