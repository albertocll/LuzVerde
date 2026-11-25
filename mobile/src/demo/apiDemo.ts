import { ErrorApi } from '../api';
import type { api as apiReal } from '../api';
import type { Candidatura, CandidaturaDatos, Cv, Evaluacion, Perfil, Sesion } from '../tipos';
import { candidaturasDemo, cvsDemo, evaluacionesDemo, perfilDemo } from './datos';

/**
 * Sustituto de la API para la vista previa web. Todo vive en memoria y se pierde al recargar.
 * Solo se carga si EXPO_PUBLIC_DEMO=1 al construir. La build normal no lo incluye.
 */

const pausa = <T,>(valor: T, ms = 250) => new Promise<T>((r) => setTimeout(() => r(structuredClone(valor)), ms));

let candidaturas = [...candidaturasDemo];
let perfil: Perfil = perfilDemo;
let evaluaciones = [...evaluacionesDemo];
let cvs = [...cvsDemo];
let siguienteId = 100;

const sesionDemo = (nombre = 'Alex Demo'): Sesion => ({ token: 'demo', usuarioId: 1, nombre });

function buscar(id: number): Candidatura {
  const c = candidaturas.find((x) => x.id === id);
  if (!c) throw new ErrorApi('No se ha encontrado.', 404);
  return c;
}

export const apiDemo: typeof apiReal = {
  versionMinima: async () => pausa({ versionMinima: '1.0.0' }),
  login: async () => pausa(sesionDemo()),
  registro: async (nombre) => pausa(sesionDemo(nombre || undefined)),

  candidaturas: async (estado) => pausa(candidaturas.filter((c) => !estado || c.estado === estado)),
  candidatura: async (id) => pausa(buscar(id)),
  crearCandidatura: async (datos: CandidaturaDatos) => {
    const nueva = { ...datos, id: siguienteId++ };
    candidaturas = [nueva, ...candidaturas];
    return pausa(nueva);
  },
  actualizarCandidatura: async (id, datos) => {
    buscar(id);
    const nueva = { ...datos, id };
    candidaturas = candidaturas.map((c) => (c.id === id ? nueva : c));
    return pausa(nueva);
  },
  borrarCandidatura: async (id) => {
    candidaturas = candidaturas.filter((c) => c.id !== id);
    return pausa(undefined);
  },

  borrarCuenta: async () => {
    candidaturas = [];
    evaluaciones = [];
    cvs = [];
    perfil = structuredClone(perfilDemo);
    return pausa(undefined);
  },

  perfil: async () => pausa(perfil),
  guardarPerfil: async (nuevo) => {
    perfil = nuevo;
    return pausa(perfil);
  },

  evaluar: async () => {
    const base = evaluaciones[0];
    const nueva: Evaluacion = { ...base, id: siguienteId++, creadoEn: new Date().toISOString(), usosRestantesHoy: 7 };
    evaluaciones = [nueva, ...evaluaciones];
    return pausa(nueva, 900);
  },
  evaluaciones: async () =>
    pausa(evaluaciones.map(({ id, empresa, puesto, semaforo, creadoEn }) => ({ id, empresa, puesto, semaforo, creadoEn }))),
  evaluacion: async (id) => {
    const e = evaluaciones.find((x) => x.id === id);
    if (!e) throw new ErrorApi('No se ha encontrado.', 404);
    return pausa(e);
  },

  generarCv: async () => {
    const nuevo: Cv = { ...cvs[0], id: siguienteId++, creadoEn: new Date().toISOString() };
    cvs = [nuevo, ...cvs];
    return pausa(nuevo, 900);
  },
  cvs: async () => pausa(cvs),
  descargarCv: async () => {
    throw new ErrorApi('En el modo demo no se descargan archivos.', 0);
  },
};
