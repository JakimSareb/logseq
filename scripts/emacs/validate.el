;;; validate.el --- Check that org files work with org-mode, org-roam and org-ql  -*- lexical-binding: t; -*-

;; Usage (from the repo root):
;;   FIXTURES_DIR=test-fixtures/org emacs --batch -l scripts/emacs/validate.el
;;
;; Exits with status 1 if any check fails.  Run by
;; .github/workflows/emacs-validate.yml on GitHub Actions.

(require 'cl-lib)
(require 'package)

(setq package-user-dir (expand-file-name
                        (or (getenv "ELPA_DIR") "~/.cache/logseq-eli-elpa"))
      package-check-signature nil
      package-archives '(("gnu"   . "https://elpa.gnu.org/packages/")
                         ("melpa" . "https://melpa.org/packages/")))
(package-initialize)

(defconst eli-packages '(org-roam org-ql))

(unless (cl-every #'package-installed-p eli-packages)
  (package-refresh-contents)
  (dolist (p eli-packages)
    (unless (package-installed-p p)
      (package-install p))))

(require 'org)
(require 'org-element)
(require 'org-lint)
(require 'org-roam)
(require 'org-ql)

(defvar eli-failures 0)

(defun eli-fail (fmt &rest args)
  (setq eli-failures (1+ eli-failures))
  (message "FAIL: %s" (apply #'format fmt args)))

(defun eli-ok (fmt &rest args)
  (message "ok:   %s" (apply #'format fmt args)))

(defconst eli-dir
  (file-truename (expand-file-name (or (getenv "FIXTURES_DIR") "test-fixtures/org"))))

(message "Emacs %s, Org %s, fixtures: %s" emacs-version org-version eli-dir)

;; 0. Environment
(if (and (fboundp 'sqlite-available-p) (sqlite-available-p))
    (eli-ok "Emacs has built-in SQLite support")
  (eli-fail "this Emacs has no built-in SQLite support (needed by org-roam)"))

(defconst eli-files (directory-files-recursively eli-dir "\\.org\\'"))

(if eli-files
    (eli-ok "found %d .org files" (length eli-files))
  (eli-fail "no .org files found in %s" eli-dir))

;; 1. Every file parses and is lint-clean
;; Register every ID first, like org-roam does in a real setup, so that
;; org-lint can resolve [[id:...]] links between files.
(require 'org-id)
(setq org-id-locations-file (make-temp-file "org-id-locations"))
(org-id-update-id-locations eli-files)

(dolist (f eli-files)
  (with-current-buffer (find-file-noselect f)
    (org-mode)
    (condition-case err
        (progn (org-element-parse-buffer) (eli-ok "%s parses" (file-name-nondirectory f)))
      (error (eli-fail "%s does not parse: %S" f err)))
    (let ((reports (org-lint)))
      (if reports
          (dolist (r reports) (eli-fail "org-lint %s: %S" (file-name-nondirectory f) r))
        (eli-ok "%s is org-lint clean" (file-name-nondirectory f))))))

;; 2. org-roam builds its database and no id link is dangling
(setq org-roam-directory eli-dir
      org-roam-db-location (make-temp-file "org-roam" nil ".db")
      org-roam-db-update-on-save nil)
(condition-case err
    (progn
      (org-roam-db-sync t)
      (let* ((file-nodes (org-roam-db-query [:select [id file] :from nodes :where (= level 0)]))
             (all-ids    (mapcar #'car (org-roam-db-query [:select [id] :from nodes])))
             (links      (org-roam-db-query [:select [source dest type] :from links]))
             (id-links   (cl-remove-if-not (lambda (l) (equal (nth 2 l) "id")) links)))
        (if (= (length file-nodes) (length eli-files))
            (eli-ok "org-roam: one file-level node per file (%d)" (length file-nodes))
          (eli-fail "org-roam: %d file-level nodes for %d files"
                    (length file-nodes) (length eli-files)))
        (eli-ok "org-roam: %d nodes, %d id links" (length all-ids) (length id-links))
        (dolist (l id-links)
          (unless (member (nth 1 l) all-ids)
            (eli-fail "org-roam: dangling id link %s (from %s)" (nth 1 l) (nth 0 l))))))
  (error (eli-fail "org-roam failed: %S" err)))

;; 3. org-ql can query TODO + SCHEDULED items (org-agenda style data)
(condition-case err
    (let ((n (length (org-ql-select eli-files '(and (todo) (scheduled))))))
      (if (>= n 1)
          (eli-ok "org-ql: %d scheduled TODO item(s)" n)
        (eli-fail "org-ql found no scheduled TODO items")))
  (error (eli-fail "org-ql failed: %S" err)))

(if (> eli-failures 0)
    (progn (message "%d check(s) failed" eli-failures) (kill-emacs 1))
  (message "All checks passed"))
