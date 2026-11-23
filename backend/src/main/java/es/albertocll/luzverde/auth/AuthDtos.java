package es.albertocll.luzverde.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegistroRequest(
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El email no es válido")
            @Size(max = 254)
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            String password,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100)
            String nombre) {
    }

    public record LoginRequest(
            @NotBlank(message = "El email es obligatorio")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            String password) {
    }

    public record TokenResponse(String token, String tipo, long expiraEnSegundos, Long usuarioId, String nombre) {
    }
}
