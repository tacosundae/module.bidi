(ns duct.router.ataraxy
  (:require [ataraxy.core :as ataraxy]
            [integrant.core :as ig]))

(derive :duct.router/ataraxy :duct/router)

(defmethod ig/init-key :duct.router/ataraxy [_ {:keys [routes endpoints]}]
  (ataraxy/handler routes endpoints))
