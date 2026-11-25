import { dateAIso, esFechaValida, esMesValido, fechaCorta, hoyIso, isoADate, oNull } from '@/formato';
import { normalizarUrl } from '@/config';

jest.mock('expo-secure-store', () => ({
  getItemAsync: jest.fn(),
  setItemAsync: jest.fn(),
  deleteItemAsync: jest.fn(),
}));

describe('formato', () => {
  it('pasa de AAAA-MM-DD a Date local y vuelve igual', () => {
    const fecha = isoADate('2026-03-29');
    expect([fecha.getFullYear(), fecha.getMonth(), fecha.getDate()]).toEqual([2026, 2, 29]);
    expect(dateAIso(fecha)).toBe('2026-03-29');
  });

  it('con una fecha mal escrita abre el calendario en hoy', () => {
    expect(dateAIso(isoADate('29/03/2026'))).toBe(hoyIso());
  });

  it('muestra fechas cortas en español', () => {
    expect(fechaCorta('2026-11-23')).toBe('23 nov 2026');
    expect(fechaCorta('2026-01-05T10:00:00Z')).toBe('5 ene 2026');
  });

  it('devuelve el texto original si no es una fecha', () => {
    expect(fechaCorta('mañana')).toBe('mañana');
  });

  it('genera hoy en formato AAAA-MM-DD', () => {
    expect(hoyIso()).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('valida fechas y meses', () => {
    expect(esFechaValida('2026-11-23')).toBe(true);
    expect(esFechaValida('23/11/2026')).toBe(false);
    expect(esFechaValida('2026-13-01')).toBe(false);
    expect(esMesValido('2026-02')).toBe(true);
    expect(esMesValido('2026-2')).toBe(false);
    expect(esMesValido('2026-00')).toBe(false);
  });

  it('convierte textos vacíos en null', () => {
    expect(oNull('  ')).toBeNull();
    expect(oNull(undefined)).toBeNull();
    expect(oNull(' Sevilla ')).toBe('Sevilla');
  });
});

describe('normalizarUrl', () => {
  it('añade http y quita barras finales', () => {
    expect(normalizarUrl('10.0.2.2:8080/')).toBe('http://10.0.2.2:8080');
    expect(normalizarUrl(' https://api.ejemplo.com// ')).toBe('https://api.ejemplo.com');
    expect(normalizarUrl('')).toBe('');
  });
});
