(ns fm.anomaly
  (:require
   [clojure.spec.alpha :as s]
   [fm.lib :as lib]))


   ;;;
   ;;; NOTE: multimethods, hierarchies
   ;;;


(def indicator-hierarchy-atom
  (atom
   (make-hierarchy)))


   ;;;
   ;;; NOTE: predicates, specs
   ;;;


(defn contains-indicator? [m]
  (boolean
   (lib/geta @indicator-hierarchy-atom m ::ident)))

(s/def :fm/anomaly
  (s/and
   map?
   contains-indicator?))

(def anomaly?
  (partial s/valid? :fm/anomaly))

(def deep-anomaly
  (partial lib/deep-some anomaly?))

(def deep-anomaly?
  (comp boolean deep-anomaly))

(s/def :fm/deep-anomaly
  (s/and
   coll?
   deep-anomaly?))

(s/def :fm/anomalous
  (s/or
   :fm/anomaly :fm/anomaly
   :fm/deep-anomaly :fm/deep-anomaly))

(def anomalous?
  (partial s/valid? :fm/anomalous))


   ;;;
   ;;; NOTE: anomaly helpers
   ;;;


(defn geta [m k]
  (lib/geta @indicator-hierarchy-atom m k)) ; ALT: partial apply

(defn geta-in [m path]
  (lib/geta-in @indicator-hierarchy-atom m path))

(defn finda [m k]
  (lib/finda @indicator-hierarchy-atom m k))


   ;;;
   ;;; NOTE: anomaly api
   ;;;


(defn unwrap [anomaly]
  (loop [a anomaly]
    (if-let [v (get-in a [::s/explain-data ::s/value])]
      (if-let [nested (deep-anomaly (lib/ensure-sequential v))]
        (recur nested)
        a)
      a)))
