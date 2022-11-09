/*
 * Copyright 2022 RelationalAI, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.relationalai;

import org.awaitility.core.DurationFactory;
import org.awaitility.pollinterval.PollInterval;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class DefaultPollInterval implements PollInterval {
    private final Double overheadRate;
    private final Long startTime;
    private final int maxDelay;

    /**
     * DefaultPollInterval constructor.
     * @param startTime in Epoch milliseconds
     * @param overheadRate
     * @param maxDelay in milliseconds
     */
    public DefaultPollInterval(Long startTime, Double overheadRate, int maxDelay) {
        this.overheadRate = overheadRate;
        this.startTime = startTime;
        this.maxDelay = maxDelay;
    }

    @Override
    public Duration next(int iteration, Duration previous) {
        if (iteration == 1) {
            return DurationFactory.of(500, TimeUnit.MILLISECONDS);
        }
        var duration = Math.min((Instant.now().toEpochMilli() - startTime) * overheadRate, maxDelay);
        return DurationFactory.of(Math.round(duration), TimeUnit.MILLISECONDS);
    }
}
