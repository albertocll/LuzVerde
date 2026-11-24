import * as SecureStore from 'expo-secure-store';

/**
 * Guarda pequeños valores (sesión, URL del servidor) en SecureStore.
 * En el modo demo de la vista previa web no hay SecureStore, así que se usa memoria.
 */
const memoria = new Map<string, string>();
const demo = process.env.EXPO_PUBLIC_DEMO === '1';

export const almacen = {
  leer: async (clave: string) => (demo ? (memoria.get(clave) ?? null) : SecureStore.getItemAsync(clave)),
  guardar: async (clave: string, valor: string) =>
    demo ? void memoria.set(clave, valor) : SecureStore.setItemAsync(clave, valor),
  borrar: async (clave: string) => (demo ? void memoria.delete(clave) : SecureStore.deleteItemAsync(clave)),
};
