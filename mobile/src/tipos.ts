export type EstadoCandidatura = 'PENDIENTE' | 'ENVIADA' | 'ESPERANDO_RESPUESTA' | 'ENTREVISTA' | 'DESCARTADA';
export type Semaforo = 'VERDE' | 'AMARILLO' | 'ROJO';
export type NivelTecnologia = 'SOLIDO' | 'MEDIO' | 'SIN_TOCAR';
export type Modalidad = 'PRESENCIAL' | 'HIBRIDO' | 'REMOTO';

export interface Sesion {
  token: string;
  usuarioId: number;
  nombre: string;
}

export interface Candidatura {
  id: number;
  empresa: string;
  puesto: string;
  enlace: string | null;
  estado: EstadoCandidatura;
  fecha: string;
  notas: string | null;
}

export type CandidaturaDatos = Omit<Candidatura, 'id'>;

export interface TecnologiaPerfil {
  nombre: string;
  nivel: NivelTecnologia;
}

export interface Experiencia {
  puesto: string;
  empresa: string;
  inicio: string;
  fin: string | null;
  descripcion: string | null;
}

export interface Formacion {
  titulo: string;
  centro: string | null;
  inicio: string | null;
  fin: string | null;
}

export interface Proyecto {
  nombre: string;
  descripcion: string | null;
  enlace: string | null;
  tecnologias: string | null;
}

export interface Idioma {
  nombre: string;
  nivel: string | null;
}

export interface Perfil {
  titular: string | null;
  telefono: string | null;
  linkedin: string | null;
  github: string | null;
  salarioMinimo: number | null;
  modalidad: Modalidad | null;
  ubicacion: string | null;
  tecnologias: TecnologiaPerfil[];
  experiencias: Experiencia[];
  formaciones: Formacion[];
  proyectos: Proyecto[];
  idiomas: Idioma[];
}

export interface Evaluacion {
  id: number;
  empresa: string | null;
  puesto: string | null;
  semaforo: Semaforo;
  resumen: string | null;
  pros: string[];
  huecos: string[];
  banderasRojas: string[];
  accionRecomendada: string | null;
  creadoEn: string;
  usosRestantesHoy: number;
}

export interface EvaluacionResumen {
  id: number;
  empresa: string | null;
  puesto: string | null;
  semaforo: Semaforo;
  creadoEn: string;
}

export interface Cv {
  id: number;
  empresa: string | null;
  puesto: string | null;
  huecos: string[];
  carta: string | null;
  creadoEn: string;
  urlPdf: string;
  urlDocx: string;
}
