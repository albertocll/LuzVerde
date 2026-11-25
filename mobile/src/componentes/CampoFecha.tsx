import Ionicons from '@expo/vector-icons/Ionicons';
import DateTimePicker, { DateTimePickerAndroid, type DateTimePickerEvent } from '@react-native-community/datetimepicker';
import { useState } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import { Campo } from '@/componentes/ui';
import { dateAIso, fechaCorta, isoADate } from '@/formato';
import { color, espacio, radio, texto } from '@/tema';

interface Props {
  etiqueta: string;
  /** Fecha en formato AAAA-MM-DD. */
  valor: string;
  alCambiar: (valor: string) => void;
  error?: string;
}

/**
 * Fecha con el selector nativo. En Android abre el calendario del sistema y en iOS lo muestra
 * debajo. En la web (modo demo) el selector no existe, así que queda el campo de texto.
 */
export function CampoFecha({ etiqueta, valor, alCambiar, error }: Props) {
  const [abiertoIos, setAbiertoIos] = useState(false);

  if (Platform.OS === 'web') {
    return (
      <Campo etiqueta={etiqueta} value={valor} onChangeText={alCambiar} placeholder="AAAA-MM-DD" error={error} />
    );
  }

  const alElegir = (evento: DateTimePickerEvent, fecha?: Date) => {
    if (Platform.OS === 'ios') setAbiertoIos(false);
    if (evento.type === 'set' && fecha) alCambiar(dateAIso(fecha));
  };

  const abrir = () => {
    if (Platform.OS === 'android') {
      DateTimePickerAndroid.open({ value: isoADate(valor), mode: 'date', onChange: alElegir });
    } else {
      setAbiertoIos((a) => !a);
    }
  };

  return (
    <View style={{ marginBottom: espacio.m }}>
      <Text style={[texto.etiqueta, { marginBottom: 6 }]}>{etiqueta}</Text>
      <Pressable
        onPress={abrir}
        accessibilityRole="button"
        accessibilityLabel={`${etiqueta}: ${fechaCorta(valor)}`}
        accessibilityHint="Abre el calendario para cambiar la fecha"
        style={[estilos.campo, error ? { borderColor: color.rojo } : null]}
      >
        <Text style={estilos.valor}>{fechaCorta(valor)}</Text>
        <Ionicons name="calendar-outline" size={18} color={color.textoSuave} />
      </Pressable>
      {abiertoIos ? <DateTimePicker value={isoADate(valor)} mode="date" display="inline" onChange={alElegir} /> : null}
      {error ? <Text style={estilos.error}>{error}</Text> : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  campo: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: color.superficie,
    borderWidth: 1,
    borderColor: color.borde,
    borderRadius: radio.m,
    paddingHorizontal: espacio.m,
    paddingVertical: 11,
  },
  valor: { fontSize: 15, color: color.texto },
  error: { color: color.rojo, fontSize: 13, marginTop: 4 },
});
