/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.kafka;

import java.io.Serializable;
import java.util.List;


public class SpoutConfig extends KafkaConfig implements Serializable {
    private static final long serialVersionUID = -1247769246497567352L;
    public List<String> zkServers = null;
    public Integer zkPort = null;
    public String zkRoot = null;
    public String id = null;

    public String outputStreamId;

    // setting for how often to save the current kafka offset to ZooKeeper
    public long stateUpdateIntervalMs = 2000;

    // Retry strategy for failed messages
    public String failedMsgRetryManagerClass = ExponentialBackoffMsgRetryManager.class.getName();

    // Exponential back-off retry settings.  These are used by ExponentialBackoffMsgRetryManager for retrying messages after a bolt
    // calls OutputCollector.fail(). These come into effect only if ExponentialBackoffMsgRetryManager is being used.
    public long retryInitialDelayMs = 0;
    public double retryDelayMultiplier = 1.0;
    public long retryDelayMaxMs = 60 * 1000;
    public int retryLimit = -1;

    public SpoutConfig(BrokerHosts hosts, String topic, String zkRoot, String id) {
        super(hosts, topic);
        this.zkRoot = zkRoot;
        this.id = id;
    }
}
