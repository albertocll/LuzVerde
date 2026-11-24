package es.albertocll.luzverde.auth;

import es.albertocll.luzverde.auth.AuthDtos.LoginRequest;
import es.albertocll.luzverde.auth.AuthDtos.RegistroRequest;
import es.albertocll.luzverde.auth.AuthDtos.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una cuenta y devuelve un token")
    public TokenResponse registro(@Valid @RequestBody RegistroRequest peticion) {
        return authService.registrar(peticion);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un token")
    public TokenResponse login(@Valid @RequestBody LoginRequest peticion, HttpServletRequest request) {
        return authService.login(peticion, request.getRemoteAddr());
    }
}
