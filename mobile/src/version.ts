import Constants from 'expo-constants';

/** Versión de la app instalada, la de app.json. */
export function versionInstalada(): string {
  return Constants.expoConfig?.version ?? '0.0.0';
}

/**
 * Compara dos versiones del tipo 1.2.3. Devuelve un número negativo si a es menor que b,
 * 0 si son iguales y positivo si a es mayor. Lo que no es un número cuenta como 0.
 */
export function compararVersiones(a: string, b: string): number {
  const partes = (v: string) => v.split('.').map((n) => Number.parseInt(n, 10) || 0);
  const [pa, pb] = [partes(a), partes(b)];
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const diferencia = (pa[i] ?? 0) - (pb[i] ?? 0);
    if (diferencia !== 0) return diferencia;
  }
  return 0;
}

export function hayQueActualizar(instalada: string, minima: string): boolean {
  return compararVersiones(instalada, minima) < 0;
}

export const PAQUETE_ANDROID = 'es.albertocll.luzverde';
export const ENLACE_PLAY = `https://play.google.com/store/apps/details?id=${PAQUETE_ANDROID}`;
