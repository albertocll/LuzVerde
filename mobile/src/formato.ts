const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

/** "2026-11-23" o un instante ISO pasa a "23 nov 2026". */
export function fechaCorta(valor: string): string {
  const [anio, mes, dia] = valor.slice(0, 10).split('-').map(Number);
  if (!anio || !mes || !dia) return valor;
  return `${dia} ${MESES[mes - 1]} ${anio}`;
}

export function hoyIso(): string {
  return dateAIso(new Date());
}

/** "2026-11-23" pasa a un Date a medianoche en la hora local. Si no es válida, hoy. */
export function isoADate(valor: string): Date {
  if (!esFechaValida(valor)) return new Date();
  const [anio, mes, dia] = valor.split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

/** Un Date pasa a "2026-11-23" con la fecha local, sin desfase por la zona horaria. */
export function dateAIso(fecha: Date): string {
  const mes = String(fecha.getMonth() + 1).padStart(2, '0');
  const dia = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

export function esFechaValida(valor: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(valor)) return false;
  const d = new Date(`${valor}T00:00:00`);
  return !Number.isNaN(d.getTime()) && d.toISOString().startsWith(valor.slice(0, 4));
}

export function esMesValido(valor: string): boolean {
  return /^\d{4}-(0[1-9]|1[0-2])$/.test(valor);
}

/** Texto vacío pasa a null, para enviar al backend. */
export function oNull(valor: string | null | undefined): string | null {
  const limpio = (valor ?? '').trim();
  return limpio ? limpio : null;
}
