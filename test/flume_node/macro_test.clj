(ns flume-node.macro-test
  (:use [flume-node.core :as flume :only [defsink defagent]]))

(defsink storm-sink
  :process (fn [event] (println event)))

(defagent :a1
  (flume/source :r1
          :type "http"
          :port 4444
          :channels :c1)
  (flume/sink :k1
        :type "net.unit8.flume_node.ClojureSink"
        :nsname "flume-node.macro-test/storm-sink"
        :channel :c1)
  (flume/channel :c1
           :type "memory"
           :capacity 1000
           :transactionCapacity 100))

(defn -main []
  (let [application (flume/make-app)]))

