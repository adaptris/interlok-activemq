package com.adaptris.activemq;

import java.net.URI;
import java.util.Properties;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.ManagementComponent;

/**
 * Management component that starts up an embedded ActiveMQ broker.
 * 
 * @author amcgrath.
 *
 */
public class ActiveMQServerComponent implements ManagementComponent {
  
  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * The property key that defines the activemq configuration file.
   * 
   */
  public static final String ACTIVEMQ_BROKER_CONFIG_FILE_NAME_KEY = "activemq.config.filename";
  
  private static final String DEFAULT_ACTIVEMQ_CONFIG = "xbean:default-activemq.xml"; // found in the jar file.
  
  private transient ClassLoader classLoader;
  
  private transient Properties properties;
  
  private transient BrokerService broker;
  
  @Override
  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void init(final Properties properties) throws Exception {
    this.properties = properties;
  }

  @Override
  public void start() throws Exception {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          log.debug("Creating ActiveMQ Broker");
          Thread.currentThread().setContextClassLoader(classLoader);
          
          String brokerConfig = properties.getProperty(ACTIVEMQ_BROKER_CONFIG_FILE_NAME_KEY, DEFAULT_ACTIVEMQ_CONFIG);
          if (!brokerConfig.startsWith("xbean:")) {
            brokerConfig = "xbean:" + brokerConfig;
          }
          broker = BrokerFactory.createBroker(new URI(brokerConfig));
          broker.start();
          log.debug("Starting {}", broker.getBrokerObjectName());
          broker.waitUntilStarted();
          log.debug("ActiveMQ Broker now running.");
        } catch (Exception ex) {
          log.error("Could not start the ActiveMQ broker", ex);
        }
      }
    }).start();
    
  }

  @Override
  public void stop() throws Exception {
    if (broker != null) {
      broker.stop();
      broker.waitUntilStopped();
      broker = null;
    }
    log.debug(this.getClass().getSimpleName() + " Stopped");
  }

  @Override
  public void destroy() throws Exception {
    log.debug(this.getClass().getSimpleName() + " Destroyed");
  }

  void waitForStart(long timeout) throws InterruptedException {
    long totalWaitTime = 0;
    while (!brokerStarted() && totalWaitTime < timeout) {
      Thread.sleep(100);
      totalWaitTime += timeout;
    }
  }

  private boolean brokerStarted() {
    return (broker != null) ? broker.isStarted() : false;
  }
}
