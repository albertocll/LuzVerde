import { StyleSheet, Text, View } from 'react-native';
import { color, espacio, radio, SEMAFOROS } from '../tema';
import type { Semaforo as TipoSemaforo } from '../tipos';

const ORDEN: TipoSemaforo[] = ['ROJO', 'AMARILLO', 'VERDE'];

/** Semáforo horizontal con la luz que toca encendida. */
export function Semaforo({ valor, tamano = 'grande' }: { valor: TipoSemaforo; tamano?: 'grande' | 'mini' }) {
  const d = tamano === 'grande' ? 26 : 10;
  return (
    <View
      style={[estilos.caja, tamano === 'mini' && estilos.cajaMini]}
      accessible
      accessibilityLabel={`Semáforo ${SEMAFOROS[valor].nombre.toLowerCase()}`}
    >
      {ORDEN.map((luz) => {
        const encendida = luz === valor;
        const c = SEMAFOROS[luz].color;
        return (
          <View
            key={luz}
            style={{
              width: d,
              height: d,
              borderRadius: d / 2,
              backgroundColor: encendida ? c : '#2B3A34',
              shadowColor: c,
              shadowOpacity: encendida ? 0.9 : 0,
              shadowRadius: encendida ? d / 2 : 0,
              elevation: encendida && tamano === 'grande' ? 6 : 0,
            }}
          />
        );
      })}
    </View>
  );
}

export function CabeceraSemaforo({ valor }: { valor: TipoSemaforo }) {
  const s = SEMAFOROS[valor];
  return (
    <View style={[estilos.cabecera, { backgroundColor: s.fondo }]}>
      <Semaforo valor={valor} />
      <View style={{ flex: 1 }}>
        <Text style={[estilos.nombre, { color: s.color }]}>{s.nombre}</Text>
        <Text style={estilos.frase}>{s.frase}</Text>
      </View>
    </View>
  );
}

const estilos = StyleSheet.create({
  caja: {
    flexDirection: 'row',
    gap: 8,
    backgroundColor: '#0B1F18',
    paddingHorizontal: 10,
    paddingVertical: 8,
    borderRadius: radio.total,
    alignSelf: 'flex-start',
  },
  cajaMini: { gap: 4, paddingHorizontal: 6, paddingVertical: 5 },
  cabecera: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacio.l,
    padding: espacio.l,
    borderRadius: radio.l,
  },
  nombre: { fontSize: 22, fontWeight: '800' },
  frase: { fontSize: 14, color: color.textoSuave, marginTop: 2 },
});
