import { almacen } from './almacen';
import { Platform } from 'react-native';

const CLAVE_URL = 'luzverde.urlServidor';

export type Entorno = 'desarrollo' | 'produccion';

/**
 * Entorno de la build, fijado con EXPO_PUBLIC_ENTORNO al construir (los perfiles de eas.json lo ponen).
 * Babel sustituye la variable al construir, así que no se puede cambiar con la app instalada.
 */
export const ENTORNO: Entorno = process.env.EXPO_PUBLIC_ENTORNO === 'produccion' ? 'produccion' : 'desarrollo';

/** En producción la URL del backend es fija y la app no deja cambiarla. */
export const SERVIDOR_EDITABLE = ENTORNO === 'desarrollo';

/**
 * URL del backend por defecto. En desarrollo sale de EXPO_PUBLIC_API_URL o, si no está, del
 * localhost del ordenador (10.0.2.2 en el emulador de Android). En producción es siempre
 * EXPO_PUBLIC_API_URL, que scripts/comprobar-entorno.js obliga a que sea https.
 */
export function urlPorDefecto(entorno: Entorno, apiUrl: string | undefined, plataforma: string): string {
  if (apiUrl) return normalizarUrl(apiUrl);
  if (entorno === 'produccion') return '';
  return plataforma === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';
}

export const URL_POR_DEFECTO = urlPorDefecto(ENTORNO, process.env.EXPO_PUBLIC_API_URL, Platform.OS);

let urlActual: string | null = null;

export async function cargarUrlServidor(): Promise<string> {
  if (!SERVIDOR_EDITABLE) return URL_POR_DEFECTO;
  if (urlActual) return urlActual;
  const guardada = await almacen.leer(CLAVE_URL);
  const url = guardada || URL_POR_DEFECTO;
  urlActual = url;
  return url;
}

export async function guardarUrlServidor(url: string): Promise<string> {
  if (!SERVIDOR_EDITABLE) return URL_POR_DEFECTO;
  const limpia = normalizarUrl(url) || URL_POR_DEFECTO;
  await almacen.guardar(CLAVE_URL, limpia);
  urlActual = limpia;
  return limpia;
}

export function normalizarUrl(url: string): string {
  let limpia = url.trim().replace(/\/+$/, '');
  if (limpia && !/^https?:\/\//i.test(limpia)) {
    limpia = `http://${limpia}`;
  }
  return limpia;
}
