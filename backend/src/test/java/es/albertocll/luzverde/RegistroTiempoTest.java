package es.albertocll.luzverde;

import es.albertocll.luzverde.auth.AuthDtos.RegistroRequest;
import es.albertocll.luzverde.auth.AuthService;
import es.albertocll.luzverde.auth.LimiteLoginService;
import es.albertocll.luzverde.seguridad.JwtService;
import es.albertocll.luzverde.usuario.Usuario;
import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El registro hace el mismo trabajo caro (el hash BCrypt) exista o no el email. Si no, un email
 * repetido respondería mucho antes y el tiempo delataría qué emails tienen cuenta.
 */
class RegistroTiempoTest {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private AuthService servicio;

    @BeforeEach
    void preparar() {
        when(encoder.encode(anyString())).thenReturn("hash");
        servicio = new AuthService(usuarios, encoder, mock(JwtService.class), mock(LimiteLoginService.class));
    }

    @Test
    void emailNuevoCalculaElHashYGuarda() {
        when(usuarios.existsByEmailIgnoreCase("nueva@ejemplo.com")).thenReturn(false);

        servicio.registrar(new RegistroRequest("nueva@ejemplo.com", "contrasena-segura", "Ana"));

        verify(encoder).encode("contrasena-segura");
        verify(usuarios).save(any(Usuario.class));
    }

    @Test
    void emailRepetidoTambienCalculaElHashPeroNoGuarda() {
        when(usuarios.existsByEmailIgnoreCase("ya@ejemplo.com")).thenReturn(true);

        servicio.registrar(new RegistroRequest("ya@ejemplo.com", "contrasena-segura", "Ana"));

        verify(encoder, times(1)).encode("contrasena-segura");
        verify(usuarios, never()).save(any(Usuario.class));
    }
}
