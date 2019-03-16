(ns duct.module.bidi
  (:require [ataraxy.core :as ataraxy]
            [duct.core :as duct]
            [duct.core.merge :as merge]
            [integrant.core :as ig]))

(defn- add-ns-prefix [kw prefix]
  (keyword (str prefix (if-let [ns (namespace kw)] (str "." ns)))
           (name kw)))

(defn- infer-keys [keys prefix]
  (into {} (for [k keys] [k (ig/ref (add-ns-prefix k prefix))])))

(defn- infer-handlers [routes project-ns]
  (infer-keys (ataraxy/result-keys routes) (str project-ns ".handler")))

(defn- middleware-keys [routes]
  (set (mapcat #(mapcat keys (:meta %)) (ataraxy/parse routes))))

(defn- infer-middleware [routes project-ns]
  (infer-keys (middleware-keys routes) (str project-ns ".middleware")))

(defmethod ig/init-key :duct.module/bidi [_ routes]
  (fn [config]
    (let [project-ns (:duct.core/project-ns config)
          routes     (dissoc routes ::duct/requires)
          handlers   (infer-handlers routes project-ns)
          middleware (infer-middleware routes project-ns)]
      (duct/merge-configs
       config
       {:duct.handler/root
        {:router (ig/ref :duct.router/ataraxy)}
        :duct.router/bidi
        {:routes     (with-meta routes {:demote true})
         :handlers   (with-meta handlers {:demote true})
         :middleware (with-meta middleware {:demote true})}}))))
