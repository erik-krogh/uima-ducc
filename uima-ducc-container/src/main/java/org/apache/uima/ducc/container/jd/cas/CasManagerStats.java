/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.apache.uima.ducc.container.jd.cas;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CasManagerStats {
	
	public enum RetryReason { ProcessPreempt, ProcessDown, NodeDown, UserErrorRetry, TimeoutRetry };
	
	private AtomicInteger crTotal = new AtomicInteger(0);
	private AtomicInteger crGets = new AtomicInteger(0);
	
	private AtomicInteger retryQueuePuts = new AtomicInteger(0);
	private AtomicInteger retryQueueGets = new AtomicInteger(0);
	
	private ConcurrentHashMap<String,AtomicInteger> retryReasonsMap = new ConcurrentHashMap<String,AtomicInteger>();
	
	public void setCrTotal(int value) {
		crTotal.set(value);
	}
	
	public int getCrTotal() {
		return crTotal.get();
	}
	
	public void incCrGets() {
		crGets.incrementAndGet();
	}
	
	public int getCrGets() {
		return crGets.get();
	}
	
	public void incRetryQueuePuts() {
		retryQueuePuts.incrementAndGet();
	}
	
	public int getRetryQueuePuts() {
		return retryQueuePuts.get();
	}
	
	public void incRetryQueueGets() {
		retryQueueGets.incrementAndGet();
	}
	
	public int getRetryQueueGets() {
		return retryQueueGets.get();
	}
	
	public void incRetryReasons(RetryReason retryReason) {
		if(retryReason != null) {
			String key = retryReason.name();
			retryReasonsMap.putIfAbsent(key, new AtomicInteger(0));
			AtomicInteger value = retryReasonsMap.get(key);
			value.incrementAndGet();
		}
	}
	
	public ConcurrentHashMap<String,AtomicInteger> getRetryReasons() {
		return retryReasonsMap;
	}
	
	public int getNumberOfPreemptions() {
		int retVal = 0;
		String key = RetryReason.ProcessPreempt.name();
		if(retryReasonsMap.containsKey(key)) {
			AtomicInteger value = retryReasonsMap.get(key);
			retVal = value.get();
		}
		return retVal;
	}
}