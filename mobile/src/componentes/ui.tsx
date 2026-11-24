import Ionicons from '@expo/vector-icons/Ionicons';
import type { ComponentProps, ReactNode } from 'react';
import {
  ActivityIndicator,
  KeyboardAvoidingView,
  Platform,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  type TextInputProps,
  type ViewStyle,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { color, espacio, radio, sombra, texto } from '../tema';

export type NombreIcono = ComponentProps<typeof Ionicons>['name'];

export function Pantalla({
  children,
  titulo,
  subtitulo,
  accion,
  alRefrescar,
  refrescando = false,
  conBordes = true,
}: {
  children: ReactNode;
  titulo?: string;
  subtitulo?: string;
  accion?: ReactNode;
  alRefrescar?: () => void;
  refrescando?: boolean;
  conBordes?: boolean;
}) {
  return (
    <SafeAreaView style={estilos.pantalla} edges={conBordes ? ['top'] : []}>
      <KeyboardAvoidingView style={{ flex: 1 }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
        <ScrollView
          contentContainerStyle={estilos.contenido}
          keyboardShouldPersistTaps="handled"
          refreshControl={
            alRefrescar ? (
              <RefreshControl refreshing={refrescando} onRefresh={alRefrescar} colors={[color.primario]} />
            ) : undefined
          }
        >
          {titulo ? (
            <View style={estilos.cabecera}>
              <View style={{ flex: 1 }}>
                <Text style={texto.titulo}>{titulo}</Text>
                {subtitulo ? <Text style={[texto.suave, { marginTop: 2 }]}>{subtitulo}</Text> : null}
              </View>
              {accion}
            </View>
          ) : null}
          {children}
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

export function Tarjeta({ children, estilo, alPulsar }: { children: ReactNode; estilo?: ViewStyle; alPulsar?: () => void }) {
  if (alPulsar) {
    return (
      <Pressable
        onPress={alPulsar}
        accessibilityRole="button"
        style={({ pressed }) => [estilos.tarjeta, estilo, pressed && { opacity: 0.85, transform: [{ scale: 0.99 }] }]}
      >
        {children}
      </Pressable>
    );
  }
  return <View style={[estilos.tarjeta, estilo]}>{children}</View>;
}

export function Seccion({ titulo, children, accion }: { titulo: string; children: ReactNode; accion?: ReactNode }) {
  return (
    <View style={{ marginTop: espacio.xl }}>
      <View style={estilos.seccionCabecera}>
        <Text style={texto.subtitulo}>{titulo}</Text>
        {accion}
      </View>
      {children}
    </View>
  );
}

type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'texto';

export function Boton({
  titulo,
  alPulsar,
  variante = 'primario',
  icono,
  cargando = false,
  desactivado = false,
  estilo,
}: {
  titulo: string;
  alPulsar: () => void;
  variante?: VarianteBoton;
  icono?: NombreIcono;
  cargando?: boolean;
  desactivado?: boolean;
  estilo?: ViewStyle;
}) {
  const v = VARIANTES[variante];
  const inactivo = desactivado || cargando;
  return (
    <Pressable
      onPress={alPulsar}
      disabled={inactivo}
      accessibilityRole="button"
      accessibilityLabel={titulo}
      accessibilityState={{ disabled: inactivo, busy: cargando }}
      style={({ pressed }) => [
        estilos.boton,
        { backgroundColor: v.fondo, borderColor: v.borde },
        variante === 'texto' && { paddingHorizontal: espacio.s },
        pressed && { opacity: 0.8 },
        inactivo && { opacity: 0.5 },
        estilo,
      ]}
    >
      {cargando ? (
        <ActivityIndicator color={v.texto} />
      ) : (
        <>
          {icono ? <Ionicons name={icono} size={18} color={v.texto} /> : null}
          <Text style={[estilos.botonTexto, { color: v.texto }]}>{titulo}</Text>
        </>
      )}
    </Pressable>
  );
}

const VARIANTES: Record<VarianteBoton, { fondo: string; texto: string; borde: string }> = {
  primario: { fondo: color.primario, texto: color.blanco, borde: color.primario },
  secundario: { fondo: color.superficie, texto: color.primarioOscuro, borde: color.borde },
  peligro: { fondo: color.superficie, texto: color.rojo, borde: color.rojoClaro },
  texto: { fondo: 'transparent', texto: color.primario, borde: 'transparent' },
};

export function BotonIcono({
  icono,
  alPulsar,
  etiqueta,
  colorIcono = color.primarioOscuro,
}: {
  icono: NombreIcono;
  alPulsar: () => void;
  etiqueta: string;
  colorIcono?: string;
}) {
  return (
    <Pressable
      onPress={alPulsar}
      accessibilityLabel={etiqueta}
      hitSlop={8}
      style={({ pressed }) => [estilos.botonIcono, pressed && { opacity: 0.6 }]}
    >
      <Ionicons name={icono} size={20} color={colorIcono} />
    </Pressable>
  );
}

export function Campo({
  etiqueta,
  error,
  ayuda,
  style,
  ...props
}: TextInputProps & { etiqueta: string; error?: string; ayuda?: string }) {
  return (
    <View style={{ marginBottom: espacio.m }}>
      <Text style={[texto.etiqueta, { marginBottom: 6 }]}>{etiqueta}</Text>
      <TextInput
        accessibilityLabel={etiqueta}
        accessibilityHint={error ?? ayuda}
        placeholderTextColor={color.textoTenue}
        style={[
          estilos.campo,
          props.multiline && { minHeight: 110, textAlignVertical: 'top', paddingTop: 12 },
          error ? { borderColor: color.rojo } : null,
          style,
        ]}
        {...props}
      />
      {error ? <Text style={estilos.error}>{error}</Text> : ayuda ? <Text style={estilos.ayuda}>{ayuda}</Text> : null}
    </View>
  );
}

export function Chip({
  texto: etiqueta,
  activo,
  alPulsar,
  colorActivo = color.primarioOscuro,
  fondoActivo = color.primarioOscuro,
}: {
  texto: string;
  activo: boolean;
  alPulsar: () => void;
  colorActivo?: string;
  fondoActivo?: string;
}) {
  return (
    <Pressable
      onPress={alPulsar}
      accessibilityRole="button"
      accessibilityState={{ selected: activo }}
      style={[
        estilos.chip,
        activo && { backgroundColor: fondoActivo, borderColor: fondoActivo === color.primarioOscuro ? fondoActivo : colorActivo },
      ]}
    >
      <Text
        style={[
          estilos.chipTexto,
          activo && { color: fondoActivo === color.primarioOscuro ? color.blanco : colorActivo },
        ]}
      >
        {etiqueta}
      </Text>
    </Pressable>
  );
}

export function Insignia({ texto: contenido, colorTexto, fondo }: { texto: string; colorTexto: string; fondo: string }) {
  return (
    <View style={[estilos.insignia, { backgroundColor: fondo }]}>
      <View style={[estilos.punto, { backgroundColor: colorTexto }]} />
      <Text style={[estilos.insigniaTexto, { color: colorTexto }]}>{contenido}</Text>
    </View>
  );
}

export function Vacio({ icono, titulo, texto: detalle }: { icono: NombreIcono; titulo: string; texto: string }) {
  return (
    <View style={estilos.vacio}>
      <View style={estilos.vacioIcono}>
        <Ionicons name={icono} size={30} color={color.primario} />
      </View>
      <Text style={[texto.subtitulo, { textAlign: 'center' }]}>{titulo}</Text>
      <Text style={[texto.suave, { textAlign: 'center', marginTop: 4 }]}>{detalle}</Text>
    </View>
  );
}

export function AvisoError({ mensaje }: { mensaje: string | null }) {
  if (!mensaje) return null;
  return (
    <View style={estilos.aviso} accessibilityRole="alert" accessibilityLiveRegion="polite">
      <Ionicons name="alert-circle" size={18} color={color.rojo} />
      <Text style={[texto.cuerpo, { color: color.rojo, flex: 1 }]}>{mensaje}</Text>
    </View>
  );
}

export function Cargando() {
  return (
    <View style={{ paddingVertical: espacio.xxl, alignItems: 'center' }}>
      <ActivityIndicator size="large" color={color.primario} />
    </View>
  );
}

export function ListaPuntos({ puntos, icono, colorIcono }: { puntos: string[]; icono: NombreIcono; colorIcono: string }) {
  return (
    <View style={{ gap: espacio.s }}>
      {puntos.map((p, i) => (
        <View key={i} style={{ flexDirection: 'row', gap: espacio.s, alignItems: 'flex-start' }}>
          <Ionicons name={icono} size={18} color={colorIcono} style={{ marginTop: 1 }} />
          <Text style={[texto.cuerpo, { flex: 1 }]}>{p}</Text>
        </View>
      ))}
    </View>
  );
}

const estilos = StyleSheet.create({
  pantalla: { flex: 1, backgroundColor: color.fondo },
  contenido: { padding: espacio.l, paddingBottom: 48 },
  cabecera: { flexDirection: 'row', alignItems: 'center', marginBottom: espacio.l, marginTop: espacio.s },
  tarjeta: {
    backgroundColor: color.superficie,
    borderRadius: radio.l,
    padding: espacio.l,
    borderWidth: 1,
    borderColor: color.borde,
    ...sombra,
  },
  seccionCabecera: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: espacio.m,
  },
  boton: {
    minHeight: 48,
    borderRadius: radio.m,
    borderWidth: 1,
    paddingHorizontal: espacio.l,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: espacio.s,
  },
  botonTexto: { fontSize: 15, fontWeight: '700' },
  botonIcono: {
    width: 40,
    height: 40,
    borderRadius: radio.total,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: color.superficie,
    borderWidth: 1,
    borderColor: color.borde,
  },
  campo: {
    backgroundColor: color.superficie,
    borderWidth: 1,
    borderColor: color.borde,
    borderRadius: radio.m,
    paddingHorizontal: espacio.m,
    paddingVertical: 11,
    fontSize: 15,
    color: color.texto,
  },
  error: { color: color.rojo, fontSize: 13, marginTop: 4 },
  ayuda: { color: color.textoTenue, fontSize: 12, marginTop: 4 },
  chip: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: radio.total,
    borderWidth: 1,
    borderColor: color.borde,
    backgroundColor: color.superficie,
  },
  chipTexto: { fontSize: 13, fontWeight: '600', color: color.textoSuave },
  insignia: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    alignSelf: 'flex-start',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: radio.total,
  },
  punto: { width: 7, height: 7, borderRadius: 4 },
  insigniaTexto: { fontSize: 12, fontWeight: '700' },
  vacio: { alignItems: 'center', paddingVertical: espacio.xxl, paddingHorizontal: espacio.xl },
  vacioIcono: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: color.primarioClaro,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: espacio.m,
  },
  aviso: {
    flexDirection: 'row',
    gap: espacio.s,
    alignItems: 'center',
    backgroundColor: color.rojoClaro,
    padding: espacio.m,
    borderRadius: radio.m,
    marginBottom: espacio.m,
  },
});
