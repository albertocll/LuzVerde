# política de privacidad (borrador)

Borrador para la ficha de Google Play y la sección de seguridad de datos. No es un texto legal revisado. Hay que completarlo antes de publicar la app.

Última actualización: pendiente

## quién es el responsable

Alberto [apellidos], [email de contacto]. Luz Verde es un proyecto personal sin ánimo de lucro.

## qué datos se recogen

- Cuenta: nombre, email y contraseña. La contraseña se guarda cifrada con BCrypt y nadie puede leerla.
- Perfil profesional: titular, teléfono, LinkedIn, GitHub, ubicación, salario mínimo, modalidad, tecnologías, experiencia, formación, proyectos e idiomas. Todo lo escribe el usuario y todos los campos son opcionales.
- Candidaturas: empresa, puesto, enlace, estado, fecha y notas.
- Ofertas que el usuario pega para evaluarlas o para generar un CV, con el resultado de la evaluación y los CV generados.

La app no recoge ubicación del dispositivo, contactos, fotos ni identificadores publicitarios. No usa analítica ni publicidad.

## para qué se usan

Para guardar las candidaturas del usuario, evaluar ofertas contra su perfil y generar su CV. No se usan para nada más y no se venden.

## con quién se comparten

- Anthropic: el texto de la oferta y el perfil se envían a la API de Claude para hacer la evaluación o el CV. [Comprobar y enlazar la política de uso de datos de la API de Anthropic vigente.]
- Railway: aloja el servidor y la base de datos. [Indicar región.]

## cuánto tiempo se guardan

Mientras la cuenta exista. Desde Perfil, "Borrar cuenta y datos" borra al momento la cuenta, el perfil, las candidaturas, las evaluaciones, los CV y el registro de usos de IA (`DELETE /api/v1/cuenta`). No quedan copias en la base de datos.

## derechos

Desde Perfil, "Exportar mis datos" descarga al momento un JSON con la cuenta, el perfil, las candidaturas, las evaluaciones con el texto de cada oferta, los CV y los usos de IA (`GET /api/v1/cuenta/exportacion`). Eso cubre el acceso y la portabilidad. El usuario también puede pedir acceso, rectificación, supresión, portabilidad y oposición escribiendo a [email]. También puede reclamar ante la Agencia Española de Protección de Datos (aepd.es).

## seguridad

Las comunicaciones van cifradas por HTTPS. El token de sesión se guarda en el almacén seguro del móvil. Más detalle en [seguridad.md](seguridad.md).
