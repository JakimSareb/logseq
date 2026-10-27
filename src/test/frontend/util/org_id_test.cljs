(ns frontend.util.org-id-test
  (:require [cljs.test :refer [are deftest is testing]]
            [frontend.util.org-id :as org-id]))

(def id "72289d9a-eb2f-427b-ad97-b605a4b8c59b")

(deftest page-header
  (testing "drawer first, keywords after it"
    (is (= (str ":PROPERTIES:\n:ID:       " id "\n:END:\n#+title: Mi página")
           (org-id/page-header id "#+title: Mi página")))
    (is (= (str ":PROPERTIES:\n:ID:       " id "\n:END:")
           (org-id/page-header id nil)
           (org-id/page-header id "")))))

(deftest add-id-to-header
  (testing "no drawer yet: a new one goes first"
    (are [content expected] (= expected (org-id/add-id-to-header content id))
      "#+title: A"
      (str ":PROPERTIES:\n:ID:       " id "\n:END:\n#+title: A")

      ""
      (str ":PROPERTIES:\n:ID:       " id "\n:END:")

      nil
      (str ":PROPERTIES:\n:ID:       " id "\n:END:")))
  (testing "an existing drawer is reused"
    (is (= (str ":PROPERTIES:\n:ID:       " id "\n:ROAM_ALIASES: x\n:END:\n#+title: A")
           (org-id/add-id-to-header ":PROPERTIES:\n:ROAM_ALIASES: x\n:END:\n#+title: A" id)))))

(deftest id-link
  (is (= (str "[[id:" id "][Mi página]]")
         (org-id/id-link id "Mi página")))
  (testing "brackets and line breaks cannot break the link"
    (is (= (str "[[id:" id "][a (b) c]]")
           (org-id/id-link id "a [b]\nc")))))
