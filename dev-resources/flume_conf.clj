(defagent :a1
  (source :r1
          :type "syslogudp"
          :host "127.0.0.1"
          :port 4444
          :channels :c1)
  (sink :k1
        :type "logger"
        :channel :c1)
;  (sink :k2
;        :type "example.MailSink"
;        :channel :c1)

  (channel :c1
           :type "memory"
           :capacity 1000
           :transactionCapacity 100))



