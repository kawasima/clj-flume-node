clj-flume-node
===============

This is the flume node application launched by clojure.
[Apache Flume](http://flume.apache.org) is a servie for collecting events and logs.
It's a grate tool, but you have to write the configuration file by Java properties format. :sob

Using by clj-flume-node, you can write the flume configuration file by S-expression. It's so wonderful!

## Getting Started

Call `make-application` to start flume node.

    (make-application)

## Examples

### Configure agent

    (defagent :a1
      (source :r1
              :type "syslogudp"
              :host "127.0.0.1"
              :port 4444
              :channels :c1)
      (sink :k1
            :type "logger"
            :channel :c1)

      (channel :c1
               :type "memory"
               :capacity 1000
               :transactionCapacity 100))

## Costom sink and source

`defsink` is a macro to define a customized Sink.

    (defsink your-own-sink
      :process (fn [event]
                 (println (String. (.getBody event)))))

`defsource` is a macro to define a customized (Pollable)Source. `:process` function must return Event object.

    (defsink your-own-source
      :process (fn []
                 (EventBuilder/withBody (.getBytes msg))))

    (defagent
      (source :r1
              :type "examples/your-own-source"
              :channels :c1)
      (sink :k1
            :type "examples/your-own-sink"
            :channel :c1))

## License

Copyright © 2014 kawasima
Distributed under the Eclipse Public License,, the same as Clojure.


