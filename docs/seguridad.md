# seguridad

Qué datos maneja la app, qué riesgos tiene y cómo se cubren. Para informar de un fallo, mira [SECURITY.md](../SECURITY.md).

## datos

La app guarda nombre, email, contraseña (solo su hash BCrypt), perfil profesional, candidaturas, ofertas evaluadas y CV generados. Son datos personales. Los CV llevan teléfono, ubicación y enlaces si el usuario los rellena.

## riesgos y medidas

| Riesgo | Medida | Prueba automática |
|---|---|---|
| Ver o cambiar datos de otro usuario | Toda consulta filtra por el id del token. Lo ajeno responde 404 | `cadaUsuarioSoloVeLoSuyo`, `losCvSonPrivados`, `lasEvaluacionesSonPrivadas`, `elPerfilEsPrivado` |
| Token falsificado o manipulado | Firma HS256, emisor (`luz-verde`) y destinatario (`luz-verde-app`) verificados en cada petición. Se rechaza `alg: none` | `tokenConFirmaAlteradaEsRechazado`, `tokenSinFirmaAlgNoneEsRechazado`, `rechazaTokenSinEmisorNiDestinatario` |
| Mapa de la API expuesto en producción | Swagger apagado salvo con `SWAGGER_ENABLED=true`. Docker Compose local lo activa | `swaggerEstaApagadoPorDefecto` |
| Fuerza bruta contra el login | 5 fallos por email o 20 por IP en 15 minutos devuelven 429 | `fuerzaBrutaContraUnaCuentaSeBloquea`, `LimiteLoginServiceTest` |
| Llenar la memoria del servidor probando miles de emails | Con más de 10.000 claves se borran las caducadas, como mucho una vez por minuto | `probarMilesDeEmailsNoLlenaLaMemoria` |
| IP falseada con `X-Forwarded-For` | Por defecto se ignora la cabecera. Detrás de Railway, `FORWARD_HEADERS_STRATEGY=native` solo la acepta de proxies internos | Revisión de código |
| Base de datos accesible desde la red local | Docker Compose publica PostgreSQL solo en `127.0.0.1` | Revisión de código |
| Saber qué emails están registrados por el tiempo del login | Se compara la contraseña también cuando el email no existe | Revisión de código |
| Saber qué emails están registrados usando el registro | El registro responde siempre `202` con el mismo mensaje y sin token. Si el email ya existe no cambia nada. El hash BCrypt se calcula en los dos casos para igualar el tiempo. La app entra después con el login | `registroConEmailRepetidoRespondeIgualYNoTocaLaCuenta`, `RegistroTiempoTest` |
| Asignación masiva de campos | Los DTO solo aceptan campos editables. El dueño sale del token | `asignacionMasivaNoPermiteElegirIdNiDueno` |
| Inyección SQL | Consultas de Spring Data con parámetros. Los enums se validan | `entradasMaliciosasNoRompenNadaNiFiltranTrazas` |
| Trazas o mensajes internos en las respuestas | Manejador de errores final con mensaje genérico. `include-stacktrace: never` | `entradasMaliciosasNoRompenNadaNiFiltranTrazas` |
| Contraseña o hash en las respuestas | Las respuestas de auth solo llevan token, id y nombre | `lasRespuestasNoExponenLaContrasenaNiSuHash` |
| Clave de la API de Claude expuesta | Solo en variable de entorno del servidor. La app nunca la recibe | Revisión de código y gitleaks en la CI |
| Gasto descontrolado en la IA | 10 usos por usuario y día, tope de tokens por llamada y límite mensual en la consola de Anthropic | `limiteDeDiezAlDia` |
| Instrucciones escondidas en una oferta (prompt injection) | El texto va entre etiquetas y el prompt dice que es un dato. La salida pasa por un esquema fijo y por `GuardianVeracidad` | Revisión de código |
| CV con datos inventados | `GuardianVeracidad` | `CvIntegrationTest`, `GuardianVeracidadTest` |
| Endpoints de administración expuestos | Actuator solo expone `health` sin detalles | `soloElHealthDeActuatorEsPublico` |
| Cabeceras inseguras | `nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, `Cache-Control: no-store` | `cabecerasDeSeguridad` |
| Token robado del móvil | Se guarda en `expo-secure-store` (Keystore de Android) | No automatizada |
| Dependencias vulnerables | Dependabot semanal y CodeQL con consultas `security-extended` | GitHub Actions |
| Secretos subidos al repo | `.env` ignorado y gitleaks en cada push | GitHub Actions |

## pendiente

- Verificar el email al registrarse. Hoy alguien puede registrar un email ajeno, intentar entrar con su contraseña y deducir por el fallo que el email ya tenía cuenta. El límite del login (5 fallos por email, 20 por IP en 15 minutos) frena esa vía, pero cerrarla del todo pide mandar un enlace por correo, y eso necesita un proveedor de email.
- Forzar HTTPS en producción. Railway lo sirve por HTTPS y la app debe apuntar a esa URL (fase 5).
- Poner `FORWARD_HEADERS_STRATEGY=native` en Railway, para que el límite por IP use la IP real (fase 5).
- Cambiar `ddl-auto: update` por migraciones con Flyway antes de tener datos reales.
- Borrado de cuenta y exportación de datos, que pide Google Play y el RGPD (fase 6).
- Revisar la política de privacidad ([borrador](privacidad.md)) antes de publicar.
