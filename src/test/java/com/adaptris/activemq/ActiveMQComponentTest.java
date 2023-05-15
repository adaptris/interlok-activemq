package com.adaptris.activemq;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;

import com.adaptris.interlok.junit.scaffolding.BaseCase;

public class ActiveMQComponentTest {

  private static final Properties TEST_PROPERTIES;
  private static final String PROPERTIES_RESOURCE = "unit-tests.properties";
  private static final String TEST_ACTIVEMQ_CONFIG = "activemq.test.configuration";

/*
  private static final String OPENWIRE = "tcp://127.0.0.1:61616";
  private static final String AMQP = "tcp://127.0.0.1:5672";
  private static final String STOMP = "tcp://127.0.0.1:61613";
  private static final String MQTT = "tcp://127.0.0.1:1883";

  private static final String[] DEFAULT_PROTOCOLS =
    {
        OPENWIRE, AMQP, STOMP, MQTT
    };
*/

  static {
    TEST_PROPERTIES = new Properties();

    InputStream in = BaseCase.class.getClassLoader().getResourceAsStream(PROPERTIES_RESOURCE);

    if (in == null) {
      throw new RuntimeException("cannot locate resource [" + PROPERTIES_RESOURCE + "] on classpath");
    }

    try {
      TEST_PROPERTIES.load(in);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static boolean skipTests() {
    return BooleanUtils.toBoolean(TEST_PROPERTIES.getProperty("activemq.skip.tests", "false"));
  }

  @Test
  public void testDefaultStart() throws Exception {
    assumeFalse(skipTests());
    ActiveMQServerComponent comp = new ActiveMQServerComponent();
    comp.init(new Properties());
    comp.setClassLoader(Thread.currentThread().getContextClassLoader());
    try {
      comp.start();
      comp.waitForStart(60000);
      System.err.println(comp.brokerName() + " started");
      // for (String p : DEFAULT_PROTOCOLS) {
      // assertTrue("Checking " + p, verify(p));
      // }
    } catch (InterruptedException | TimeoutException e) {
      System.err.println("Failed to start");
    } finally {
      comp.stop();
    }
  }

  @Test
  public void testStart_ConfiguredActiveMQ_XML() throws Exception {
    assumeFalse(skipTests());
    ActiveMQServerComponent comp = new ActiveMQServerComponent();
    comp.init(createBootProperties());
    try {
      comp.start();
      comp.waitForStart(60000);
      System.err.println(comp.brokerName() + " started");
      // assertTrue(verify("tcp://localhost:61617"), "tcp://localhost:61617");
    } catch (InterruptedException | TimeoutException e) {
      System.err.println("Failed to start");
    } finally {
      comp.stop();
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

}
