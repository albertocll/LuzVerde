import { useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { api, ErrorApi } from '@/api';
import { ResultadoEvaluacion } from '@/componentes/ResultadoEvaluacion';
import { AvisoError, Cargando, Pantalla } from '@/componentes/ui';
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

  return (
    <Pantalla conBordes={false}>
      <AvisoError mensaje={error} />
      {evaluacion ? <ResultadoEvaluacion evaluacion={evaluacion} /> : error ? null : <Cargando />}
    </Pantalla>
  );
}
