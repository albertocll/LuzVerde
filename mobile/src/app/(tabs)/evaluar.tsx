import { router, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { Text, View } from 'react-native';
import { api, ErrorApi } from '@/api';
import { AvisoPerfil } from '@/componentes/AvisoPerfil';
import { ResultadoEvaluacion } from '@/componentes/ResultadoEvaluacion';
import { Semaforo } from '@/componentes/Semaforo';
import { AvisoError, Boton, Campo, Pantalla, Seccion, Tarjeta } from '@/componentes/ui';
import { fechaCorta } from '@/formato';
import { color, espacio, texto } from '@/tema';
import type { Evaluacion, EvaluacionResumen } from '@/tipos';

const MINIMO = 80;

export default function Evaluar() {
  const [oferta, setOferta] = useState('');
  const [resultado, setResultado] = useState<Evaluacion | null>(null);
  const [historial, setHistorial] = useState<EvaluacionResumen[]>([]);
  const [evaluando, setEvaluando] = useState(false);
  const [generandoCv, setGenerandoCv] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useFocusEffect(
    useCallback(() => {
      api.evaluaciones().then(setHistorial).catch(() => undefined);
    }, []),
  );

  const evaluar = async () => {
    setError(null);
    setEvaluando(true);
    try {
      const r = await api.evaluar(oferta.trim());
      setResultado(r);
      setHistorial((h) => [r, ...h.filter((x) => x.id !== r.id)]);
    } catch (e) {
      setError(e instanceof ErrorApi ? e.message : 'No se ha podido evaluar la oferta.');
    } finally {
      setEvaluando(false);
    }
  };

  const generarCv = async () => {
    setError(null);
    setGenerandoCv(true);
    try {
      await api.generarCv(oferta.trim());
      router.push('/cv');
    } catch (e) {
      setError(e instanceof ErrorApi ? e.message : 'No se ha podido generar el CV.');
    } finally {
      setGenerandoCv(false);
    }
  };

  const nueva = () => {
    setResultado(null);
    setOferta('');
    setError(null);
  };

  const caracteres = oferta.trim().length;

  return (
    <Pantalla titulo="Evaluar oferta" subtitulo="Pega la oferta y te digo si encaja con tu perfil.">
      {resultado ? null : <AvisoPerfil />}
      <AvisoError mensaje={error} />

      {resultado ? (
        <>
          <ResultadoEvaluacion evaluacion={resultado} alGenerarCv={generarCv} generandoCv={generandoCv} />
          <Text style={[texto.suave, { textAlign: 'center', marginTop: espacio.l }]}>
            Te quedan {resultado.usosRestantesHoy} usos de IA hoy.
          </Text>
          <Boton titulo="Evaluar otra oferta" variante="texto" icono="refresh" alPulsar={nueva} />
        </>
      ) : (
        <>
          <Campo
            etiqueta="Texto de la oferta"
            value={oferta}
            onChangeText={setOferta}
            multiline
            placeholder="Copia aquí la descripción completa: requisitos, salario, modalidad..."
            style={{ minHeight: 200 }}
            ayuda={caracteres < MINIMO ? `Mínimo ${MINIMO} caracteres (llevas ${caracteres}).` : `${caracteres} caracteres.`}
          />
          <Boton
            titulo={evaluando ? 'Evaluando...' : 'Evaluar'}
            icono="sparkles-outline"
            alPulsar={evaluar}
            cargando={evaluando}
            desactivado={caracteres < MINIMO}
          />
          <Text style={[texto.suave, { textAlign: 'center', marginTop: espacio.m, fontSize: 13 }]}>
            {evaluando ? 'La IA tarda unos segundos en leer la oferta.' : 'La oferta se compara con tu perfil.'}
          </Text>
        </>
      )}

      {historial.length > 0 ? (
        <Seccion titulo="Evaluaciones recientes">
          <View style={{ gap: espacio.s }}>
            {historial.slice(0, 10).map((h) => (
              <Tarjeta key={h.id} alPulsar={() => router.push(`/evaluacion/${h.id}`)} estilo={{ paddingVertical: espacio.m }}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: espacio.m }}>
                  <Semaforo valor={h.semaforo} tamano="mini" />
                  <View style={{ flex: 1 }}>
                    <Text style={{ fontWeight: '700', color: color.texto }} numberOfLines={1}>
                      {h.puesto || 'Oferta sin título'}
                    </Text>
                    <Text style={[texto.suave, { fontSize: 13 }]} numberOfLines={1}>
                      {[h.empresa, fechaCorta(h.creadoEn)].filter(Boolean).join(' · ')}
                    </Text>
                  </View>
                </View>
              </Tarjeta>
            ))}
          </View>
        </Seccion>
      ) : null}
    </Pantalla>
  );
}
