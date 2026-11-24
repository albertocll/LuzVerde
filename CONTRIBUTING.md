# cómo contribuir

## preparar el entorno

Necesitas Java 21, Maven, Docker, Node 22 y Android Studio con un emulador.

```
cp .env.example .env        # rellena DB_PASSWORD y JWT_SECRET
docker compose up --build   # backend y PostgreSQL
cd mobile && npm install && npx expo start --android
```

## antes de abrir un pull request

```
cd backend && mvn verify
cd mobile && npm run typecheck && npm test
```

La CI ejecuta lo mismo, más la búsqueda de secretos y CodeQL.

## normas

- Una rama por cambio: `feat/...`, `fix/...`, `docs/...`.
- Mensajes de commit en español, en presente y con una primera línea corta: "Añade filtro por fecha en candidaturas".
- Textos de la app, mensajes de error y documentación en español.
- Cada endpoint nuevo lleva su test de integración, incluido el caso de que otro usuario intente acceder.
- Nada de claves, tokens ni datos personales reales en el código, en los tests ni en los issues.
- Si el cambio afecta a quien usa la app, añádelo a [CHANGELOG.md](CHANGELOG.md).

## dónde está cada cosa

Mira [docs/arquitectura.md](docs/arquitectura.md) y [docs/decisiones.md](docs/decisiones.md).
