clj-flume-node
===============

This is the flume node application launched by clojure.
You can write the flume configuration file by S-expression. It's so wonderful!

## Getting Started

    % lein run

## Example of the configuration

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



