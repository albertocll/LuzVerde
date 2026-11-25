# decisiones

Registro de decisiones técnicas. Cada una dice qué se eligió, por qué y qué se descartó.

## 1. monorepo con backend y app

Fecha: 2026-11-23

Un solo repositorio con `backend/` y `mobile/`. Un cambio en la API y su uso en la app van en el mismo commit y la CI prueba las dos partes juntas. Se descartaron dos repositorios porque para una persona sola multiplican el trabajo de sincronizar versiones.

## 2. JWT sin estado

Fecha: 2026-11-23

El backend no guarda sesiones. El token lleva el id y el email del usuario, caduca a las 24 horas y se firma con HS256 con una clave de al menos 32 bytes que llega por `JWT_SECRET`. No hay refresh token: si caduca, la app vuelve al login. Es suficiente para una app personal y evita una tabla de sesiones.

## 3. salida estructurada de la IA forzando una herramienta

Fecha: 2026-11-23

Las llamadas a Claude declaran una herramienta con un esquema JSON y fuerzan su uso con `tool_choice`. Así la respuesta siempre llega como JSON con los campos esperados. Se descartó pedir JSON en el texto del prompt porque a veces llega con texto alrededor y hay que limpiarlo.

## 4. el CV no se fía del modelo

Fecha: 2026-11-23

La regla de no inventar experiencia no puede depender de que el modelo obedezca. `GuardianVeracidad` toma los datos duros del perfil, filtra las tecnologías contra el perfil y borra frases que nombran tecnologías que el usuario no tiene. El modelo solo decide el orden y la redacción de las viñetas.

## 5. límite de IA por usuario y día en base de datos

Fecha: 2026-11-23

Cada llamada a la IA deja un registro en `usos_ia` antes de hacerse, en su propia transacción. Así cuenta aunque la llamada falle, porque el gasto ya se ha producido. El límite (10 por defecto) es común a evaluaciones y CV.

## 6. modelo pequeño por defecto

Fecha: 2026-11-23

Se usa `claude-haiku-5-5`. Las tareas son de comparar y reformular textos cortos, y el presupuesto de partida es de 2 euros al mes. El modelo se cambia con `ANTHROPIC_MODELO` sin tocar código.

## 7. PDF con OpenPDF y Helvetica sin incrustar

Fecha: 2026-11-23

OpenPDF tiene licencia libre y genera texto real, no imágenes. Helvetica es una de las fuentes estándar de PDF, tiene las mismas métricas que Arial y no hace falta incrustarla, así que el texto se extrae sin problemas. El DOCX usa Arial con Apache POI.

## 8. límite de intentos de login en memoria

Fecha: 2026-11-24

Cinco fallos por email o veinte por IP en 15 minutos bloquean el login durante esa ventana. Se guarda en memoria porque el proyecto corre en una sola instancia. Si algún día hay varias, habrá que moverlo a la base de datos o a Redis.

## 9. Expo Go para desarrollo

Fecha: 2026-11-23

La app solo usa módulos incluidos en Expo Go (Expo Router, SecureStore, FileSystem, Sharing), así que se prueba sin compilar nada nativo. Para Google Play se generará el AAB con EAS Build.

## 10. modo demo web que no entra en producción

Fecha: 2026-11-24

Para enseñar la app sin backend ni móvil, `npm run web:demo` la abre en el navegador con una API falsa en memoria y datos inventados. El código vive en `mobile/src/demo/` y solo se carga con `EXPO_PUBLIC_DEMO=1`. Babel sustituye esa variable al construir y Metro elimina el `require` en la build normal, así que el modo demo no llega a la app de Google Play. Se comprobó buscando los datos demo en la exportación normal.

## 11. commits con Conventional Commits

Fecha: 2026-11-24

Los mensajes siguen [Conventional Commits](https://www.conventionalcommits.org/es/v1.0.0/) en español (`feat(mobile): ...`, `fix(seguridad): ...`, `docs: ...`). Así el historial se lee por área y el CHANGELOG se escribe casi solo.

## 12. versión de la API en la ruta

Fecha: 2026-11-24

Todas las rutas de la API cuelgan de `/api/v1`. Se cambió antes de publicar, cuando aún no había usuarios. Si un cambio rompe versiones antiguas de la app, irá en `/api/v2` y `/api/v1` seguirá viva hasta que la mayoría actualice. `/actuator/health` y Swagger quedan fuera del prefijo porque no son parte del contrato con la app.

## 13. esquema con migraciones de Flyway

Fecha: 2026-11-25

Hasta ahora Hibernate creaba las tablas con `ddl-auto: update`. Eso no borra columnas, no deja rastro de los cambios y puede hacer cosas raras con datos reales. Ahora el esquema vive en `backend/src/main/resources/db/migration` (`V1__esquema_inicial.sql`, `V2__...`) y Hibernate solo lo valida al arrancar (`ddl-auto: validate`). Los tests usan las mismas migraciones contra PostgreSQL 17.

Las bases que ya existían, como la de Docker Compose en local, se marcan como versión 1 sin ejecutar `V1` (`baseline-on-migrate`), porque ya tienen esas tablas. Se comprobó cargando el esquema que creaba Hibernate y arrancando: Flyway hace el baseline, aplica `V2` y la validación pasa.

Un cambio en una entidad ahora necesita su migración `V<n>__descripcion.sql`. Las migraciones ya publicadas no se tocan.

## 14. entornos de la app fijados al construir

Fecha: 2026-11-25

La app tiene dos entornos, `desarrollo` y `produccion`, elegidos con `EXPO_PUBLIC_ENTORNO` al construir. En desarrollo la URL del backend se cambia desde la app, que es lo cómodo con el emulador. En producción la URL viene de `EXPO_PUBLIC_API_URL`, la app no deja tocarla y la build falla si no es https. Así un usuario de Google Play no puede apuntar la app a otro servidor por error, y una build sin URL no llega a la tienda.
