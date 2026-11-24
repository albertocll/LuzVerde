import { render, screen } from '@testing-library/react-native';
import { Text } from 'react-native';
import { api } from '@/api';
import { ComprobadorVersion } from '@/componentes/AvisoActualizar';
import { compararVersiones, hayQueActualizar } from '@/version';

jest.mock('expo-constants', () => ({ __esModule: true, default: { expoConfig: { version: '1.0.0' } } }));
jest.mock('expo-secure-store', () => ({ getItemAsync: jest.fn(), setItemAsync: jest.fn(), deleteItemAsync: jest.fn() }));
jest.mock('expo-file-system', () => ({ File: jest.fn(), Paths: { cache: '/cache' } }));

describe('versiones', () => {
  it('compara número a número y no como texto', () => {
    expect(compararVersiones('1.10.0', '1.9.0')).toBeGreaterThan(0);
    expect(compararVersiones('1.0.0', '1.0.0')).toBe(0);
    expect(compararVersiones('1.0', '1.0.1')).toBeLessThan(0);
  });

  it('pide actualizar solo si la instalada es menor que la mínima', () => {
    expect(hayQueActualizar('1.0.0', '1.0.1')).toBe(true);
    expect(hayQueActualizar('1.0.1', '1.0.1')).toBe(false);
    expect(hayQueActualizar('2.0.0', '1.5.0')).toBe(false);
  });
});

describe('ComprobadorVersion', () => {
  afterEach(() => jest.restoreAllMocks());

  it('muestra el aviso si el backend exige una versión mayor', async () => {
    jest.spyOn(api, 'versionMinima').mockResolvedValue({ versionMinima: '1.1.0' });
    await render(
      <ComprobadorVersion>
        <Text>App</Text>
      </ComprobadorVersion>,
    );
    expect(await screen.findByText('Hay una versión nueva')).toBeTruthy();
    expect(screen.queryByText('App')).toBeNull();
  });

  it('deja pasar si la versión vale o si no hay conexión', async () => {
    jest.spyOn(api, 'versionMinima').mockRejectedValue(new Error('sin red'));
    await render(
      <ComprobadorVersion>
        <Text>App</Text>
      </ComprobadorVersion>,
    );
    expect(await screen.findByText('App')).toBeTruthy();
  });
});
