# Logseq para Eli

Fork de Logseq 0.10.15 orientado a org-mode nativo de Emacs. Lee `docs/eli/ROADMAP.md`
antes de empezar: contiene objetivos, fases y estado.

- Rama de trabajo: `mi-rama-0.10.15`. Cambios en ramas `eli/...` y PR hacia esa rama.
- No hay Clojure en el entorno de trabajo: las pruebas se apoyan en GitHub Actions.
  Comprueba el resultado de los checks del PR antes de dar un cambio por bueno.
- No compilar instaladores ni lanzar workflows de release salvo petición expresa.
  Excepción pedida por Eli: la web se publica sola en GitHub Pages al fusionar un PR
  (`.github/workflows/publish-web.yml`).
- Ficheros de ejemplo org en `test-fixtures/org/`; se validan con Emacs en
  `.github/workflows/emacs-validate.yml` (script: `scripts/emacs/validate.el`).
- Mantén el estado de `docs/eli/ROADMAP.md` al día al terminar cada fase.
