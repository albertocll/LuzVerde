# pruebas de extremo a extremo con Maestro

Flujos de [Maestro](https://maestro.mobile.dev) sobre la app instalada en un emulador de Android.

| Flujo | Qué prueba |
|---|---|
| `login.yaml` | Crear cuenta, cerrar sesión y volver a entrar |
| `candidatura.yaml` | Crear una candidatura, verla en la lista y borrarla |
| `evaluar.yaml` | Evaluar una oferta con la IA (etiqueta `ia`, gasta un uso) |

`_registro.yaml` es un subflujo: crea una cuenta con un email único y entra. `config.yaml` lo deja fuera de la ejecución.

## cómo se lanzan

1. Backend en marcha: `docker compose up --build` en la raíz.
2. Emulador abierto con la app instalada con el perfil `desarrollo` de EAS (paquete `es.albertocll.luzverde`). Expo Go no vale porque tiene otro paquete. La app apunta a `http://10.0.2.2:8080`, que es el backend del ordenador.
3. Desde `mobile/`:

```bash
maestro test --exclude-tags ia .maestro    # sin gastar IA
maestro test .maestro                      # todos, con ANTHROPIC_API_KEY en el backend
```

## estado

Los flujos están escritos contra los textos y etiquetas de accesibilidad de la app, pero todavía no se han ejecutado en un emulador. La primera ejecución puede pedir ajustar algún `index` donde el mismo texto sale dos veces (la pestaña y el botón "Evaluar", o el botón y el diálogo "Borrar").
