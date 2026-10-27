(ns frontend.test.frontend-node-test-runner
  "This is a custom version of the node-test-runner for the frontend build"
  {:dev/always true} ;; necessary for test-data freshness
  (:require [frontend.state :as state]
            [frontend.test.node-test-runner :as node-test-runner]
            [shadow.test.env :as env]
            [lambdaisland.glogi.console :as glogi-console]
            ;; activate humane test output for all tests
            [pjstadig.humane-test-output]))

;; Needed for new test runners
(defn ^:dev/after-load reset-test-data! []
  (-> (env/get-test-data)
      (env/reset-test-data!)))

(defn main [& args]
  []
  (glogi-console/install!) ;; see log messages
  (reset-test-data!)
  ;; The fork defaults to org (ROADMAP 1.01) but most existing tests were written
  ;; for markdown. Keep them on markdown; tests of the default itself reset it.
  (state/set-preferred-format! :markdown)
  (node-test-runner/parse-and-run-tests args))
