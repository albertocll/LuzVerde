import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { Alert } from 'react-native';
import { api, ErrorApi } from '@/api';
import { ResultadoEvaluacion } from '@/componentes/ResultadoEvaluacion';
import { AvisoError, Boton, Cargando, Pantalla } from '@/componentes/ui';
import { espacio } from '@/tema';
import type { Evaluacion } from '@/tipos';

export default function DetalleEvaluacion() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [evaluacion, setEvaluacion] = useState<Evaluacion | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .evaluacion(Number(id))
      .then(setEvaluacion)
      .catch((e) => setError(e instanceof ErrorApi ? e.message : 'No se ha podido cargar.'));
  }, [id]);

  const borrar = () => {
    if (!evaluacion) return;
    const nombre = evaluacion.puesto || 'esta oferta';
    Alert.alert('Borrar evaluación', `¿Seguro que quieres borrar la evaluación de ${nombre}? No recuperas el uso de IA.`, [
      { text: 'Cancelar', style: 'cancel' },
      {
        text: 'Borrar',
        style: 'destructive',
        onPress: async () => {
          try {
            await api.borrarEvaluacion(evaluacion.id);
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
      <AvisoError mensaje={error} />
      {evaluacion ? (
        <>
          <ResultadoEvaluacion evaluacion={evaluacion} />
          <Boton
            titulo="Borrar evaluación"
            icono="trash-outline"
            variante="peligro"
            alPulsar={borrar}
            estilo={{ marginTop: espacio.l }}
          />
        </>
      ) : error ? null : (
        <Cargando />
      )}
    </Pantalla>
  );
}
