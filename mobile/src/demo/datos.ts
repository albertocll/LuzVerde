import type { Candidatura, Cv, Evaluacion, Perfil } from '../tipos';

/** Datos inventados para el modo demo. No salen nunca de la vista previa web. */

export const candidaturasDemo: Candidatura[] = [
  {
    id: 1,
    empresa: 'Norte Digital',
    puesto: 'Desarrollador Java junior',
    enlace: 'https://example.com/ofertas/1',
    estado: 'ENTREVISTA',
    fecha: '2026-11-18',
    notas: 'Entrevista técnica el jueves 27 a las 10:00.',
  },
  {
    id: 2,
    empresa: 'Cooperativa Brisa',
    puesto: 'Backend Spring Boot',
    enlace: null,
    estado: 'ESPERANDO_RESPUESTA',
    fecha: '2026-11-12',
    notas: 'Prueba técnica entregada el 15.',
  },
  {
    id: 3,
    empresa: 'Logística Ébano',
    puesto: 'Programador full stack',
    enlace: 'https://example.com/ofertas/3',
    estado: 'ENVIADA',
    fecha: '2026-11-20',
    notas: null,
  },
  {
    id: 4,
    empresa: 'Estudio Faro',
    puesto: 'Desarrollador React Native',
    enlace: null,
    estado: 'PENDIENTE',
    fecha: '2026-11-23',
    notas: 'Preparar CV adaptado.',
  },
  {
    id: 5,
    empresa: 'Seguros Altamar',
    puesto: 'Analista programador COBOL',
    enlace: null,
    estado: 'DESCARTADA',
    fecha: '2026-11-02',
    notas: 'Piden 5 años de COBOL.',
  },
];

export const perfilDemo: Perfil = {
  titular: 'Desarrollador backend Java y Spring Boot',
  telefono: '600 000 000',
  linkedin: 'linkedin.com/in/demo',
  github: 'github.com/demo',
  salarioMinimo: 24000,
  modalidad: 'HIBRIDO',
  ubicacion: 'Madrid',
  tecnologias: [
    { nombre: 'Java', nivel: 'SOLIDO' },
    { nombre: 'Spring Boot', nivel: 'SOLIDO' },
    { nombre: 'PostgreSQL', nivel: 'MEDIO' },
    { nombre: 'React Native', nivel: 'MEDIO' },
    { nombre: 'Docker', nivel: 'MEDIO' },
    { nombre: 'Kubernetes', nivel: 'SIN_TOCAR' },
  ],
  experiencias: [
    {
      puesto: 'Desarrollador en prácticas',
      empresa: 'Consultora Prisma',
      inicio: '2025-03',
      fin: '2025-06',
      descripcion: 'API REST con Spring Boot y tests de integración.',
    },
  ],
  formaciones: [{ titulo: 'CFGS Desarrollo de Aplicaciones Multiplataforma', centro: 'IES Demo', inicio: '2023-09', fin: '2025-06' }],
  proyectos: [
    {
      nombre: 'Luz Verde',
      descripcion: 'Asesor de empleo con IA para Android.',
      enlace: 'github.com/demo/luz-verde',
      tecnologias: 'Spring Boot, PostgreSQL, Expo',
    },
  ],
  idiomas: [
    { nombre: 'Español', nivel: 'Nativo' },
    { nombre: 'Inglés', nivel: 'B2' },
  ],
};

export const evaluacionesDemo: Evaluacion[] = [
  {
    id: 1,
    empresa: 'Norte Digital',
    puesto: 'Desarrollador Java junior',
    semaforo: 'VERDE',
    resumen: 'Piden Java, Spring Boot y SQL, que es justo tu stack. El salario está por encima de tu mínimo.',
    pros: ['Java y Spring Boot como stack principal', 'Híbrido en Madrid', 'Salario de 26.000 a 30.000 euros'],
    huecos: ['Mencionan Kafka, que no has tocado'],
    banderasRojas: [],
    accionRecomendada: 'Aplica hoy y genera un CV adaptado.',
    creadoEn: '2026-11-18T09:12:00Z',
    usosRestantesHoy: 8,
  },
  {
    id: 2,
    empresa: 'Logística Ébano',
    puesto: 'Programador full stack',
    semaforo: 'AMARILLO',
    resumen: 'Encaja el backend, pero el frontend es Angular y no lo tienes en el perfil.',
    pros: ['Backend en Spring Boot', 'Equipo pequeño con mentor'],
    huecos: ['Angular', 'Experiencia mínima de 2 años'],
    banderasRojas: ['No indican salario'],
    accionRecomendada: 'Aplica si te interesa aprender Angular. Pregunta por el salario.',
    creadoEn: '2026-11-20T17:40:00Z',
    usosRestantesHoy: 9,
  },
  {
    id: 3,
    empresa: 'Seguros Altamar',
    puesto: 'Analista programador COBOL',
    semaforo: 'ROJO',
    resumen: 'El puesto es de mantenimiento COBOL en host y piden 5 años de experiencia.',
    pros: ['Contrato indefinido'],
    huecos: ['COBOL', 'JCL', '5 años de experiencia'],
    banderasRojas: ['Presencial cinco días', 'Guardias de fin de semana'],
    accionRecomendada: 'No aplicar.',
    creadoEn: '2026-11-02T11:05:00Z',
    usosRestantesHoy: 9,
  },
];

export const cvsDemo: Cv[] = [
  {
    id: 1,
    empresa: 'Norte Digital',
    puesto: 'Desarrollador Java junior',
    huecos: ['Kafka: no aparece en tu perfil, así que no se ha puesto en el CV.'],
    carta:
      'Hola, equipo de Norte Digital. Soy desarrollador backend con Java y Spring Boot. En mis prácticas en Consultora Prisma hice una API REST con tests de integración, y en Luz Verde he montado un backend con PostgreSQL y Docker. Me encaja el puesto porque trabajáis con el mismo stack. Me gustaría contaros más en una entrevista.',
    creadoEn: '2026-11-18T09:20:00Z',
    urlPdf: '/api/v1/cv/1/pdf',
    urlDocx: '/api/v1/cv/1/docx',
  },
];
