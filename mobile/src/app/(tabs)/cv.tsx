import Ionicons from '@expo/vector-icons/Ionicons';
import * as Sharing from 'expo-sharing';
import { useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { Pressable, Share, StyleSheet, Text, View } from 'react-native';
import { api, ErrorApi } from '@/api';
import { AvisoError, Boton, Campo, Cargando, ListaPuntos, Pantalla, Seccion, Tarjeta, Vacio } from '@/componentes/ui';
import { fechaCorta } from '@/formato';
import { color, espacio, radio, texto } from '@/tema';
import type { Cv } from '@/tipos';

const MIME = {
  pdf: 'application/pdf',
  docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
};

export default function PantallaCv() {
  const [lista, setLista] = useState<Cv[] | null>(null);
  const [abierto, setAbierto] = useState<number | null>(null);
  const [oferta, setOferta] = useState('');
  const [generando, setGenerando] = useState(false);
  const [descargando, setDescargando] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    try {
      const cvs = await api.cvs();
      setLista(cvs);
      setAbierto((a) => a ?? cvs[0]?.id ?? null);
    } catch (e) {
      setError(e instanceof ErrorApi ? e.message : 'No se han podido cargar los CV.');
      setLista((l) => l ?? []);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      void cargar();
    }, [cargar]),
  );

  const generar = async () => {
    setError(null);
    setGenerando(true);
    try {
      const cv = await api.generarCv(oferta.trim());
      setOferta('');
      setLista((l) => [cv, ...(l ?? [])]);
      setAbierto(cv.id);
    } catch (e) {
      setError(e instanceof ErrorApi ? e.message : 'No se ha podido generar el CV.');
    } finally {
      setGenerando(false);
    }
  };

  const descargar = async (cv: Cv, formato: 'pdf' | 'docx') => {
    setError(null);
    setDescargando(`${cv.id}-${formato}`);
    try {
      const uri = await api.descargarCv(cv, formato);
      if (await Sharing.isAvailableAsync()) {
        await Sharing.shareAsync(uri, {
          mimeType: MIME[formato],
          dialogTitle: formato === 'pdf' ? 'Abrir o compartir el CV en PDF' : 'Abrir o compartir el CV en Word',
        });
      } else {
        setError(`Guardado en ${uri}`);
      }
    } catch (e) {
      setError(e instanceof ErrorApi ? e.message : 'No se ha podido descargar el archivo.');
    } finally {
      setDescargando(null);
    }
  };

  const compartirCarta = (carta: string) => Share.share({ message: carta });

  return (
    <Pantalla titulo="CV adaptado" subtitulo="Un CV para cada oferta, sin inventar nada.">
      <AvisoError mensaje={error} />

      <Tarjeta>
        <Campo
          etiqueta="Oferta"
          value={oferta}
          onChangeText={setOferta}
          multiline
          placeholder="Pega la oferta para la que quieres el CV"
        />
        <Boton
          titulo={generando ? 'Generando...' : 'Generar CV y carta'}
          icono="sparkles-outline"
          alPulsar={generar}
          cargando={generando}
          desactivado={oferta.trim().length < 80}
        />
        <Text style={[texto.suave, { fontSize: 12, marginTop: espacio.s }]}>
          Formato ATS: una columna, texto seleccionable, sin tablas ni gráficos.
        </Text>
      </Tarjeta>

      <Seccion titulo="Tus CV">
        {lista === null ? (
          <Cargando />
        ) : lista.length === 0 ? (
          <Vacio icono="document-text-outline" titulo="Todavía no hay CV" texto="Genera el primero desde una oferta." />
        ) : (
          <View style={{ gap: espacio.m }}>
            {lista.map((cv) => {
              const desplegado = abierto === cv.id;
              return (
                <Tarjeta key={cv.id}>
                  <Pressable onPress={() => setAbierto(desplegado ? null : cv.id)} style={estilos.cabecera}>
                    <View style={estilos.icono}>
                      <Ionicons name="document-text" size={22} color={color.primario} />
                    </View>
                    <View style={{ flex: 1 }}>
                      <Text style={texto.subtitulo} numberOfLines={1}>
                        {cv.puesto || 'CV adaptado'}
                      </Text>
                      <Text style={[texto.suave, { fontSize: 13 }]} numberOfLines={1}>
                        {[cv.empresa, fechaCorta(cv.creadoEn)].filter(Boolean).join(' · ')}
                      </Text>
                    </View>
                    <Ionicons name={desplegado ? 'chevron-up' : 'chevron-down'} size={20} color={color.textoTenue} />
                  </Pressable>

                  <View style={estilos.botones}>
                    <Boton
                      titulo="PDF"
                      icono="download-outline"
                      alPulsar={() => descargar(cv, 'pdf')}
                      cargando={descargando === `${cv.id}-pdf`}
                      estilo={{ flex: 1 }}
                    />
                    <Boton
                      titulo="Word"
                      icono="download-outline"
                      variante="secundario"
                      alPulsar={() => descargar(cv, 'docx')}
                      cargando={descargando === `${cv.id}-docx`}
                      estilo={{ flex: 1 }}
                    />
                  </View>

                  {desplegado ? (
                    <View style={{ marginTop: espacio.l, gap: espacio.l }}>
                      {cv.huecos.length > 0 ? (
                        <View>
                          <Text style={[texto.etiqueta, { marginBottom: espacio.s }]}>
                            Lo que pide la oferta y no está en tu CV
                          </Text>
                          <ListaPuntos puntos={cv.huecos} icono="remove-circle" colorIcono={color.ambar} />
                        </View>
                      ) : null}
                      {cv.carta ? (
                        <View>
                          <View style={estilos.cartaCabecera}>
                            <Text style={texto.etiqueta}>Carta de presentación</Text>
                            <Boton titulo="Compartir" icono="share-outline" variante="texto" alPulsar={() => compartirCarta(cv.carta!)} />
                          </View>
                          <View style={estilos.carta}>
                            <Text style={texto.cuerpo} selectable>
                              {cv.carta}
                            </Text>
                          </View>
                        </View>
                      ) : null}
                    </View>
                  ) : null}
                </Tarjeta>
              );
            })}
          </View>
        )}
      </Seccion>
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  cabecera: { flexDirection: 'row', alignItems: 'center', gap: espacio.m },
  icono: {
    width: 44,
    height: 44,
    borderRadius: radio.m,
    backgroundColor: color.primarioClaro,
    alignItems: 'center',
    justifyContent: 'center',
  },
  botones: { flexDirection: 'row', gap: espacio.s, marginTop: espacio.l },
  cartaCabecera: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  carta: { backgroundColor: color.superficieSuave, borderRadius: radio.m, padding: espacio.m },
});
