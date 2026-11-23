package es.albertocll.luzverde.seguridad;

/**
 * Lo que guardamos en el contexto de seguridad tras validar el token.
 */
public record UsuarioAutenticado(Long id, String email) {
}
