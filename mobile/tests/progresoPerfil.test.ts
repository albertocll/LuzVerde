import { calcularProgreso } from '@/progresoPerfil';
import type { Perfil } from '@/tipos';

const vacio: Perfil = {
  titular: null,
  telefono: null,
  linkedin: null,
  github: null,
  salarioMinimo: null,
  modalidad: null,
  ubicacion: null,
  tecnologias: [],
  experiencias: [],
  formaciones: [],
  proyectos: [],
  idiomas: [],
};

describe('calcularProgreso', () => {
  it('un perfil vacío está al 0 % y lo primero es el stack', () => {
    const p = calcularProgreso(vacio);
    expect(p.porcentaje).toBe(0);
    expect(p.pendientes[0]).toContain('stack');
  });

  it('sin perfil cargado también da 0 %', () => {
    expect(calcularProgreso(null).porcentaje).toBe(0);
  });

  it('un perfil completo está al 100 % sin pendientes', () => {
    const completo: Perfil = {
      ...vacio,
      titular: 'Desarrollador junior',
      salarioMinimo: 20000,
      modalidad: 'HIBRIDO',
      ubicacion: 'Sevilla',
      tecnologias: [
        { nombre: 'Java', nivel: 'SOLIDO' },
        { nombre: 'React', nivel: 'MEDIO' },
        { nombre: 'SQL', nivel: 'MEDIO' },
      ],
      proyectos: [{ nombre: 'App', descripcion: null, enlace: null, tecnologias: null }],
      formaciones: [{ titulo: 'DAM', centro: null, inicio: null, fin: null }],
      idiomas: [{ nombre: 'Inglés', nivel: 'B1' }],
    };
    expect(calcularProgreso(completo)).toEqual({ porcentaje: 100, pendientes: [] });
  });

  it('dos tecnologías no bastan para el stack', () => {
    const p = calcularProgreso({
      ...vacio,
      tecnologias: [
        { nombre: 'Java', nivel: 'SOLIDO' },
        { nombre: 'SQL', nivel: 'MEDIO' },
      ],
    });
    expect(p.porcentaje).toBe(0);
  });
});
