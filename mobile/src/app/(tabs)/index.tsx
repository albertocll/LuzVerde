import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { api, ErrorApi } from '@/api';
import { useAuth } from '@/auth';
import { AvisoPerfil } from '@/componentes/AvisoPerfil';
import { AvisoError, Boton, Cargando, Chip, Insignia, Pantalla, Tarjeta, Vacio } from '@/componentes/ui';
import { fechaCorta } from '@/formato';
import { color, espacio, ESTADOS, texto } from '@/tema';
import type { Candidatura, EstadoCandidatura } from '@/tipos';

const FILTROS: (EstadoCandidatura | null)[] = [
  null,
  'PENDIENTE',
  'ENVIADA',
  'ESPERANDO_RESPUESTA',
  'ENTREVISTA',
  'DESCARTADA',
];

export default function Candidaturas() {
  const { sesion } = useAuth();
  const [filtro, setFiltro] = useState<EstadoCandidatura | null>(null);
  const [lista, setLista] = useState<Candidatura[] | null>(null);
  const [todas, setTodas] = useState<Candidatura[]>([]);
  const [refrescando, setRefrescando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    try {
      setError(null);
      const [filtradas, completas] = await Promise.all([
        api.candidaturas(filtro ?? undefined),
        filtro ? api.candidaturas() : Promise.resolve(null),
      ]);
      setLista(filtradas);
      setTodas(completas ?? filtradas);
    } catch (e) {
      setError(e instanceof ErrorApi ? e.message : 'No se han podido cargar las candidaturas.');
      setLista((l) => l ?? []);
    }
  }, [filtro]);

  useFocusEffect(
    useCallback(() => {
      void cargar();
    }, [cargar]),
  );

  const refrescar = async () => {
    setRefrescando(true);
    await cargar();
    setRefrescando(false);
  };

  const cuenta = (estado: EstadoCandidatura) => todas.filter((c) => c.estado === estado).length;
  const activas = todas.filter((c) => c.estado !== 'DESCARTADA').length;
  const primerNombre = sesion?.nombre.split(' ')[0] ?? '';

  return (
    <Pantalla
      titulo="Candidaturas"
      subtitulo={primerNombre ? `Hola, ${primerNombre}` : undefined}
      alRefrescar={refrescar}
      refrescando={refrescando}
      accion={<Boton titulo="Nueva" icono="add" alPulsar={() => router.push('/candidatura/nueva')} />}
    >
      <AvisoPerfil />

      <View style={estilos.resumen}>
        <Dato numero={activas} etiqueta="Activas" />
        <Dato numero={cuenta('ENTREVISTA')} etiqueta="Entrevistas" destacado />
        <Dato numero={cuenta('ESPERANDO_RESPUESTA')} etiqueta="Esperando" />
      </View>

      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={estilos.filtros}>
        {FILTROS.map((f) => (
          <Chip
            key={f ?? 'todas'}
            texto={f ? ESTADOS[f].nombre : 'Todas'}
            activo={filtro === f}
            alPulsar={() => setFiltro(f)}
          />
        ))}
      </ScrollView>

      <AvisoError mensaje={error} />

      {lista === null ? (
        <Cargando />
      ) : lista.length === 0 ? (
        <Vacio
          icono="briefcase-outline"
          titulo={filtro ? 'Nada en este estado' : 'Aún no tienes candidaturas'}
          texto={filtro ? 'Prueba con otro filtro.' : 'Añade la primera con el botón Nueva o desde una oferta evaluada.'}
        />
      ) : (
        <View style={{ gap: espacio.m }}>
          {lista.map((c) => (
            <Tarjeta key={c.id} alPulsar={() => router.push(`/candidatura/${c.id}`)}>
              <View style={estilos.fila}>
                <View style={{ flex: 1 }}>
                  <Text style={texto.subtitulo} numberOfLines={1}>
                    {c.puesto}
                  </Text>
                  <Text style={[texto.suave, { marginTop: 2 }]} numberOfLines={1}>
                    {c.empresa}
                  </Text>
                </View>
                <Ionicons name="chevron-forward" size={20} color={color.textoTenue} />
              </View>
              <View style={[estilos.fila, { marginTop: espacio.m }]}>
                <Insignia
                  texto={ESTADOS[c.estado].nombre}
                  colorTexto={ESTADOS[c.estado].color}
                  fondo={ESTADOS[c.estado].fondo}
                />
                <Text style={estilos.fecha}>{fechaCorta(c.fecha)}</Text>
              </View>
            </Tarjeta>
          ))}
        </View>
      )}
    </Pantalla>
  );
}

function Dato({ numero, etiqueta, destacado }: { numero: number; etiqueta: string; destacado?: boolean }) {
  return (
    <View style={[estilos.dato, destacado && { backgroundColor: color.primarioOscuro }]}>
      <Text style={[estilos.datoNumero, destacado && { color: color.blanco }]}>{numero}</Text>
      <Text style={[estilos.datoEtiqueta, destacado && { color: '#A7F3D0' }]}>{etiqueta}</Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  resumen: { flexDirection: 'row', gap: espacio.s, marginBottom: espacio.l },
  dato: {
    flex: 1,
    backgroundColor: color.superficie,
    borderRadius: 16,
    paddingVertical: espacio.m,
    paddingHorizontal: espacio.m,
    borderWidth: 1,
    borderColor: color.borde,
  },
  datoNumero: { fontSize: 24, fontWeight: '800', color: color.texto },
  datoEtiqueta: { fontSize: 12, fontWeight: '600', color: color.textoSuave, marginTop: 2 },
  filtros: { gap: espacio.s, paddingBottom: espacio.l },
  fila: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: espacio.m },
  fecha: { fontSize: 13, color: color.textoTenue },
});
