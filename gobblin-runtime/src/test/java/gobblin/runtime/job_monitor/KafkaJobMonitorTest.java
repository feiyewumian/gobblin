/*
 * Copyright (C) 2014-2016 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 */

package gobblin.runtime.job_monitor;

import java.net.URI;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.typesafe.config.ConfigFactory;


public class KafkaJobMonitorTest {

  @Test
  public void test() throws Exception {

    MockedKafkaJobMonitor monitor = MockedKafkaJobMonitor.create(ConfigFactory.empty());
    monitor.startAsync();

    monitor.getMockKafkaStream().pushToStream("job1:1");
    monitor.awaitExactlyNSpecs(1);
    Assert.assertTrue(monitor.getJobSpecs().containsKey(new URI("job1")));
    Assert.assertEquals(monitor.getJobSpecs().get(new URI("job1")).getVersion(), "1");

    monitor.getMockKafkaStream().pushToStream("job2:1");
    monitor.awaitExactlyNSpecs(2);
    Assert.assertTrue(monitor.getJobSpecs().containsKey(new URI("job2")));
    Assert.assertEquals(monitor.getJobSpecs().get(new URI("job2")).getVersion(), "1");

    monitor.getMockKafkaStream().pushToStream(MockedKafkaJobMonitor.REMOVE + ":job1");
    monitor.awaitExactlyNSpecs(1);
    Assert.assertFalse(monitor.getJobSpecs().containsKey(new URI("job1")));
    Assert.assertTrue(monitor.getJobSpecs().containsKey(new URI("job2")));

    monitor.getMockKafkaStream().pushToStream("job1:2,job2:2");
    monitor.awaitExactlyNSpecs(2);
    Assert.assertTrue(monitor.getJobSpecs().containsKey(new URI("job1")));
    Assert.assertEquals(monitor.getJobSpecs().get(new URI("job1")).getVersion(), "2");
    Assert.assertTrue(monitor.getJobSpecs().containsKey(new URI("job2")));
    Assert.assertEquals(monitor.getJobSpecs().get(new URI("job2")).getVersion(), "2");

    monitor.shutDown();
  }
}
