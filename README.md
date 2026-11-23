# Luz Verde

Asesor de empleo para Android. Guarda tus candidaturas, tu perfil profesional y, en próximas fases, evalúa ofertas y genera CV adaptados a cada una.

Proyecto de portfolio de Alberto (github.com/albertocll).

## stack

- Backend: Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security con JWT, PostgreSQL.
- Documentación de la API: OpenAPI con Swagger UI (springdoc).
- Tests: JUnit 5 y MockMvc sobre H2 en memoria.
- App móvil (fase 4): React Native con Expo y TypeScript.

## estructura

```
luz-verde/
├── backend/            API REST en Spring Boot
├── mobile/             App React Native (pendiente)
├── docker-compose.yml  PostgreSQL y backend para desarrollo local
└── .env.example        Variables de entorno de ejemplo
```

## arrancar en local

Necesitas Docker y, si vas a ejecutar el backend fuera de Docker, Java 21 y Maven.

1. Copia `.env.example` como `.env` y rellena `DB_PASSWORD` y `JWT_SECRET`. Para la clave JWT vale `openssl rand -base64 48`.
2. Levanta todo con Docker:

   ```
   docker compose up --build
   ```

   O solo la base de datos, y el backend desde el IDE o la terminal:

   ```
   docker compose up db
   cd backend
   mvn spring-boot:run
   ```

   En este segundo caso el backend necesita las variables `DB_PASSWORD` y `JWT_SECRET` en el entorno (en IntelliJ, en la configuración de ejecución).
3. La API queda en `http://localhost:8080` y la documentación en `http://localhost:8080/swagger-ui.html`.

## tests

```
cd backend
mvn test
```

Los tests usan H2 en memoria, así que no necesitan Docker ni PostgreSQL.

## api

Todas las rutas salvo `/auth/**` y Swagger piden la cabecera `Authorization: Bearer <token>`.

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/auth/registro` | Crea una cuenta y devuelve un token |
| POST | `/auth/login` | Devuelve un token |
| GET | `/candidaturas?estado=ENVIADA` | Lista tus candidaturas, con filtro opcional por estado |
| GET | `/candidaturas/{id}` | Detalle de una candidatura |
| POST | `/candidaturas` | Crea una candidatura |
| PUT | `/candidaturas/{id}` | Actualiza una candidatura |
| DELETE | `/candidaturas/{id}` | Borra una candidatura |
| GET | `/perfil` | Tu perfil (vacío si no lo has rellenado) |
| POST | `/evaluaciones` | Evalúa una oferta con IA y devuelve semáforo, pros, huecos, banderas rojas y acción recomendada |
| GET | `/evaluaciones` | Tus últimas evaluaciones |
| GET | `/evaluaciones/{id}` | Detalle de una evaluación |
| POST | `/cv` | Genera un CV adaptado a una oferta (PDF y DOCX) y una carta de presentación |
| GET | `/cv` | Tus CV generados |
| GET | `/cv/{id}/pdf` | Descarga el CV en PDF |
| GET | `/cv/{id}/docx` | Descarga el CV en Word |
| PUT | `/perfil` | Crea o sustituye tu perfil |

Estados de una candidatura: `PENDIENTE`, `ENVIADA`, `ESPERANDO_RESPUESTA`, `ENTREVISTA`, `DESCARTADA`.

El perfil guarda el titular del CV, el teléfono, LinkedIn y GitHub, el stack con su nivel (`SOLIDO`, `MEDIO`, `SIN_TOCAR`), el salario mínimo en euros brutos al año, la modalidad (`PRESENCIAL`, `HIBRIDO`, `REMOTO`), la ubicación, la experiencia, la formación, los proyectos y los idiomas. Las fechas de experiencia y formación van en formato `AAAA-MM`.

## evaluador de ofertas

`POST /evaluaciones` recibe el texto de una oferta, lo compara con tu perfil usando la API de Claude y devuelve un semáforo (`VERDE`, `AMARILLO` o `ROJO`) con pros, huecos, banderas rojas y la acción recomendada.

- La clave de la API vive solo en la variable de entorno `ANTHROPIC_API_KEY` del servidor. La app nunca la ve.
- Sin clave, el resto de la API funciona y el evaluador responde 503.
- Límite de 10 usos de IA por usuario y día (hora de Madrid) y tope de tokens por llamada.
- Modelo por defecto: `claude-haiku-5-5`, el más pequeño de la familia actual. Se cambia con `ANTHROPIC_MODELO`.
- El modelo responde llamando a una herramienta con un esquema JSON fijo, así la salida siempre tiene la misma forma.
- Los tests simulan la API y no gastan créditos.

## generador de cv

`POST /cv` recibe el texto de una oferta y genera un CV adaptado en PDF (texto seleccionable) y en DOCX, más una carta de presentación de 150 palabras como máximo.

Formato pensado para ATS: una sola columna, sin tablas, imágenes, iconos ni barras de nivel, Helvetica en el PDF (métricas de Arial) y Arial en el DOCX, apartados con nombres normales (Perfil, Tecnologías, Experiencia, Proyectos, Formación, Idiomas) y fechas en formato `MM/AAAA`.

La regla dura es que el CV no inventa nada, y no depende solo del modelo:

- Puestos, empresas, fechas, títulos y centros salen siempre del perfil. El modelo solo elige el orden y reformula las viñetas.
- Las tecnologías del CV son las del perfil con nivel sólido o medio. Si el modelo propone otra, se descarta.
- Cualquier frase que nombre una tecnología que pide la oferta y el usuario no tiene se elimina del CV y de la carta, y esa tecnología pasa a la lista de huecos de la respuesta.
- Las experiencias y proyectos que el modelo no use se añaden igualmente con el texto del usuario.

Los PDF se generan con OpenPDF (LGPL/MPL) y los DOCX con Apache POI (Apache 2.0). Comparte el límite diario de usos de IA con el evaluador.

Cada usuario solo ve y modifica sus datos. Si pides una candidatura de otro usuario, la API responde 404, igual que si no existiera.

## variables de entorno

| Variable | Obligatoria | Uso |
|---|---|---|
| `DB_URL` | No | URL JDBC. Por defecto `jdbc:postgresql://localhost:5432/luzverde` |
| `DB_USER` | No | Usuario de PostgreSQL. Por defecto `luzverde` |
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL |
| `JWT_SECRET` | Sí | Clave en Base64 de al menos 32 bytes para firmar los tokens |
| `JWT_EXPIRACION_MINUTOS` | No | Duración del token. Por defecto 1440 (un día) |
| `PORT` | No | Puerto HTTP. Por defecto 8080 |
| `ANTHROPIC_API_KEY` | Para usar la IA | Clave de la API de Claude |
| `ANTHROPIC_MODELO` | No | Modelo. Por defecto `claude-haiku-5-5` |
| `ANTHROPIC_MAX_TOKENS` | No | Tope de tokens de salida por llamada. Por defecto 2048 |
| `LIMITE_IA_DIARIO` | No | Usos de IA por usuario y día. Por defecto 10 |

Ningún secreto vive en el repositorio.

## fases

- [x] Fase 1. Backend base: registro y login, candidaturas, perfil, tests y Swagger.
- [x] Fase 2. Evaluador de ofertas con la API de Claude.
- [x] Fase 3. Generador de CV compatible con ATS.
- [ ] Fase 4. App React Native.
- [ ] Fase 5. Despliegue en Railway.
- [ ] Fase 6. Publicación en Google Play.
