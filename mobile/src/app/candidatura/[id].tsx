import { router, Stack, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { Alert, Linking, Text, View } from 'react-native';
import { api, ErrorApi } from '@/api';
import { CampoFecha } from '@/componentes/CampoFecha';
import { AvisoError, Boton, Campo, Cargando, Chip, Pantalla } from '@/componentes/ui';
import { esFechaValida, hoyIso, oNull } from '@/formato';
import { color, espacio, ESTADOS, texto } from '@/tema';
import type { EstadoCandidatura } from '@/tipos';

const ESTADOS_ORDEN = Object.keys(ESTADOS) as EstadoCandidatura[];

/**
 * Crear (id = "nueva") o editar una candidatura.
 * Desde una evaluación llegan empresa, puesto y notas como parámetros para rellenar el formulario.
 */
export default function DetalleCandidatura() {
  const params = useLocalSearchParams<{ id: string; empresa?: string; puesto?: string; notas?: string }>();
  const esNueva = params.id === 'nueva';
  const id = esNueva ? null : Number(params.id);

  const [cargando, setCargando] = useState(!esNueva);
  const [empresa, setEmpresa] = useState(params.empresa ?? '');
  const [puesto, setPuesto] = useState(params.puesto ?? '');
  const [enlace, setEnlace] = useState('');
  const [estado, setEstado] = useState<EstadoCandidatura>('PENDIENTE');
  const [fecha, setFecha] = useState(hoyIso());
  const [notas, setNotas] = useState(params.notas ?? '');
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [campos, setCampos] = useState<Record<string, string>>({});

  useEffect(() => {
    if (id === null) return;
    api
      .candidatura(id)
      .then((c) => {
        setEmpresa(c.empresa);
        setPuesto(c.puesto);
        setEnlace(c.enlace ?? '');
        setEstado(c.estado);
        setFecha(c.fecha);
        setNotas(c.notas ?? '');
      })
      .catch((e) => setError(e instanceof ErrorApi ? e.message : 'No se ha podido cargar.'))
      .finally(() => setCargando(false));
  }, [id]);

  const guardar = async () => {
    if (!esFechaValida(fecha)) {
      setCampos({ fecha: 'Usa el formato AAAA-MM-DD' });
      return;
    }
    setGuardando(true);
    setError(null);
    setCampos({});
    const datos = { empresa, puesto, enlace: oNull(enlace), estado, fecha, notas: oNull(notas) };
    try {
      if (id === null) {
        await api.crearCandidatura(datos);
      } else {
        await api.actualizarCandidatura(id, datos);
      }
      router.back();
    } catch (e) {
      if (e instanceof ErrorApi) {
        setError(e.message);
        setCampos(e.campos);
      } else {
        setError('No se ha podido guardar.');
      }
    } finally {
      setGuardando(false);
    }
  };

  const borrar = () => {
    if (id === null) return;
    Alert.alert('Borrar candidatura', `¿Seguro que quieres borrar ${puesto} en ${empresa}?`, [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Borrar',
        style: 'destructive',
        onPress: async () => {
          try {
            await api.borrarCandidatura(id);
            router.back();
          } catch (e) {
            setError(e instanceof ErrorApi ? e.message : 'No se ha podido borrar.');
          }
        },
      },
    ]);
  };

  return (
    <Pantalla conBordes={false}>
      <Stack.Screen options={{ title: esNueva ? 'Nueva candidatura' : 'Editar candidatura' }} />
      {cargando ? (
        <Cargando />
      ) : (
        <>
          <AvisoError mensaje={error} />
          <Campo etiqueta="Puesto" value={puesto} onChangeText={setPuesto} placeholder="Desarrollador Java junior" error={campos.puesto} />
          <Campo etiqueta="Empresa" value={empresa} onChangeText={setEmpresa} placeholder="Nombre de la empresa" error={campos.empresa} />
          <Campo
            etiqueta="Enlace a la oferta"
            value={enlace}
            onChangeText={setEnlace}
            placeholder="https://..."
            autoCapitalize="none"
            keyboardType="url"
            error={campos.enlace}
          />

          <Text style={[texto.etiqueta, { marginBottom: espacio.s }]}>Estado</Text>
          <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: espacio.s, marginBottom: espacio.l }}>
            {ESTADOS_ORDEN.map((e) => (
              <Chip
                key={e}
                texto={ESTADOS[e].nombre}
                activo={estado === e}
                alPulsar={() => setEstado(e)}
                colorActivo={ESTADOS[e].color}
                fondoActivo={ESTADOS[e].fondo}
              />
            ))}
          </View>

          <CampoFecha etiqueta="Fecha" valor={fecha} alCambiar={setFecha} error={campos.fecha} />
          <Campo etiqueta="Notas" value={notas} onChangeText={setNotas} multiline placeholder="Contactos, próximos pasos..." />

          <View style={{ gap: espacio.s, marginTop: espacio.s }}>
            <Boton
              titulo="Guardar"
              icono="checkmark"
              alPulsar={guardar}
              cargando={guardando}
              desactivado={!empresa.trim() || !puesto.trim()}
            />
            {enlace.trim() ? (
              <Boton titulo="Abrir oferta" icono="open-outline" variante="secundario" alPulsar={() => Linking.openURL(enlace.trim())} />
            ) : null}
            {!esNueva ? <Boton titulo="Borrar" icono="trash-outline" variante="peligro" alPulsar={borrar} /> : null}
          </View>
          <Text style={{ color: color.textoTenue, fontSize: 12, marginTop: espacio.l, textAlign: 'center' }}>
            Solo tú ves tus candidaturas.
          </Text>
        </>
      )}
    </Pantalla>
  );
}
