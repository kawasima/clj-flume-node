(defproject net.unit8/clj-flume-node "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.flume/flume-ng-node "1.4.0"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :main flume-node.core)


