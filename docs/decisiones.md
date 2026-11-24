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
