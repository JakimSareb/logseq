(ns ^:no-doc frontend.util.page-property
  (:require [clojure.string :as string]
            [frontend.db :as db]
            [frontend.modules.outliner.core :as outliner-core]
            [frontend.modules.outliner.file :as outliner-file]
            [frontend.modules.outliner.transaction :as outliner-tx]
            [frontend.state :as state]
            [frontend.util :as util]
            [frontend.util.org-id :as org-id]))

(defn insert-property
  [format content key value]
  (when (and (string? content) (not (string/blank? (name key))))
    (let [key (if (string? key) (keyword key) key)
          key-part (util/format (case format
                                  :org "#+%s: "
                                  "%s:: ") (string/lower-case (name key)))
          new-property-line (str key-part value)
          lines (string/split-lines content)
          key-exists? (atom false)
          lines (doall
                 (map (fn [line]
                        (if (and (string/starts-with?
                                  (string/lower-case line)
                                  (string/lower-case key-part))
                                 (not @key-exists?)) ; only replace the first match
                          (do
                            (reset! key-exists? true)
                            new-property-line)
                          line)) lines))
          lines (if (= lines [""]) nil lines)
          lines (if @key-exists? lines (cons new-property-line lines))]
      (string/join "\n" lines))))

(defn insert-properties
  [format content kvs]
  (reduce
   (fn [content [k v]]
     (let [k (if (string? k)
               (keyword (-> (string/lower-case k)
                            (string/replace " " "-")))
               k)
           v (if (coll? v)
               (some->>
                (seq v)
                (distinct)
                (string/join ", "))
               v)]
       (insert-property format content k v)))
   content kvs))

(defn add-property!
  "Sanitized page-name, unsanitized key / value"
  [page-name key value]
  (when-let [page (db/pull [:block/name (util/page-name-sanity-lc page-name)])]
    (let [repo (state/get-current-repo)
          key (keyword key)
          pre-block (db/get-pre-block repo (:db/id page))
          format (state/get-preferred-format)
          page-id {:db/id (:db/id page)}
          org? (= format :org)
          value (if (contains? #{:filters} key) (pr-str value) value)]
      (if pre-block
        (let [properties (:block/properties pre-block)
              new-properties (assoc properties key value)
              content (:block/content pre-block)
              new-content (insert-property format content key value)
              block {:db/id (:db/id pre-block)
                     :block/properties new-properties
                     :block/content new-content
                     :block/page page-id}
              tx [(assoc page-id :block/properties new-properties)
                  block]]
          ;; (util/pprint tx)
          (db/transact! tx))
        (let [block {:block/uuid (db/new-block-id)
                     :block/left page-id
                     :block/parent page-id
                     :block/page page-id
                     :block/content (if org?
                                      (str "#+" (string/upper-case (name key)) ": " value)
                                      (str (name key) ":: " value))
                     :block/format format
                     :block/properties {key value}
                     :block/pre-block? true}
              page-properties-tx [(assoc page-id :block/properties {key value})]]
          (outliner-tx/transact!
            {:outliner-op :insert-blocks
             :additional-tx page-properties-tx}
            (outliner-core/insert-blocks! block page {:sibling? false}))))
      (outliner-file/sync-to-file page-id))))

(defn ensure-id!
  "Org only. Returns the `:ID:` (uuid string) of a page, adding a file-level
  property drawer to the page header when it has none (ROADMAP 1.02).

  The id is the uuid of the header block: that is what the graph parser assigns
  to the header block when it reads the `:ID:` back from the file, so
  `[[id:UUID][Title]]` links resolve to the page header."
  [page-name]
  (when-let [page (db/pull [:block/name (util/page-name-sanity-lc page-name)])]
    (let [repo (state/get-current-repo)
          page-id {:db/id (:db/id page)}
          pre-block (db/get-pre-block repo (:db/id page))
          existing (some-> pre-block :block/properties :id str string/trim)]
      (cond
        (and existing (util/uuid-string? existing))
        existing

        pre-block
        (let [id (:block/uuid pre-block)
              properties (assoc (:block/properties pre-block) :id (str id))
              block {:db/id (:db/id pre-block)
                     :block/properties properties
                     :block/content (org-id/add-id-to-header (:block/content pre-block) id)
                     :block/page page-id}]
          (db/transact! [(assoc page-id :block/properties properties)
                         block])
          (outliner-file/sync-to-file page-id)
          (str id))

        :else
        (let [id (db/new-block-id)
              properties {:id (str id)}
              block {:block/uuid id
                     :block/left page-id
                     :block/parent page-id
                     :block/page page-id
                     :block/content (org-id/page-header id nil)
                     :block/format :org
                     :block/properties properties
                     :block/pre-block? true}]
          (outliner-tx/transact!
           {:outliner-op :insert-blocks
            :additional-tx [(assoc page-id :block/properties properties)]}
           (outliner-core/insert-blocks! block page {:sibling? false}))
          (outliner-file/sync-to-file page-id)
          (str id))))))
