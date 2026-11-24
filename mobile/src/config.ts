import { almacen } from './almacen';
import { Platform } from 'react-native';

const CLAVE_URL = 'luzverde.urlServidor';

/**
 * URL del backend por defecto. Se puede fijar con EXPO_PUBLIC_API_URL al arrancar Expo.
 * En el emulador de Android, 10.0.2.2 es el localhost del ordenador.
 */
export const URL_POR_DEFECTO =
  process.env.EXPO_PUBLIC_API_URL ??
  (Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080');

let urlActual: string | null = null;

export async function cargarUrlServidor(): Promise<string> {
  if (urlActual) return urlActual;
  const guardada = await almacen.leer(CLAVE_URL);
  const url = guardada || URL_POR_DEFECTO;
  urlActual = url;
  return url;
}

export async function guardarUrlServidor(url: string): Promise<string> {
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
