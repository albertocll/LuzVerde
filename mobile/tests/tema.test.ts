import { PALETAS } from '@/tema';

/** Contraste WCAG entre dos colores #RRGGBB. */
function contraste(a: string, b: string): number {
  const luminancia = (hex: string) => {
    const [r, g, bl] = [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16) / 255);
    const canal = (c: number) => (c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
    return 0.2126 * canal(r) + 0.7152 * canal(g) + 0.0722 * canal(bl);
  };
  const [x, y] = [luminancia(a), luminancia(b)].sort((m, n) => n - m);
  return (x + 0.05) / (y + 0.05);
}

describe.each(Object.entries(PALETAS))('paleta %s', (_, c) => {
  it('el texto se lee sobre el fondo y las tarjetas (AAA)', () => {
    expect(contraste(c.texto, c.fondo)).toBeGreaterThanOrEqual(7);
    expect(contraste(c.texto, c.superficie)).toBeGreaterThanOrEqual(7);
  });

  it('el texto secundario y el verde de títulos pasan AA', () => {
    expect(contraste(c.textoSuave, c.superficie)).toBeGreaterThanOrEqual(4.5);
    expect(contraste(c.primarioOscuro, c.fondo)).toBeGreaterThanOrEqual(4.5);
    expect(contraste(c.blanco, c.relleno)).toBeGreaterThanOrEqual(4.5);
  });

  it('los chips de estado se distinguen de su fondo', () => {
    const pares: [string, string][] = [
      [c.gris, c.grisClaro],
      [c.azul, c.azulClaro],
      [c.ambar, c.ambarClaro],
      [c.primario, c.primarioClaro],
      [c.rojo, c.rojoClaro],
    ];
    for (const [texto, fondo] of pares) {
      expect(contraste(texto, fondo)).toBeGreaterThanOrEqual(2.8);
    }
  });
});

it('las dos paletas tienen los mismos colores', () => {
  expect(Object.keys(PALETAS.oscuro).sort()).toEqual(Object.keys(PALETAS.claro).sort());
});
