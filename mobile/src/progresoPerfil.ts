import type { Perfil } from './tipos';

export interface ProgresoPerfil {
  /** De 0 a 100. */
  porcentaje: number;
  /** Lo siguiente que conviene rellenar, en orden de importancia. */
  pendientes: string[];
}

/**
 * Cuánto ha rellenado el usuario de lo que la IA necesita para evaluar ofertas y hacer el CV.
 * El stack y la experiencia o proyectos pesan más porque sin ellos el CV sale vacío.
 */
export function calcularProgreso(perfil: Perfil | null): ProgresoPerfil {
  if (!perfil) return { porcentaje: 0, pendientes: ['tu stack', 'tu experiencia o proyectos', 'tu formación'] };

  const criterios: { hecho: boolean; peso: number; nombre: string }[] = [
    { hecho: perfil.tecnologias.length >= 3, peso: 25, nombre: 'tu stack (al menos 3 tecnologías)' },
    { hecho: perfil.experiencias.length + perfil.proyectos.length > 0, peso: 25, nombre: 'tu experiencia o proyectos' },
    { hecho: perfil.formaciones.length > 0, peso: 15, nombre: 'tu formación' },
    { hecho: !!perfil.titular, peso: 10, nombre: 'el titular del CV' },
    { hecho: !!perfil.salarioMinimo, peso: 10, nombre: 'tu salario mínimo' },
    { hecho: !!perfil.modalidad && !!perfil.ubicacion, peso: 10, nombre: 'modalidad y ubicación' },
    { hecho: perfil.idiomas.length > 0, peso: 5, nombre: 'tus idiomas' },
  ];

  const porcentaje = criterios.reduce((total, c) => total + (c.hecho ? c.peso : 0), 0);
  return { porcentaje, pendientes: criterios.filter((c) => !c.hecho).map((c) => c.nombre) };
}
