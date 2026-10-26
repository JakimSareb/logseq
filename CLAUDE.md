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

## "Adelante X.YY"

Si Eli escribe "Adelante 1.01" (o cualquier otra versión de la tabla del ROADMAP):

1. Lee `docs/eli/ROADMAP.md`, busca esa versión y haz esa fase entera, sin pedir
   confirmación salvo decisiones que no se puedan deshacer o que cambien el diseño.
2. Rama `eli/...` desde `mi-rama-0.10.15`, PR hacia `mi-rama-0.10.15`.
3. Sube la versión (`resources/package.json` y `src/main/frontend/version.cljs`, en técnico
   `X.Y.Z`; ver "Cómo se trabaja con versiones" en el ROADMAP) y actualiza el ROADMAP.
4. Espera los checks. Si salen en verde (salvo `e2e-test`, que falla por `static/yarn.lock`
   también en la base), fusiona tú el PR: Eli lo ha autorizado de forma permanente.
5. Si algo falla, arréglalo y vuelve a comprobar; si hace falta una decisión de Eli,
   para y pregúntale lo justo.
6. Al terminar, di en pocas líneas qué cambió y cuál es la siguiente versión.

### Rangos: "Adelante 1.01-1.03"

Si Eli pide un rango, haz las versiones en orden en la misma rama y el mismo PR:

- un commit por versión, cada uno con su subida de versión técnica y su fila del ROADMAP
  marcada como hecha (la versión final del rango es la que queda en `package.json`);
- espera los checks y fusiona una sola vez, al terminar el rango;
- si una versión del rango falla en CI y no se puede arreglar, para ahí: fusiona solo hasta
  la última versión en verde (o deja el PR abierto) y explica qué quedó sin hacer.

Los grupos recomendados están en "Grupos de versiones" del ROADMAP.
