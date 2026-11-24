import { Ionicons } from '@expo/vector-icons';
import { useEffect, useState, type ReactNode } from 'react';
import { Linking, StyleSheet, Text, View } from 'react-native';
import { api } from '@/api';
import { Boton } from '@/componentes/ui';
import { color, espacio, texto } from '@/tema';
import { ENLACE_PLAY, hayQueActualizar, PAQUETE_ANDROID, versionInstalada } from '@/version';

/**
 * Pregunta al backend cuál es la versión mínima de la app. Si la instalada es más antigua,
 * muestra el aviso de actualizar en lugar de la app. Si no hay conexión, deja pasar:
 * cada pantalla ya avisa de sus propios errores de red.
 */
export function ComprobadorVersion({ children }: { children: ReactNode }) {
  const [minima, setMinima] = useState<string | null>(null);

  useEffect(() => {
    api
      .versionMinima()
      .then((r) => setMinima(r.versionMinima))
      .catch(() => setMinima(null));
  }, []);

  if (minima && hayQueActualizar(versionInstalada(), minima)) {
    return <AvisoActualizar minima={minima} />;
  }
  return <>{children}</>;
}

export function AvisoActualizar({ minima }: { minima: string }) {
  const abrirTienda = async () => {
    try {
      await Linking.openURL(`market://details?id=${PAQUETE_ANDROID}`);
    } catch {
      await Linking.openURL(ENLACE_PLAY);
    }
  };

  return (
    <View style={estilos.contenedor} accessibilityRole="alert">
      <Ionicons name="cloud-download-outline" size={56} color={color.primario} />
      <Text style={[texto.titulo, estilos.centro]}>Hay una versión nueva</Text>
      <Text style={[texto.suave, estilos.centro]}>
        Tu versión ({versionInstalada()}) ya no funciona con el servidor. Actualiza a la {minima} o posterior desde
        Google Play para seguir usando Luz Verde.
      </Text>
      <Boton titulo="Actualizar en Google Play" icono="logo-google-playstore" alPulsar={abrirTienda} />
    </View>
  );
}

const estilos = StyleSheet.create({
  contenedor: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: espacio.l,
    padding: espacio.xxl,
    backgroundColor: color.fondo,
  },
  centro: { textAlign: 'center' },
});
