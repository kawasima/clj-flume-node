package net.unit8.flume_node;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Sink;
import org.apache.flume.sink.AbstractSink;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import clojure.lang.RT;
import clojure.lang.Keyword;
import clojure.lang.Var;
import clojure.lang.IFn;

import com.google.common.base.Preconditions;

/**
 * This sink can execute a custom process written by clojure.
 */
public class ClojureSink extends AbstractSink implements Configurable {
  private static final Logger logger = LoggerFactory.getLogger(ClojureSink.class);
  private String namespace;
  private String sinkName;

  private CounterGroup counterGroup;
  private Var configureFunc;
  private IFn startFunc;
  private IFn stopFunc;
  private IFn processFunc;

  public ClojureSink() {
    counterGroup = new CounterGroup();
  }

  @Override
  public void configure(Context context) {
    String nsname = context.getString("nsname");
    Preconditions.checkState(nsname != null, "No nsname specified");
    String[] tokens = nsname.split("/", 2);
    Preconditions.checkState(tokens.length == 2, "nsname must be [namespace]/[sink-name]");

    Var sinkVar = RT.var(tokens[0], tokens[1]);
    Preconditions.checkState(sinkVar != null, tokens[0] + "/" + tokens[1] + " is not defined");
    Object sink = sinkVar.get();

    Var getFunc = RT.var("clojure.core", "get");
    startFunc   = (IFn) getFunc.invoke(sink, Keyword.intern("start"));
    stopFunc    = (IFn) getFunc.invoke(sink, Keyword.intern("stop"));
    processFunc = (IFn) getFunc.invoke(sink, Keyword.intern("process"));
    Preconditions.checkState(processFunc != null, "No :process function");
  }

  @Override
  public void start() {
    if (startFunc != null)
      startFunc.invoke();
  }

  @Override
  public void stop() {
    if (stopFunc != null)
      stopFunc.invoke();
  }

  @Override
  public Status process() throws EventDeliveryException {
    Status result = Status.READY;
    Channel channel = getChannel();
    Transaction transaction = channel.getTransaction();
    Event event = null;

    try {
      transaction.begin();
      event = channel.take();

      if (event != null) {
        processFunc.invoke(event);
      } else {
        result = Status.BACKOFF;
      }
      transaction.commit();
    } catch (Exception ex) {
      transaction.rollback();
      logger.error("Failed to deliver event. Exception follows.", ex);
      throw new EventDeliveryException("Failed to deliver event: " + event, ex);
    } finally {
      transaction.close();
    }
    return result;
  }
}