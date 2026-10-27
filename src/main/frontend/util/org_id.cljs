(ns frontend.util.org-id
  "Pure helpers for the org-mode native page header (`:ID:` property drawer
  followed by `#+title:` ...) and for `[[id:UUID][Title]]` links.

  See docs/eli/ROADMAP.md, versions 1.02 and 1.03."
  (:require [clojure.string :as string]))

(defn id-property-line
  [id]
  (str ":ID:       " id))

(defn page-header
  "File-level header of an org page: a property drawer holding `:ID:` and, after
  it, the optional `#+keyword: value` lines (already formatted).
  org-roam needs the drawer to be the very first thing in the file."
  [id keywords-content]
  (str ":PROPERTIES:\n"
       (id-property-line id) "\n"
       ":END:"
       (when-not (string/blank? keywords-content)
         (str "\n" (string/trim-newline keywords-content)))))

(def ^:private drawer-start-re #"(?i)^:PROPERTIES:[ \t]*\n")

(defn add-id-to-header
  "Returns the header `content` of an org page with `:ID:` set to `id`.
  Reuses the first property drawer when the content starts with one, otherwise
  puts a new drawer in front of the content."
  [content id]
  (let [content (or content "")]
    (if (re-find drawer-start-re content)
      (string/replace-first content drawer-start-re
                            (fn [_] (str ":PROPERTIES:\n" (id-property-line id) "\n")))
      (str ":PROPERTIES:\n"
           (id-property-line id) "\n"
           ":END:"
           (when-not (string/blank? content)
             (str "\n" content))))))

(defn link-label
  "Text that is safe to use as the description of an org link."
  [title]
  (-> (str title)
      (string/replace "[" "(")
      (string/replace "]" ")")
      (string/replace #"\s+" " ")
      string/trim))

(defn id-link
  "`[[id:UUID][Title]]`"
  [id title]
  (str "[[id:" id "][" (link-label title) "]]"))
