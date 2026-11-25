# cambios

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/). Versiones según [SemVer](https://semver.org/lang/es/).

## [sin publicar]

### añadido

- Vista previa web de la app y modo demo con datos inventados (`npm run web:demo`), fuera de la build de producción.
- README con mockup, capturas y diagrama de arquitectura.
- GIF de la demo, CV en PDF de ejemplo, Swagger UI e informe de cobertura en el README.
- Borrar la cuenta y todos sus datos desde Perfil (`DELETE /api/v1/cuenta`). El token de una cuenta borrada deja de valer.
- Aviso de "Hay una versión nueva" cuando la app es más antigua que `APP_VERSION_MINIMA` (`GET /api/v1/version`).
- Páginas web de privacidad, términos de uso y borrado de cuenta, enlazadas desde la landing.
- Límite de registros: 10 peticiones por IP y hora.
- Tests con Tomcat real de la IP del cliente detrás del proxy (`X-Forwarded-For`).

- Límite de intentos de login: 5 fallos por email o 20 por IP en 15 minutos.
- Endpoint `/actuator/health` para el despliegue.
- Cabeceras de seguridad y manejador de errores que nunca devuelve trazas.
- Tests de seguridad: tokens manipulados, fuerza bruta, asignación masiva, entradas maliciosas y cabeceras.
- Cobertura con JaCoCo (mínimo 70 % de líneas).
- Tests de la app con Jest y Testing Library.
- Aviso "Completa tu perfil" y barra de progreso en Perfil.
- Mejoras de accesibilidad: roles, estados y etiquetas en botones, chips, campos y semáforo.
- CI en GitHub Actions, CodeQL, búsqueda de secretos con gitleaks y Dependabot.
- Documentación: arquitectura, decisiones, seguridad, interfaz y borrador de política de privacidad.

### cambiado

- El esquema de la base de datos sale de migraciones de Flyway. Hibernate ya no crea tablas, solo las valida. Las bases existentes se adoptan como versión 1 sin perder datos.
- Los tests del backend corren contra PostgreSQL 17 real con Testcontainers en lugar de H2. Necesitan Docker.
- La API pasa a `/api/v1`. La app ya usa las rutas nuevas.
- El registro responde `202` con el mismo mensaje exista o no el email, y ya no devuelve token. La app entra justo después con el login.
- Cada respuesta de error lleva un `code` fijo (`INVALID_CREDENTIALS`, `AI_DAILY_LIMIT`...). La app elige su mensaje por él, así que un 429 del login ya no dice que se acabó la IA.

### seguridad

- El registro ya no revela si un email tiene cuenta. Desaparece el `409 EMAIL_TAKEN` y el tiempo de respuesta es parecido en los dos casos.
- Swagger queda apagado salvo con `SWAGGER_ENABLED=true`. Docker Compose local lo activa.
- El token JWT lleva emisor y destinatario, y el backend los comprueba.
- El límite de login borra las claves caducadas para que probar miles de emails no llene la memoria.
- El login rechaza emails y contraseñas más largos de lo que admite el registro.
- `X-Forwarded-For` se ignora salvo que se active `FORWARD_HEADERS_STRATEGY=native` detrás de un proxy.
- Docker Compose publica PostgreSQL solo en `127.0.0.1`.

## [0.4.0] - 2026-11-24

### añadido

- App Android con Expo: candidaturas, evaluación de ofertas, CV adaptado y perfil.

## [0.3.0] - 2026-11-23

### añadido

- Generador de CV compatible con ATS en PDF y DOCX, con carta de presentación y lista de huecos.
- Titular, teléfono, LinkedIn y GitHub en el perfil.

## [0.2.0] - 2026-11-23

### añadido

- Evaluador de ofertas con la API de Claude: semáforo, pros, huecos, banderas rojas y acción recomendada.
- Límite de 10 usos de IA por usuario y día.

## [0.1.0] - 2026-11-23

### añadido

- API con registro y login por JWT, CRUD de candidaturas y perfil profesional.
- Docker Compose con PostgreSQL y documentación con Swagger.
