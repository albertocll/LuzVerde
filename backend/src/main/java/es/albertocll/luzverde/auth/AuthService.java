package es.albertocll.luzverde.auth;

import es.albertocll.luzverde.auth.AuthDtos.LoginRequest;
import es.albertocll.luzverde.auth.AuthDtos.RegistroRequest;
import es.albertocll.luzverde.auth.AuthDtos.TokenResponse;
import es.albertocll.luzverde.error.CredencialesInvalidasException;
import es.albertocll.luzverde.seguridad.JwtService;
import es.albertocll.luzverde.usuario.Usuario;
import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {


    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LimiteLoginService limiteLogin;
    /** Hash de una contraseña aleatoria que nadie conoce, para igualar tiempos. */
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService,
                       LimiteLoginService limiteLogin) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limiteLogin = limiteLogin;
        this.hashFalso = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    /**
     * Crea la cuenta si el email está libre. Si ya existe no cambia nada y responde igual, para que
     * nadie pueda usar el registro para saber qué emails tienen cuenta. Tampoco devuelve token: la
     * app entra después con el login, que solo funciona con la contraseña del dueño.
     */
    @Transactional
    public void registrar(RegistroRequest peticion) {
        String email = normalizar(peticion.email());
        // El hash se calcula siempre, exista o no el email, para que el tiempo de respuesta no lo delate.
        String hash = passwordEncoder.encode(peticion.password());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            return;
        }
        usuarios.save(new Usuario(email, hash, peticion.nombre().trim()));
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest peticion, String ip) {
        String email = normalizar(peticion.email());
        limiteLogin.comprobar(email, ip);
        Usuario usuario = usuarios.findByEmailIgnoreCase(email).orElse(null);
        // Se compara la contraseña aunque el usuario no exista, para no revelar por tiempos qué emails hay.
        String hash = usuario != null ? usuario.getPasswordHash() : hashFalso;
        boolean correcta = passwordEncoder.matches(peticion.password(), hash);
        if (usuario == null || !correcta) {
            limiteLogin.registrarFallo(email, ip);
            throw new CredencialesInvalidasException();
        }
        limiteLogin.registrarExito(email);
        return token(usuario);
    }

    private TokenResponse token(Usuario usuario) {
        return new TokenResponse(
                jwtService.generarToken(usuario.getId(), usuario.getEmail()),
                "Bearer",
                jwtService.getExpiracionSegundos(),
                usuario.getId(),
                usuario.getNombre());
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
