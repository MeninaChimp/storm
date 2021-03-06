/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.storm.druid.bolt;

import org.apache.storm.tuple.ITuple;

import java.io.Serializable;

/**
 * This class gives a mapping of a {@link ITuple} to Druid Event
 * @deprecated the preferred way to ingest streaming events to druid is through Kafka, as such storm-druid
 * is deprecated and will be removed in 2.x releases of storm.
 */
@Deprecated
public interface ITupleDruidEventMapper<E> extends Serializable {

    /**
     * Returns a Druid Event for a given {@code tuple}.
     *
     * @param tuple tuple instance
     */
    public E getEvent(ITuple tuple);

}
