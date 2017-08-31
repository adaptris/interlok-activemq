package com.adaptris.activemq;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.jms.Connection;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.Test;

import com.adaptris.core.BaseCase;

public class ActiveMQComponentTest {

  private static final Properties TEST_PROPERTIES;
  private static final String PROPERTIES_RESOURCE = "unit-tests.properties";
  private static final String DEFAULT_NAME = "defaultMgmtComponentBroker";
  private static final String TEST_ACTIVEMQ_CONFIG = "activemq.test.configuration";

  private static final String OPENWIRE = "tcp://127.0.0.1:61616";
  private static final String AMQP = "tcp://127.0.0.1:5672";
  private static final String STOMP = "tcp://127.0.0.1:61613";
  private static final String MQTT = "tcp://127.0.0.1:1883";

  private static final String[] DEFAULT_PROTOCOLS =
  {
      OPENWIRE, AMQP, STOMP, MQTT
  };

  static {
    TEST_PROPERTIES = new Properties();

    InputStream in = BaseCase.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE);

    if (in == null) {
      throw new RuntimeException("cannot locate resource [" + PROPERTIES_RESOURCE + "] on classpath");
    }

    try {
      TEST_PROPERTIES.load(in);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static boolean skipTests() {
    return BooleanUtils.toBoolean(TEST_PROPERTIES.getProperty("activemq.skip.tests", "false"));
  }

  @Test
  public void testDefaultStart() throws Exception {
    if (!skipTests()) {
      ActiveMQServerComponent comp = new ActiveMQServerComponent();
      comp.init(new Properties());
      comp.setClassLoader(Thread.currentThread().getContextClassLoader());
      try {
        comp.start();
        comp.waitForStart(60000);
        for (String p : DEFAULT_PROTOCOLS) {
          assertTrue("Checking " + p, verify(p));
        }
      }
      finally {
        comp.stop();
      }
    }
  }

  @Test
  public void testStart_ConfiguredActiveMQ_XML() throws Exception {
    if (!skipTests()) {
      ActiveMQServerComponent comp = new ActiveMQServerComponent();
      comp.init(createBootProperties());
      try {
        comp.start();
        comp.waitForStart(60000);
        assertTrue("tcp://localhost:61617", verify("tcp://localhost:61617"));
      }
      finally {
        comp.stop();
      }
    }
  }

  private Properties createBootProperties() {
    Properties result = new Properties();
    result.setProperty(ActiveMQServerComponent.ACTIVEMQ_BROKER_CONFIG_FILE_NAME_KEY,
        relativize(TEST_PROPERTIES.getProperty(TEST_ACTIVEMQ_CONFIG)));
    return result;
  }

  private static String relativize(String s) {
    if (!isEmpty(s)) {
      return s.replaceAll(Matcher.quoteReplacement(System.getProperty("user.dir")), ".").replaceAll("\\\\", "/");
    }
    return s;
  }

  private boolean verify(String url) {
    boolean ok = false;
    ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    Connection connection = null;
    Session session = null;
    try {
      connection = connectionFactory.createConnection();
      connection.start();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      ok = true;
    }
    catch (Exception e) {

    }
    finally {
      close(session, connection);
    }
    return ok;
  }

  private void close(Session s, Connection c) {
    if (s != null) {
      try {
        c.close();
      }
      catch (Exception e) {
      }
    }
    if (c != null) {
      try {
        c.stop();
      }
      catch (Exception e) {
      }
      try {
        c.close();
      }
      catch (Exception e) {
      }
    }
  }

}
