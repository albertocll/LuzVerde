import { DateTimePickerAndroid } from '@react-native-community/datetimepicker';
import { fireEvent, render, screen } from '@testing-library/react-native';
import { Platform } from 'react-native';
import { CampoFecha } from '@/componentes/CampoFecha';

jest.mock('@react-native-community/datetimepicker', () => ({
  __esModule: true,
  default: () => null,
  DateTimePickerAndroid: { open: jest.fn() },
}));

const abrir = DateTimePickerAndroid.open as jest.Mock;

describe('CampoFecha en Android', () => {
  const plataforma = Platform.OS;

  beforeEach(() => {
    Platform.OS = 'android';
    abrir.mockReset();
  });

  afterAll(() => {
    Platform.OS = plataforma;
  });

  it('enseña la fecha legible y abre el calendario con ella', async () => {
    await render(<CampoFecha etiqueta="Fecha" valor="2026-11-20" alCambiar={jest.fn()} />);

    expect(screen.getByText('20 nov 2026')).toBeTruthy();
    await fireEvent.press(screen.getByRole('button'));

    const opciones = abrir.mock.calls[0][0];
    expect(opciones.mode).toBe('date');
    expect(opciones.value.getDate()).toBe(20);
    expect(opciones.value.getMonth()).toBe(10);
  });

  it('devuelve la fecha elegida en formato AAAA-MM-DD', async () => {
    const alCambiar = jest.fn();
    await render(<CampoFecha etiqueta="Fecha" valor="2026-11-20" alCambiar={alCambiar} />);
    await fireEvent.press(screen.getByRole('button'));

    abrir.mock.calls[0][0].onChange({ type: 'set' }, new Date(2026, 0, 5));
    expect(alCambiar).toHaveBeenCalledWith('2026-01-05');
  });

  it('no cambia nada si se cierra el calendario sin elegir', async () => {
    const alCambiar = jest.fn();
    await render(<CampoFecha etiqueta="Fecha" valor="2026-11-20" alCambiar={alCambiar} />);
    await fireEvent.press(screen.getByRole('button'));

    abrir.mock.calls[0][0].onChange({ type: 'dismissed' }, undefined);
    expect(alCambiar).not.toHaveBeenCalled();
  });
});
