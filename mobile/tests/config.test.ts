import { urlPorDefecto } from '@/config';

jest.mock('expo-secure-store', () => ({ getItemAsync: jest.fn(), setItemAsync: jest.fn(), deleteItemAsync: jest.fn() }));

describe('urlPorDefecto', () => {
  it('en desarrollo usa el localhost del ordenador si no hay EXPO_PUBLIC_API_URL', () => {
    expect(urlPorDefecto('desarrollo', undefined, 'android')).toBe('http://10.0.2.2:8080');
    expect(urlPorDefecto('desarrollo', undefined, 'web')).toBe('http://localhost:8080');
  });

  it('usa EXPO_PUBLIC_API_URL sin la barra final', () => {
    expect(urlPorDefecto('produccion', 'https://api.ejemplo.com/', 'android')).toBe('https://api.ejemplo.com');
    expect(urlPorDefecto('desarrollo', 'http://192.168.1.20:8080', 'android')).toBe('http://192.168.1.20:8080');
  });

  it('en producción nunca cae en localhost', () => {
    expect(urlPorDefecto('produccion', undefined, 'android')).toBe('');
  });
});
