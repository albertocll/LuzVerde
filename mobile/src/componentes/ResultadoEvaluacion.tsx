import { router } from 'expo-router';
import { Text, View } from 'react-native';
import { color, espacio, texto } from '../tema';
import type { Evaluacion } from '../tipos';
import { CabeceraSemaforo } from './Semaforo';
import { Boton, ListaPuntos, Tarjeta } from './ui';

export function ResultadoEvaluacion({
  evaluacion,
  alGenerarCv,
  generandoCv = false,
}: {
  evaluacion: Evaluacion;
  alGenerarCv?: () => void;
  generandoCv?: boolean;
}) {
  const e = evaluacion;
  const titulo = [e.puesto, e.empresa].filter(Boolean).join(' · ');

  const guardarComoCandidatura = () =>
    router.push({
      pathname: '/candidatura/[id]',
      params: {
        id: 'nueva',
        empresa: e.empresa ?? '',
        puesto: e.puesto ?? '',
        notas: [e.resumen, e.accionRecomendada].filter(Boolean).join('\n\n'),
      },
    });

  return (
    <View style={{ gap: espacio.m }}>
      {titulo ? <Text style={texto.subtitulo}>{titulo}</Text> : null}
      <CabeceraSemaforo valor={e.semaforo} />
      {e.resumen ? <Text style={texto.cuerpo}>{e.resumen}</Text> : null}

      {e.accionRecomendada ? (
        <Tarjeta estilo={{ backgroundColor: color.primarioOscuro, borderColor: color.primarioOscuro }}>
          <Text style={{ color: '#A7F3D0', fontWeight: '700', fontSize: 13, marginBottom: 4 }}>Qué hacer ahora</Text>
          <Text style={{ color: color.blanco, fontSize: 15, lineHeight: 21 }}>{e.accionRecomendada}</Text>
        </Tarjeta>
      ) : null}

      {e.pros.length > 0 ? (
        <Tarjeta>
          <Text style={[texto.etiqueta, { marginBottom: espacio.s }]}>A tu favor</Text>
          <ListaPuntos puntos={e.pros} icono="checkmark-circle" colorIcono={color.primario} />
        </Tarjeta>
      ) : null}

      {e.huecos.length > 0 ? (
        <Tarjeta>
          <Text style={[texto.etiqueta, { marginBottom: espacio.s }]}>Te falta</Text>
          <ListaPuntos puntos={e.huecos} icono="remove-circle" colorIcono={color.ambar} />
        </Tarjeta>
      ) : null}

      {e.banderasRojas.length > 0 ? (
        <Tarjeta estilo={{ borderColor: color.rojoClaro }}>
          <Text style={[texto.etiqueta, { marginBottom: espacio.s, color: color.rojo }]}>Banderas rojas</Text>
          <ListaPuntos puntos={e.banderasRojas} icono="flag" colorIcono={color.rojo} />
        </Tarjeta>
      ) : null}

      <View style={{ gap: espacio.s, marginTop: espacio.s }}>
        {alGenerarCv ? (
          <Boton titulo="Generar CV para esta oferta" icono="document-text-outline" alPulsar={alGenerarCv} cargando={generandoCv} />
        ) : null}
        <Boton titulo="Guardar como candidatura" icono="bookmark-outline" variante="secundario" alPulsar={guardarComoCandidatura} />
      </View>
    </View>
  );
}
