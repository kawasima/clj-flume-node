(ns flume-node.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [org.apache.flume.conf FlumeConfiguration]
           [org.apache.flume.lifecycle LifecycleSupervisor LifecycleState
            LifecycleSupervisor$SupervisorPolicy$AlwaysRestartPolicy]
           [org.apache.flume.node Application]))

(declare ^:dynamic *config*)
(defmacro source [n & components]
  `[:sources ~n (hash-map ~@components)])

(defmacro sink [n & components]
  `[:sinks ~n (hash-map ~@components)])

(defmacro channel [n & components]
  `[:channels ~n (hash-map ~@components)])


(defmacro defagent [agt & body]
  `(def *config* {~agt
                    (reduce (fn [ks# [c# n# attrs#]]
                              (assoc-in ks# [c# n#] attrs#))
                            {} [~@body])}))
(defn convert-config-map!
  ([config-map form ks]
   (cond
    (map? form) (doall (map #(convert-config-map! config-map (second %) (conj ks (first %))) form))
    :else (.put config-map
                (clojure.string/join "." (map name ks))
                (cond (keyword? form) (name form)
                      :else (str form)))))
  ([config-map form] (convert-config-map! config-map form [])))

(defn component-names [config-map component-type]
  (let [agent-key (first (keys config-map))]
    (->> (get-in config-map [agent-key component-type])
         keys
         (map name)
         string/join)))

(defn clj-map-configuration-provider [agent-name config-map]
  (proxy [org.apache.flume.node.AbstractConfigurationProvider] [agent-name]
    (getFlumeConfiguration
     []
     (let [config-java (java.util.HashMap.)]
       (convert-config-map! config-java config-map)
       (doto config-java
         (.put (str agent-name ".sources")
               (component-names config-map :sources))

         (.put (str agent-name ".sinks")
               (component-names config-map :sinks))

         (.put (str agent-name ".channels")
               (component-names config-map :channels)))
       (FlumeConfiguration. config-java)))))


(defn make-app [config]
  (let [agent-name (name (first (keys config)))
        supervisor (LifecycleSupervisor.)
        configuration-provider (clj-map-configuration-provider agent-name config)
        configuration (.getConfiguration configuration-provider)
        application (Application.)]
    (. application handleConfigurationEvent configuration)
    (println (-> configuration-provider
                 .getFlumeConfiguration
                 (.getConfigurationFor agent-name)))
    (. application start)
    application))



(defn -main []
  (binding [*ns* (find-ns 'flume-node.core)]
    (-> "flume_conf.clj"
      io/resource
      io/as-file
      .getPath
      load-file))
  (let [application (make-app *config*)]))
