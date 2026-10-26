# Logseq para Eli: hoja de ruta

Fork de Logseq 0.10.15 (grafo de ficheros). Rama de trabajo: `mi-rama-0.10.15`.

## Objetivo

- Trabajar con ficheros locales (carpeta de grafo), incluida una carpeta dentro de Dropbox.
- Todo en formato org-mode nativo de Emacs.
- Diarios en una hoja anual en lugar de un fichero por día.
- Compatible con org-mode, org-roam, org-roam-ui, org-roam-dailies / org-journal,
  org-agenda, org-transclusion, org-ql, pdf-tools + org-noter.
- Ajuste para convertir notas `.md` / `.org` antiguas al formato nativo.
- Páginas `[[pagina]]` y etiquetas `#etiqueta` pasan a enlaces `[[id:UUID][Título]]`.

## Reglas de trabajo

- Las pruebas corren en GitHub Actions (en el entorno de trabajo no hay Clojure).
- No se compila ni se publica ningún instalador hasta que Eli lo pida.
- La web se publica sola en GitHub Pages al fusionar un PR (`publish-web.yml`).
  Requiere activar una vez Settings > Pages > Source: GitHub Actions.
- Cada cambio se hace en una rama `eli/...` y entra por PR a `mi-rama-0.10.15`.

## Fases y versiones

Cada fase se publica con su versión. Se dice "Adelante 1.01" y se hace la fila de esa
versión (ver "Cómo se trabaja con versiones" más abajo).

| Versión | Fase | Contenido | Estado |
|---------|------|-----------|--------|
| 1.0 | A1 | CI en la rama del fork | hecho y verificado |
| 1.0 | A2 | Actualizador apuntando al fork (no al Logseq original) | hecho |
| 1.0 | A3 | Validación con Emacs + org-roam + org-ql en CI | hecho y verificado |
| 1.0 | A3b | Tests de ida y vuelta (abrir, guardar, sin cambios) | hecho y verificado (PR #4) |
| 1.0 | A4 | Publicación automática de la web en GitHub Pages | despliegue manual correcto el 26-oct; falta confirmar el automático al fusionar |
| 1.01 | B4 | Org como formato por defecto (y ampliar fixtures: subniveles, DEADLINE, etiquetas de cabecera) | pendiente |
| 1.02 | B5 | Propiedades nativas (`#+title`, `#+filetags`, `:ID:`) | pendiente |
| 1.03 | B6 | Enlaces por ID `[[id:UUID][Título]]` | pendiente |
| 1.04 | B7 | Conversor en Ajustes | pendiente |
| 1.05 | B8 | TODO, SCHEDULED, DEADLINE, etiquetas de cabecera | pendiente |
| 1.06 | C9 | Diarios en hoja anual compatible con org-journal | pendiente (formato decidido, ver abajo) |
| 1.07 | D10 | Carpeta local / Dropbox (ficheros "conflicted copy") | pendiente |
| 1.08 | E11 | org-roam-ui, org-roam-dailies / org-journal, org-agenda | pendiente |
| 1.09 | E12 | org-transclusion | pendiente |
| 1.10 | E13 | pdf-tools + org-noter | pendiente |

## Cómo se trabaja con versiones

- Esquema de versiones (decisión de Eli): empieza en 1.0 y sube con dos decimales:
  1.01, 1.02, ... 1.10, 1.11, etc.
- El actualizador (`semver`) exige `X.Y.Z` estricto, así que el número "de Eli" se traduce
  al técnico así: `1.0` -> `1.0.0`, `1.01` -> `1.0.1`, `1.10` -> `1.0.10`, `1.25` -> `1.0.25`.
  Valen hasta 1.99; después se decidirá.
- La versión técnica vive en dos sitios que se cambian juntos: `resources/package.json`
  (`version`) y `src/main/frontend/version.cljs` (`version`).
- Cada fase termina con su versión subida en el mismo PR, con el ROADMAP actualizado
  (estado y, si hay, hallazgos) y el PR fusionado cuando los checks estén en verde.
- Subir la versión no compila ni publica instaladores; eso solo se hace si Eli lo pide.

## Formato de la hoja anual (decisión de Eli)

- Debe ser compatible con org-journal en modo anual (`org-journal-file-type` = `yearly`):
  un único `.org` por año con los días dentro, en lugar de un fichero por día.
- El detalle exacto (nombre del fichero, cabecera del año, formato del título de cada día
  y de las entradas con hora) se fija al empezar 1.06 comprobándolo con org-journal
  instalado en el CI de Emacs (`scripts/emacs/validate.el`), no de memoria.

## Estado conocido del CI

- `e2e-test` falla en el paso "Ensure static yarn.lock is up to date" también en la
  rama original sin cambios (comprobado con el PR de referencia #2). No lo causa ningún
  cambio del fork; hay que regenerar `static/yarn.lock` antes de poder fiarse de ese check.
- Los logs de Actions no se pueden leer desde la sesión de trabajo: los workflows propios
  publican los errores como comentario en el PR.
- El corrector `typos` trata todo como inglés; los ficheros en español están excluidos en `typos.toml`.

## Hallazgos de A3b

- Abrir y guardar sin editar conserva propiedades de página (`:PROPERTIES:`, `#+title`,
  `#+filetags`), enlaces `[[id:...]]`, `TODO`, `SCHEDULED` y el cajón `:ID:` de bloque.
- Se pierde la línea en blanco entre dos bloques de nivel 1 (`...página.\n\n* Bloque` pasa a
  `...página.\n* Bloque`): `tree->file-content` recorta el contenido de cada bloque y los une
  con un solo `\n`. No cambia el significado en org, pero el primer guardado de cualquier
  página escrita a mano en Emacs genera diferencias de espacios en blanco.
- Decisión de Eli (26-oct-2026): se acepta esa normalización. El test ignora solo las líneas
  en blanco justo antes de una cabecera `* ...` y los saltos de línea finales; cualquier otra
  diferencia (p. ej. líneas en blanco dentro de un bloque) sigue siendo un fallo.
- Guardar es estable: lo guardado, al reabrirlo y guardarlo otra vez, no cambia más.

## Decisiones pendientes

Ninguna por ahora.
