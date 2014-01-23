package net.unit8.flume_node;

import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Source;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import clojure.lang.RT;
import clojure.lang.Keyword;
import clojure.lang.Var;
import clojure.lang.IFn;

import com.google.common.base.Preconditions;

public class ClojureSource extends AbstractSource implements PollableSource, Configurable {
  private static final Logger logger = LoggerFactory.getLogger(ClojureSource.class);
  private String namespace;
  private String sourceName;

  private CounterGroup counterGroup;
  private Var configureFunc;
  private IFn startFunc;
  private IFn stopFunc;
  private IFn processFunc;

  public ClojureSource() {
    counterGroup = new CounterGroup();
  }

  @Override
  public void configure(Context context) {
    String nsname = context.getString("nsname");
    Preconditions.checkState(nsname != null, "No nsname specified");
    String[] tokens = nsname.split("/", 2);
    Preconditions.checkState(tokens.length == 2, "nsname must be [namespace]/[source-name]");

    Var sourceVar = RT.var(tokens[0], tokens[1]);
    Preconditions.checkState(sourceVar != null, tokens[0] + "/" + tokens[1] + " is not defined");
    Object source = sourceVar.get();

    Var getFunc = RT.var("clojure.core", "get");
    startFunc   = (IFn) getFunc.invoke(source, Keyword.intern("start"));
    processFunc = (IFn) getFunc.invoke(source, Keyword.intern("process"));
    stopFunc    = (IFn) getFunc.invoke(source, Keyword.intern("stop"));
    Preconditions.checkState(processFunc != null, "No :process function");
  }

  @Override
  public void start() {
    if (startFunc != null)
      startFunc.invoke();
    super.start();
  }

  @Override
  public void stop() {
    if (stopFunc != null)
      stopFunc.invoke();
    super.stop();
  }

  @Override
  public Status process() throws EventDeliveryException {
    try {
      Event event = (Event) processFunc.invoke();
      if (event != null) {
        getChannelProcessor().processEvent(event);
        counterGroup.incrementAndGet("events.successful");
      }
    } catch (Exception ex) {
      counterGroup.incrementAndGet("events.failed");
    }
    return Status.READY;
  }
}