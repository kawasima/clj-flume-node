clj-flume-node
===============

This is the flume node application launched by clojure.
[Apache Flume](http://flume.apache.org) is a servie for collecting events and logs.
It's a grate tool, but you have to write the configuration file by Java properties format. :sob:

Using by clj-flume-node, you can write the flume configuration file by S-expression. It's so wonderful! :laughing:

## Install

Add the following dependency to your project.clj file:

    [net.unit8/clj-flume-node "0.1.0"]

## Getting Started

    (use 'flume-node.core :only '(defagent defsink defsource) :as flume)
    
Call `make-application` to start flume node.

    (flume/make-application)

## Examples

### Configure agent

```clojure
(defagent :a1
  (flume/source :r1
                :type "syslogudp"
                :host "127.0.0.1"
                :port 4444
                :channels :c1)
  (flume/sink :k1
              :type "logger"
              :channel :c1)
  (flume/channel :c1
                 :type "memory"
                 :capacity 1000
                 :transactionCapacity 100))
```

### Custom sink and source

`defsink` is a macro to define a customized Sink.

```clojure
(defsink your-own-sink
  :process (fn [event]
             (println (String. (.getBody event)))))
```

`defsource` is a macro to define a customized (Pollable)Source. `:process` function must return Event object.

```clojure
(defsink your-own-source
  :process (fn []
             (EventBuilder/withBody (.getBytes msg))))
```

Then you can use `[namespace]/[sink name]` and `[namespace]/[source name]` in :type section.

```clojure
(defagent
  (flume/source :r1
                :type "examples/your-own-source"
                :channels :c1)
  (flume/sink :k1
              :type "examples/your-own-sink"
              :channel :c1))
```

### License

Copyright © 2014 kawasima
Distributed under the Eclipse Public License,, the same as Clojure.


