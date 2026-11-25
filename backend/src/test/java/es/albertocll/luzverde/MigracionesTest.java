package es.albertocll.luzverde;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El esquema lo crean las migraciones de Flyway y Hibernate lo valida al arrancar
 * (ddl-auto: validate). Si una entidad no coincide con las tablas, el contexto no arranca.
 */
class MigracionesTest extends TestBase {

    @Autowired
    Flyway flyway;

    @Test
    void todasLasMigracionesEstanAplicadas() {
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
    }
}
