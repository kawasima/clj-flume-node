(defproject net.unit8/clj-flume-node "0.1.0"
  :description "A clojure library for Apache Flume."
  :url "https://github.com/kawasima/clj-flume-node.git"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.apache.flume/flume-ng-node "1.4.0"]]
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :main flume-node.core)
