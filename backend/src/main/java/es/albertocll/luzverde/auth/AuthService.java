package es.albertocll.luzverde.auth;

import es.albertocll.luzverde.auth.AuthDtos.LoginRequest;
import es.albertocll.luzverde.auth.AuthDtos.RegistroRequest;
import es.albertocll.luzverde.auth.AuthDtos.TokenResponse;
import es.albertocll.luzverde.error.ConflictoException;
import es.albertocll.luzverde.error.CredencialesInvalidasException;
import es.albertocll.luzverde.seguridad.JwtService;
import es.albertocll.luzverde.usuario.Usuario;
import es.albertocll.luzverde.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse registrar(RegistroRequest peticion) {
        String email = normalizar(peticion.email());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ConflictoException("Ya existe una cuenta con ese email");
        }
        Usuario usuario = usuarios.save(new Usuario(
                email,
                passwordEncoder.encode(peticion.password()),
                peticion.nombre().trim()));
        return token(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest peticion) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(normalizar(peticion.email()))
                .orElseThrow(CredencialesInvalidasException::new);
        if (!passwordEncoder.matches(peticion.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }
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
