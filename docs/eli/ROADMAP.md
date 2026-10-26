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
- Cada cambio se hace en una rama `eli/...` y entra por PR a `mi-rama-0.10.15`.

## Fases

| Fase | Contenido | Estado |
|------|-----------|--------|
| A1 | CI en la rama del fork | hecho (pendiente de verificar en GitHub) |
| A2 | Actualizador apuntando al fork (no al Logseq original) | hecho |
| A3 | Validación con Emacs + org-roam + org-ql en CI | en curso |
| A3b | Tests de ida y vuelta (abrir, guardar, sin cambios) | pendiente |
| B4 | Org como formato por defecto | pendiente |
| B5 | Propiedades nativas (`#+title`, `#+filetags`, `:ID:`) | pendiente |
| B6 | Enlaces por ID `[[id:UUID][Título]]` | pendiente |
| B7 | Conversor en Ajustes | pendiente |
| B8 | TODO, SCHEDULED, DEADLINE, etiquetas de cabecera | pendiente |
| C9 | Diarios en hoja anual | pendiente (requiere diseño) |
| D10 | Carpeta local / Dropbox (ficheros "conflicted copy") | pendiente |
| E11-13 | org-roam-ui/dailies/journal, org-transclusion, pdf-tools + org-noter | pendiente |

## Decisiones pendientes

- Esquema de versiones del fork. El actualizador y `frontend.version` esperan semver
  estricto `X.Y.Z`; un sufijo como `-eli.1` rompería la comprobación.
- Formato exacto de la hoja anual (se confirma al empezar la fase C).
