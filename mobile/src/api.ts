import { File, Paths } from 'expo-file-system';
import { cargarUrlServidor } from './config';
import type {
  Candidatura,
  CandidaturaDatos,
  Cv,
  EstadoCandidatura,
  Evaluacion,
  EvaluacionResumen,
  Perfil,
  Sesion,
} from './tipos';

export class ErrorApi extends Error {
  constructor(
    message: string,
    readonly estado: number,
    readonly campos: Record<string, string> = {},
    /** Código fijo del backend, como INVALID_CREDENTIALS. Null si la respuesta no lo trae. */
    readonly codigo: string | null = null,
  ) {
    super(message);
  }
}

let token: string | null = null;
let alCaducarSesion: (() => void) | null = null;

export function fijarToken(nuevo: string | null) {
  token = nuevo;
}

export function alExpirarSesion(callback: () => void) {
  alCaducarSesion = callback;
}

/**
 * Mensajes por el code que manda el backend. La app elige el texto por el código,
 * así un cambio de redacción en el servidor no cambia lo que ve el usuario.
 */
export const MENSAJES_POR_CODIGO: Record<string, string> = {
  INVALID_CREDENTIALS: 'El email o la contraseña no son correctos.',
  TOO_MANY_ATTEMPTS: 'Demasiados intentos. Espera unos minutos y vuelve a probar.',
  AI_DAILY_LIMIT: 'Has llegado al límite de usos de IA por hoy. Vuelve mañana.',
  AI_NOT_CONFIGURED: 'El servidor no tiene la IA configurada todavía.',
  AI_FAILED: 'El servicio de IA ha fallado. Prueba otra vez en un rato.',
  VALIDATION_ERROR: 'Revisa los campos marcados.',
  MALFORMED_REQUEST: 'La app ha enviado algo que el servidor no entiende. Actualiza la app.',
  NOT_FOUND: 'No se ha encontrado.',
  INTERNAL_ERROR: 'Ha fallado el servidor. Inténtalo de nuevo más tarde.',
};

/** Si el login tras registrarse falla, el email ya tenía cuenta con otra contraseña. No se dice cuál de las dos. */
export const MENSAJE_REGISTRO_FALLIDO =
  'No se ha podido crear la cuenta. Si ya tienes una con ese email, entra con tu contraseña.';

/** Solo para respuestas sin code, como el 401 de un token caducado. */
const MENSAJES: Record<number, string> = {
  401: 'Tu sesión ha caducado. Vuelve a entrar.',
  404: 'No se ha encontrado.',
  429: 'Demasiadas peticiones. Espera un poco y vuelve a probar.',
  502: 'El servicio de IA ha fallado. Prueba otra vez en un rato.',
  503: 'El servidor no tiene la IA configurada todavía.',
};

/** Versión de la API. Las rutas de abajo son relativas a ella. */
export const PREFIJO_API = '/api/v1';

async function peticion<T>(ruta: string, opciones: RequestInit = {}): Promise<T> {
  const base = await cargarUrlServidor();
  let respuesta: Response;
  try {
    respuesta = await fetch(`${base}${PREFIJO_API}${ruta}`, {
      ...opciones,
      headers: {
        Accept: 'application/json',
        ...(opciones.body ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...opciones.headers,
      },
    });
  } catch {
    throw new ErrorApi(`No se puede conectar con el servidor (${base}). Revisa la URL en Perfil.`, 0);
  }

  if (respuesta.status === 204) {
    return undefined as T;
  }

  const cuerpo = await respuesta.text();
  const datos = cuerpo ? safeJson(cuerpo) : null;

  if (!respuesta.ok) {
    if (respuesta.status === 401 && token && !ruta.startsWith('/auth/')) {
      alCaducarSesion?.();
    }
    const codigo = typeof datos?.code === 'string' ? datos.code : null;
    const detalle = typeof datos?.detail === 'string' ? datos.detail : null;
    throw new ErrorApi(
      (codigo ? MENSAJES_POR_CODIGO[codigo] : undefined) ?? detalle ?? MENSAJES[respuesta.status] ?? `Error ${respuesta.status}`,
      respuesta.status,
      datos?.campos ?? {},
      codigo,
    );
  }
  return datos as T;
}

function safeJson(texto: string): any {
  try {
    return JSON.parse(texto);
  } catch {
    return null;
  }
}

const json = (cuerpo: unknown) => JSON.stringify(cuerpo);

interface RespuestaToken {
  token: string;
  usuarioId: number;
  nombre: string;
}

async function login(email: string, password: string): Promise<Sesion> {
  const r = await peticion<RespuestaToken>('/auth/login', { method: 'POST', body: json({ email, password }) });
  return { token: r.token, usuarioId: r.usuarioId, nombre: r.nombre };
}

const apiServidor = {
  versionMinima: () => peticion<{ versionMinima: string }>('/version'),

  login,
  /**
   * El backend responde igual exista o no el email, para no revelar quién tiene cuenta.
   * Por eso el registro no trae token: se entra justo después con el login.
   */
  async registro(nombre: string, email: string, password: string): Promise<Sesion> {
    await peticion<{ mensaje: string }>('/auth/registro', {
      method: 'POST',
      body: json({ nombre, email, password }),
    });
    try {
      return await login(email, password);
    } catch (e) {
      if (e instanceof ErrorApi && e.codigo === 'INVALID_CREDENTIALS') {
        throw new ErrorApi(MENSAJE_REGISTRO_FALLIDO, e.estado, {}, e.codigo);
      }
      throw e;
    }
  },

  candidaturas: (estado?: EstadoCandidatura) =>
    peticion<Candidatura[]>(`/candidaturas${estado ? `?estado=${estado}` : ''}`),
  candidatura: (id: number) => peticion<Candidatura>(`/candidaturas/${id}`),
  crearCandidatura: (datos: CandidaturaDatos) =>
    peticion<Candidatura>('/candidaturas', { method: 'POST', body: json(datos) }),
  actualizarCandidatura: (id: number, datos: CandidaturaDatos) =>
    peticion<Candidatura>(`/candidaturas/${id}`, { method: 'PUT', body: json(datos) }),
  borrarCandidatura: (id: number) => peticion<void>(`/candidaturas/${id}`, { method: 'DELETE' }),

  /** Borra la cuenta y todos sus datos en el servidor. No se puede deshacer. */
  borrarCuenta: () => peticion<void>('/cuenta', { method: 'DELETE' }),

  perfil: () => peticion<Perfil>('/perfil'),
  guardarPerfil: (perfil: Perfil) => peticion<Perfil>('/perfil', { method: 'PUT', body: json(perfil) }),

  evaluar: (ofertaTexto: string) =>
    peticion<Evaluacion>('/evaluaciones', { method: 'POST', body: json({ ofertaTexto }) }),
  evaluaciones: () => peticion<EvaluacionResumen[]>('/evaluaciones'),
  evaluacion: (id: number) => peticion<Evaluacion>(`/evaluaciones/${id}`),

  generarCv: (ofertaTexto: string) => peticion<Cv>('/cv', { method: 'POST', body: json({ ofertaTexto }) }),
  cvs: () => peticion<Cv[]>('/cv'),

  /** Descarga el PDF o el DOCX a la caché del móvil y devuelve su URI local. */
  async descargarCv(cv: Cv, formato: 'pdf' | 'docx'): Promise<string> {
    const base = await cargarUrlServidor();
    const ruta = formato === 'pdf' ? cv.urlPdf : cv.urlDocx;
    const nombre = `CV-${(cv.empresa ?? 'oferta').replace(/[^\p{L}\p{N}]+/gu, '-')}-${cv.id}.${formato}`;
    const destino = new File(Paths.cache, nombre);
    if (destino.exists) {
      destino.delete();
    }
    const tarea = File.createDownloadTask(`${base}${ruta}`, destino, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    const archivo = await tarea.downloadAsync();
    if (!archivo) {
      throw new ErrorApi('La descarga no se ha completado.', 0);
    }
    return archivo.uri;
  },
};

/**
 * Con EXPO_PUBLIC_DEMO=1 (solo en npm run web:demo) la app usa datos falsos en memoria.
 * Babel sustituye la variable al construir y Metro elimina el require en la build normal.
 */
export const api: typeof apiServidor =
  process.env.EXPO_PUBLIC_DEMO === '1' ? require('./demo/apiDemo').apiDemo : apiServidor;
