# seguridad

Qué datos maneja la app, qué riesgos tiene y cómo se cubren. Para informar de un fallo, mira [SECURITY.md](../SECURITY.md).

## datos

La app guarda nombre, email, contraseña (solo su hash BCrypt), perfil profesional, candidaturas, ofertas evaluadas y CV generados. Son datos personales. Los CV llevan teléfono, ubicación y enlaces si el usuario los rellena.

## riesgos y medidas

| Riesgo | Medida | Prueba automática |
|---|---|---|
| Ver o cambiar datos de otro usuario | Toda consulta filtra por el id del token. Lo ajeno responde 404 | `cadaUsuarioSoloVeLoSuyo`, `losCvSonPrivados`, `lasEvaluacionesSonPrivadas`, `elPerfilEsPrivado` |
| Token falsificado o manipulado | Firma HS256 verificada en cada petición. Se rechaza `alg: none` | `tokenConFirmaAlteradaEsRechazado`, `tokenSinFirmaAlgNoneEsRechazado`, `JwtServiceTest` |
| Fuerza bruta contra el login | 5 fallos por email o 20 por IP en 15 minutos devuelven 429 | `fuerzaBrutaContraUnaCuentaSeBloquea` |
| Saber qué emails están registrados por el tiempo de respuesta | Se compara la contraseña también cuando el email no existe | Revisión de código |
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

- Forzar HTTPS en producción. Railway lo sirve por HTTPS y la app debe apuntar a esa URL (fase 5).
- Confiar en la cabecera `X-Forwarded-For` solo del proxy de Railway, para que el límite por IP use la IP real.
- Borrado de cuenta y exportación de datos, que pide Google Play y el RGPD (fase 6).
- Revisar la política de privacidad ([borrador](privacidad.md)) antes de publicar.
