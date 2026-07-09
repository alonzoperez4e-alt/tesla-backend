# Git hooks del proyecto

Hooks versionados en el repo. Git no los usa automáticamente: cada quien debe
apuntar `core.hooksPath` a esta carpeta **una vez por clon**:

```bash
git config core.hooksPath .githooks
```

## Hooks disponibles

- **pre-push**: ejecuta `./mvnw -q test` antes de cada push. Si los tests fallan,
  aborta el push. Para saltarlo puntualmente: `git push --no-verify`.

El objetivo es feedback rápido; la validación completa (cobertura, SonarCloud,
Checkov) corre en el pipeline de CI sobre el Pull Request.
