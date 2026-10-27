(ns frontend.org-roundtrip-test
  "A3b: round-trip tests for org files.

  Every .org file in test-fixtures/org is loaded into a test graph like Logseq
  does when it opens a file, then serialized back with the same code that saves
  a page (frontend.modules.file.core/tree->file-content), without editing
  anything. The result must be identical to the original file.

  Accepted normalizations (see docs/eli/ROADMAP.md, section Hallazgos de A3b):
  - trailing newlines at the end of the file;
  - blank lines right before a headline (`* ...`): Logseq trims every block
    and joins blocks with a single newline, so those lines are dropped.
  Any other difference, including blank lines inside a block, is a failure.

  Run on its own with:
    node static/tests.js -n frontend.org-roundtrip-test"
  (:require [cljs.test :refer [deftest is testing use-fixtures]]
            [clojure.string :as string]
            [datascript.core :as d]
            [frontend.db :as db]
            [frontend.db.conn :as conn]
            [frontend.modules.file.core :as file-core]
            [frontend.modules.outliner.tree :as tree]
            [frontend.config :as config]
            [frontend.state :as state]
            [frontend.test.helper :as test-helper]
            ["fs" :as fs]
            ["path" :as node-path]))

(use-fixtures :each test-helper/start-and-destroy-db)

(def ^:private fixtures-dir "test-fixtures/org")

(defn- normalize
  [s]
  (-> s
      (string/replace #"\n+$" "")
      (string/replace #"\n\n+(?=\*+ )" "\n")))

(defn- read-fixtures
  "Reads every .org file of the fixtures dir as a file map, like the ones the
  graph parser receives when a graph is opened."
  []
  (->> (array-seq (fs/readdirSync fixtures-dir))
       (filter #(string/ends-with? % ".org"))
       sort
       (mapv (fn [file-name]
               {:file/path (str "pages/" file-name)
                :file/content (str (fs/readFileSync (node-path/join fixtures-dir file-name)))}))))

(defn- page-name-of-file
  [path]
  (ffirst (d/q '[:find ?pn
                 :in $ ?path
                 :where
                 [?p :block/file ?f]
                 [?p :block/name ?pn]
                 [?f :file/path ?path]]
               (conn/get-db test-helper/test-db)
               path)))

(defn- save-file-content
  "Content Logseq would write to `path` if the page were saved without changes."
  [path]
  (let [repo test-helper/test-db]
    (when-let [page-name (page-name-of-file path)]
      (let [blocks (db/get-page-blocks-no-cache repo page-name)
            vec-tree (tree/blocks->vec-tree repo blocks page-name)]
        (file-core/tree->file-content vec-tree {:init-level 1})))))

(deftest fixtures-are-found
  (is (seq (read-fixtures)) (str "no .org files in " fixtures-dir)))

(deftest open-and-save-without-changes
  (let [files (read-fixtures)]
    (test-helper/load-test-files files)
    (doseq [{:file/keys [path content]} files]
      (testing path
        (let [saved (save-file-content path)]
          (is (some? saved) (str "no page was created from " path))
          (is (= (normalize content)
                 (some-> saved normalize))))))))

(deftest save-is-stable-after-reopening
  (testing "opening the saved content and saving it again gives the same text"
    (let [files (read-fixtures)
          _ (test-helper/load-test-files files)
          first-pass (mapv (fn [{:file/keys [path]}]
                             {:file/path path
                              :file/content (or (save-file-content path) "")})
                           files)
          _ (test-helper/load-test-files first-pass)]
      (doseq [{:file/keys [path content]} first-pass]
        (testing path
          (is (= (normalize content)
                 (some-> (save-file-content path) normalize))))))))

;; B4 / 1.01: org is the default format

(deftest org-is-the-default-format
  (let [previous (get-in @state/state [:me :preferred_format])]
    (try
      ;; the test runner pins markdown for the older tests: remove it to see the real default
      (swap! state/state update :me dissoc :preferred_format)
      (testing "no graph config and no user setting"
        (is (= :org (state/get-preferred-format)))
        (is (= "org" (config/get-file-extension (state/get-preferred-format)))))
      (testing "an explicit setting still wins"
        (state/set-preferred-format! :markdown)
        (is (= :markdown (state/get-preferred-format))))
      (finally
        (if previous
          (state/set-preferred-format! previous)
          (swap! state/state update :me dissoc :preferred_format))))))

(deftest new-graphs-get-an-org-config
  (let [template (str (fs/readFileSync "src/resources/templates/config.edn"))]
    (is (re-find #"(?m)^ :preferred-format \"Org\"" template)
        "the default config.edn must set :preferred-format \"Org\"")))
