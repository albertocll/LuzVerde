import { Appearance } from 'react-native';
import type { EstadoCandidatura, Semaforo } from './tipos';

/** Paleta clara, la de siempre. */
const claro = {
  fondo: '#F3F6F4',
  superficie: '#FFFFFF',
  superficieSuave: '#EEF3F0',
  texto: '#0E1B16',
  textoSuave: '#5B6B64',
  textoTenue: '#8A9993',
  borde: '#DDE6E1',
  primario: '#16A34A',
  /** Verde oscuro para textos e iconos sobre fondos claros. */
  primarioOscuro: '#0F3D2E',
  primarioClaro: '#DCFCE7',
  primarioTenue: 'rgba(15,61,46,0.12)',
  /** Fondo de los bloques destacados (acción recomendada, total de candidaturas, chip activo). */
  relleno: '#0F3D2E',
  ambar: '#D97706',
  ambarClaro: '#FEF3C7',
  rojo: '#DC2626',
  rojoClaro: '#FEE2E2',
  azul: '#2563EB',
  azulClaro: '#DBEAFE',
  gris: '#64748B',
  grisClaro: '#E2E8F0',
  blanco: '#FFFFFF',
};

export type Paleta = typeof claro;

/**
 * Paleta oscura. Los "claros" pasan a fondos oscuros teñidos y los colores de texto se aclaran
 * para mantener el contraste. tests/tema.test.ts comprueba los contrastes de las dos paletas.
 */
const oscuro: Paleta = {
  fondo: '#0B1411',
  superficie: '#13201B',
  superficieSuave: '#1A2A24',
  texto: '#E7F0EC',
  textoSuave: '#A3B5AD',
  textoTenue: '#7B8D85',
  borde: '#26372F',
  primario: '#16A34A',
  primarioOscuro: '#86EFAC',
  primarioClaro: '#0F2E1F',
  primarioTenue: 'rgba(134,239,172,0.12)',
  relleno: '#14532D',
  ambar: '#F59E0B',
  ambarClaro: '#422006',
  rojo: '#F87171',
  rojoClaro: '#450A0A',
  azul: '#60A5FA',
  azulClaro: '#172554',
  gris: '#94A3B8',
  grisClaro: '#1E293B',
  blanco: '#FFFFFF',
};

export const PALETAS = { claro, oscuro };

/**
 * El esquema sigue el ajuste del sistema al abrir la app. Los estilos se crean al cargar cada
 * pantalla, así que si el usuario cambia el tema con la app abierta, se aplica al volver a abrirla.
 */
export const ESQUEMA: 'claro' | 'oscuro' = Appearance.getColorScheme() === 'dark' ? 'oscuro' : 'claro';

export const color: Paleta = PALETAS[ESQUEMA];

export const espacio = { xs: 4, s: 8, m: 12, l: 16, xl: 24, xxl: 32 };
export const radio = { s: 8, m: 12, l: 18, total: 999 };

export const texto = {
  titulo: { fontSize: 26, fontWeight: '800' as const, color: color.texto, letterSpacing: -0.5 },
  subtitulo: { fontSize: 18, fontWeight: '700' as const, color: color.texto },
  cuerpo: { fontSize: 15, color: color.texto, lineHeight: 21 },
  suave: { fontSize: 14, color: color.textoSuave, lineHeight: 20 },
  etiqueta: { fontSize: 13, fontWeight: '600' as const, color: color.textoSuave },
};

export const sombra = {
  shadowColor: '#0F3D2E',
  shadowOpacity: 0.06,
  shadowRadius: 10,
  shadowOffset: { width: 0, height: 3 },
  elevation: 2,
};

export const ESTADOS: Record<EstadoCandidatura, { nombre: string; color: string; fondo: string }> = {
  PENDIENTE: { nombre: 'Pendiente', color: color.gris, fondo: color.grisClaro },
  ENVIADA: { nombre: 'Enviada', color: color.azul, fondo: color.azulClaro },
  ESPERANDO_RESPUESTA: { nombre: 'Esperando respuesta', color: color.ambar, fondo: color.ambarClaro },
  ENTREVISTA: { nombre: 'Entrevista', color: color.primario, fondo: color.primarioClaro },
  DESCARTADA: { nombre: 'Descartada', color: color.rojo, fondo: color.rojoClaro },
};

export const SEMAFOROS: Record<Semaforo, { nombre: string; color: string; fondo: string; frase: string }> = {
  VERDE: { nombre: 'Verde', color: color.primario, fondo: color.primarioClaro, frase: 'Encaja contigo' },
  AMARILLO: { nombre: 'Amarillo', color: color.ambar, fondo: color.ambarClaro, frase: 'Encaja a medias' },
  ROJO: { nombre: 'Rojo', color: color.rojo, fondo: color.rojoClaro, frase: 'No encaja' },
};
