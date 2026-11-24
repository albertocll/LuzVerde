const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

/** "2026-11-23" o un instante ISO pasa a "23 nov 2026". */
export function fechaCorta(valor: string): string {
  const [anio, mes, dia] = valor.slice(0, 10).split('-').map(Number);
  if (!anio || !mes || !dia) return valor;
  return `${dia} ${MESES[mes - 1]} ${anio}`;
}

export function hoyIso(): string {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
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
