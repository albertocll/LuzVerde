import { fireEvent, render, screen } from '@testing-library/react-native';
import { router } from 'expo-router';
import { ResultadoEvaluacion } from '@/componentes/ResultadoEvaluacion';
import { CabeceraSemaforo } from '@/componentes/Semaforo';
import type { Evaluacion } from '@/tipos';

jest.mock('expo-router', () => ({ router: { push: jest.fn() } }));

const evaluacion: Evaluacion = {
  id: 1,
  empresa: 'Empresa Ejemplo',
  puesto: 'Desarrollador Java junior',
  semaforo: 'AMARILLO',
  resumen: 'Encajas en lo principal, pero piden Kubernetes.',
  pros: ['Java a nivel sólido'],
  huecos: ['Kubernetes'],
  banderasRojas: ['No dicen el salario'],
  accionRecomendada: 'Aplica y pregunta el salario en la primera llamada.',
  creadoEn: '2026-11-23T10:00:00Z',
  usosRestantesHoy: 8,
};

describe('Semáforo', () => {
  it('muestra el nombre y la frase de cada color', async () => {
    await render(<CabeceraSemaforo valor="VERDE" />);
    expect(screen.getByText('Verde')).toBeTruthy();
    expect(screen.getByText('Encaja contigo')).toBeTruthy();
  });
});

describe('ResultadoEvaluacion', () => {
  beforeEach(() => jest.clearAllMocks());

  it('pinta todas las partes de la evaluación', async () => {
    await render(<ResultadoEvaluacion evaluacion={evaluacion} />);

    expect(screen.getByText('Desarrollador Java junior · Empresa Ejemplo')).toBeTruthy();
    expect(screen.getByText('Amarillo')).toBeTruthy();
    expect(screen.getByText('Java a nivel sólido')).toBeTruthy();
    expect(screen.getByText('Kubernetes')).toBeTruthy();
    expect(screen.getByText('No dicen el salario')).toBeTruthy();
    expect(screen.getByText('Qué hacer ahora')).toBeTruthy();
  });

  it('solo ofrece generar el CV si se le pasa la acción', async () => {
    const { rerender } = await render(<ResultadoEvaluacion evaluacion={evaluacion} />);
    expect(screen.queryByText('Generar CV para esta oferta')).toBeNull();

    const generar = jest.fn();
    await rerender(<ResultadoEvaluacion evaluacion={evaluacion} alGenerarCv={generar} />);
    await fireEvent.press(screen.getByText('Generar CV para esta oferta'));
    expect(generar).toHaveBeenCalled();
  });

  it('guarda la oferta como candidatura con los datos rellenos', async () => {
    await render(<ResultadoEvaluacion evaluacion={evaluacion} />);

    await fireEvent.press(screen.getByText('Guardar como candidatura'));

    expect(router.push).toHaveBeenCalledWith({
      pathname: '/candidatura/[id]',
      params: expect.objectContaining({
        id: 'nueva',
        empresa: 'Empresa Ejemplo',
        puesto: 'Desarrollador Java junior',
      }),
    });
  });

  it('oculta las banderas rojas si no hay', async () => {
    await render(<ResultadoEvaluacion evaluacion={{ ...evaluacion, banderasRojas: [] }} />);
    expect(screen.queryByText('Banderas rojas')).toBeNull();
  });
});
