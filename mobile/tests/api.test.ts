import { alExpirarSesion, api, ErrorApi, fijarToken } from '@/api';

jest.mock('expo-secure-store', () => ({
  getItemAsync: jest.fn(async () => 'http://servidor.test'),
  setItemAsync: jest.fn(),
  deleteItemAsync: jest.fn(),
}));

jest.mock('expo-file-system', () => ({
  File: jest.fn(),
  Paths: { cache: '/cache' },
}));

function respuesta(estado: number, cuerpo?: unknown): Response {
  return {
    ok: estado >= 200 && estado < 300,
    status: estado,
    text: async () => (cuerpo === undefined ? '' : JSON.stringify(cuerpo)),
  } as Response;
}

const fetchSimulado = jest.fn();
globalThis.fetch = fetchSimulado as unknown as typeof fetch;

beforeEach(() => {
  fetchSimulado.mockReset();
  fijarToken(null);
});

describe('api', () => {
  it('hace login contra la URL configurada y devuelve la sesión', async () => {
    fetchSimulado.mockResolvedValue(respuesta(200, { token: 'abc', usuarioId: 3, nombre: 'Ana' }));

    const sesion = await api.login('ana@ejemplo.com', 'secreta');

    expect(sesion).toEqual({ token: 'abc', usuarioId: 3, nombre: 'Ana' });
    const [url, opciones] = fetchSimulado.mock.calls[0];
    expect(url).toBe('http://servidor.test/api/v1/auth/login');
    expect(opciones.method).toBe('POST');
    expect(JSON.parse(opciones.body)).toEqual({ email: 'ana@ejemplo.com', password: 'secreta' });
    expect(opciones.headers.Authorization).toBeUndefined();
  });

  it('envía el token en las peticiones autenticadas', async () => {
    fijarToken('mi-token');
    fetchSimulado.mockResolvedValue(respuesta(200, []));

    await api.candidaturas('ENVIADA');

    const [url, opciones] = fetchSimulado.mock.calls[0];
    expect(url).toBe('http://servidor.test/api/v1/candidaturas?estado=ENVIADA');
    expect(opciones.headers.Authorization).toBe('Bearer mi-token');
  });

  it('usa el mensaje del servidor y los errores de cada campo', async () => {
    fetchSimulado.mockResolvedValue(
      respuesta(400, { detail: 'Datos no válidos', campos: { empresa: 'La empresa es obligatoria' } }),
    );

    const error = await api.crearCandidatura({} as never).catch((e) => e);

    expect(error).toBeInstanceOf(ErrorApi);
    expect(error.message).toBe('Datos no válidos');
    expect(error.estado).toBe(400);
    expect(error.campos.empresa).toBe('La empresa es obligatoria');
  });

  it('elige el mensaje por el code y no por el texto del servidor', async () => {
    fetchSimulado.mockResolvedValue(respuesta(429, { code: 'AI_DAILY_LIMIT', detail: 'texto que puede cambiar' }));

    const error = await api.evaluar('oferta').catch((e) => e);

    expect(error.codigo).toBe('AI_DAILY_LIMIT');
    expect(error.message).toContain('límite de usos de IA');
  });

  it('distingue los intentos de login del límite de IA aunque los dos sean 429', async () => {
    fetchSimulado.mockResolvedValue(respuesta(429, { code: 'TOO_MANY_ATTEMPTS', detail: 'x' }));

    await expect(api.login('a@b.com', 'x')).rejects.toThrow('Demasiados intentos');
  });

  it('usa el texto del servidor si el code es desconocido', async () => {
    fetchSimulado.mockResolvedValue(respuesta(409, { code: 'CODIGO_NUEVO', detail: 'Mensaje del servidor' }));

    await expect(api.perfil()).rejects.toThrow('Mensaje del servidor');
  });

  it('avisa cuando no hay conexión con el servidor', async () => {
    fetchSimulado.mockRejectedValue(new TypeError('Network request failed'));

    const error = await api.perfil().catch((e) => e);

    expect(error).toBeInstanceOf(ErrorApi);
    expect(error.estado).toBe(0);
    expect(error.message).toContain('http://servidor.test');
  });

  it('cierra la sesión si el token caduca, pero no en el login', async () => {
    const alCaducar = jest.fn();
    alExpirarSesion(alCaducar);
    fijarToken('caducado');

    fetchSimulado.mockResolvedValue(respuesta(401));
    await api.perfil().catch(() => undefined);
    expect(alCaducar).toHaveBeenCalledTimes(1);

    fetchSimulado.mockResolvedValue(respuesta(401, { detail: 'Email o contraseña incorrectos' }));
    await expect(api.login('a@b.com', 'x')).rejects.toThrow('Email o contraseña incorrectos');
    expect(alCaducar).toHaveBeenCalledTimes(1);
  });

  it('acepta respuestas sin cuerpo', async () => {
    fijarToken('t');
    fetchSimulado.mockResolvedValue(respuesta(204));

    await expect(api.borrarCandidatura(1)).resolves.toBeUndefined();
  });
  it('borra la cuenta con DELETE /cuenta y el token', async () => {
    fijarToken('mi-token');
    fetchSimulado.mockResolvedValue(respuesta(204));

    await expect(api.borrarCuenta()).resolves.toBeUndefined();

    const [url, opciones] = fetchSimulado.mock.calls[0];
    expect(url).toBe('http://servidor.test/api/v1/cuenta');
    expect(opciones.method).toBe('DELETE');
    expect(opciones.headers.Authorization).toBe('Bearer mi-token');
  });
});
