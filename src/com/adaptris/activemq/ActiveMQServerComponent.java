package com.adaptris.activemq;

import java.net.URI;
import java.util.Properties;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.management.ManagementComponent;

public class ActiveMQServerComponent implements ManagementComponent {
  
  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private static final String ACTIVEMQ_BROKER_CONFIG_FILE_NAME_KEY = "activemq.config.filename";
  
  private ClassLoader classLoader;
  
  private Properties properties;
  
  private BrokerService broker;
  
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
          
          String brokerConfig = properties.getProperty(ACTIVEMQ_BROKER_CONFIG_FILE_NAME_KEY);
          if(brokerConfig != null) {
            if(!brokerConfig.startsWith("xbean:"))
              brokerConfig = "xbean:" + brokerConfig;
            
            broker = BrokerFactory.createBroker(new URI(brokerConfig));
            broker.start();
          } else
            log.warn("No ActiveMQ config file configured, not starting ActiveMQ.");
          
        } catch (Exception ex) {
          log.error("Could not start the ActiveMQ broker", ex);
        }
      }
    }).start();
    
  }

  @Override
  public void stop() throws Exception {
    this.broker.stop();
  }

  @Override
  public void destroy() throws Exception {
  }

}
