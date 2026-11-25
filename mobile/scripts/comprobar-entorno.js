#!/usr/bin/env node
/**
 * Se ejecuta en EAS Build antes de instalar dependencias (script eas-build-pre-install).
 * Una build de producción sin URL https del backend se para aquí, en vez de llegar a Google Play.
 */
const entorno = process.env.EXPO_PUBLIC_ENTORNO ?? 'desarrollo';
const url = process.env.EXPO_PUBLIC_API_URL ?? '';

if (entorno !== 'desarrollo' && entorno !== 'produccion') {
  console.error(`EXPO_PUBLIC_ENTORNO debe ser "desarrollo" o "produccion", no "${entorno}".`);
  process.exit(1);
}

if (entorno === 'produccion' && !/^https:\/\/[^/]+/.test(url)) {
  console.error(
    'La build de producción necesita EXPO_PUBLIC_API_URL con https (la URL de Railway). ' +
      'Créala en expo.dev > Project > Environment variables, entorno production.',
  );
  process.exit(1);
}

console.log(`Entorno ${entorno}${url ? `, backend ${url}` : ''}.`);
