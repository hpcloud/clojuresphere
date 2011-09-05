(ns clojuresphere.core
  (:use [clojuresphere.util :only [memory-stats]]
        [compojure.core :only [defroutes GET POST ANY]]
        [hiccup.middleware :only [wrap-base-url]]
        [ring.util.response :only [response]]
        [ring.middleware.params :only [wrap-params]]
        [ring.adapter.jetty :only [run-jetty]])
  (:require [clojuresphere.layout :as layout]
            [clojuresphere.project-model :as project]
            [compojure.route :as route]))

;; TODO: search
(defroutes routes
  (GET "/_stats" {{gc "gc"} :params}
       (prn-str (merge {:projects (count project/graph)
                        :memory (memory-stats :gc gc)})))
  (GET "/" [] (layout/welcome))
  (GET ["/:pid" :pid #"[a-zA-Z0-9\-\.\_]+"] [pid] (layout/project-detail pid))
  (GET ["/:aid/:gid/:ver"
        :aid #"[a-zA-Z0-9\-\.\_]+"
        :gid #"[a-zA-Z0-9\-\.\_]+"
        :ver #"[a-zA-Z0-9\-\.\_]+"]
       [gid aid ver]
        (layout/project-version-detail gid aid ver))
  (route/resources "/")
  (route/not-found (layout/not-found)))

(def app (-> #'routes
             wrap-base-url
             wrap-params))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty app {:port port :join? false})))

;(run-jetty #'app {:port 8080 :join? false})
