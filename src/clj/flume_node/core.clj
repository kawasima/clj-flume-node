(ns flume-node.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [org.apache.flume.conf FlumeConfiguration]
           [org.apache.flume.lifecycle LifecycleSupervisor LifecycleState
            LifecycleSupervisor$SupervisorPolicy$AlwaysRestartPolicy]
           [org.apache.flume.node Application]))

(def ^:dynamic *config*)
(defmacro source [n & components]
  (let [cs (apply hash-map components)
        source-type (cs :type)]
    (if (.contains source-type "/")
      [:sources n (-> cs
                    (assoc :type "net.unit8.flume_node.ClojureSource")
                    (assoc :nsname source-type))]
      [:sources n cs])))

(defn sink [n & components]
  (let [cs (apply hash-map components)
        sink-type (cs :type)]
    (if (.contains sink-type "/")
      [:sinks n (-> cs
                    (assoc :type "net.unit8.flume_node.ClojureSink")
                    (assoc :nsname sink-type))]
      [:sinks n cs])))

(defn channel [n & components]
  [:channels n (apply hash-map components)])


(defmacro defagent [agt & body]
  `(alter-var-root (var *config*)
                   (constantly
                    {~agt
                     (reduce (fn [ks# [c# n# attrs#]]
                               (assoc-in ks# [c# n#] attrs#))
                             {} [~@body])})))

(defmacro defsink [sink-name & body]
  `(def ~sink-name (hash-map ~@body)))

(defmacro defsource [source-name & body]
  `(def ~source-name (hash-map ~@body)))

(defn convert-config-map!
  ([config-map form ks]
   (cond
    (map? form) (doall (map #(convert-config-map! config-map (second %) (conj ks (first %))) form))
    :else (.put config-map
                (string/join "." (map name ks))
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


(defn make-app
  ([] (make-app *config*))
  ([config]
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
     application)))



(defn -main []
  (binding [*ns* (find-ns 'flume-node.core)]
    (-> "flume_conf.clj"
      io/resource
      io/as-file
      .getPath
      load-file))
  (let [application (make-app)]))

