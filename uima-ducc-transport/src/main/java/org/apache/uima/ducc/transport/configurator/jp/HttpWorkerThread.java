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

package org.apache.uima.ducc.transport.configurator.jp;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.uima.ducc.common.utils.DuccLogger;
import org.apache.uima.ducc.common.utils.Utils;
import org.apache.uima.ducc.container.net.iface.IMetaCas;
import org.apache.uima.ducc.container.net.iface.IMetaCasTransaction;
import org.apache.uima.ducc.container.net.iface.IMetaCasTransaction.JdState;
import org.apache.uima.ducc.container.net.iface.IMetaCasTransaction.Type;
import org.apache.uima.ducc.container.net.iface.IPerformanceMetrics;
import org.apache.uima.ducc.container.net.impl.MetaCasTransaction;
import org.apache.uima.ducc.container.net.impl.PerformanceMetrics;
import org.apache.uima.ducc.transport.event.common.IProcessState.ProcessState;

public class HttpWorkerThread implements Runnable {
	DuccLogger logger = new DuccLogger(HttpWorkerThread.class);
	private DuccHttpClient httpClient = null;
//	private IUimaProcessor uimaProcessor;
	private JobProcessComponent duccComponent;
	private Object monitor = new Object();
	private CountDownLatch workerThreadCount = null;
	private CountDownLatch threadReadyCount = null;
	private Object processorInstance = null;

	public HttpWorkerThread(JobProcessComponent component, DuccHttpClient httpClient,
			Object processorInstance, CountDownLatch workerThreadCount,
			CountDownLatch threadReadyCount) {
		this.duccComponent = component;
		this.httpClient = httpClient;
		this.processorInstance = processorInstance;
		this.workerThreadCount = workerThreadCount;
		this.threadReadyCount = threadReadyCount;
	}
	@SuppressWarnings("unchecked")
	public void run() {
		String command="";
		PostMethod postMethod = null;
	    logger.info("HttpWorkerThread.run()", null, "Starting JP Process Thread Id:"+Thread.currentThread().getId());
	    Method processMethod = null;
	    boolean error=false;
	    // ***** DEPLOY ANALYTICS ***********
	    // First, deploy analytics in a provided process container. Use java reflection to call
	    // deploy method. The process container has been instantiated in the main thread and
	    // loaded from ducc-user jar provided in system classpath
	    try {
			processMethod = processorInstance.getClass().getDeclaredMethod("process", Object.class);	
			Method deployMethod = processorInstance.getClass().getDeclaredMethod("deploy", String.class);
			deployMethod.invoke(processorInstance, (Object)Utils.findDuccHome());

			// each thread needs its own PostMethod
			postMethod = new PostMethod(httpClient.getJdUrl());
			// Set request timeout
			postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, duccComponent.getTimeout());
	   	} catch( Throwable t) {
	   		error = true;
	   		synchronized(JobProcessComponent.class) {
				duccComponent.setState(ProcessState.FailedInitialization);
			}
            t.printStackTrace();
	   		logger.error("HttpWorkerThread.run()", null, t);
	   		System.out.println("EXITING WorkThread ID:"
					+ Thread.currentThread().getId());
	   		return;  // non-recovorable error
	   	} finally {
			// count down the latch. Once all threads deploy and initialize their analytics the processing
			// may being
			threadReadyCount.countDown();  // this thread is ready
			// **************************************************************************
			// now block and wait until all threads finish deploying and initializing 
			// analytics in provided process container
			// **************************************************************************
			try {
				threadReadyCount.await();
			} catch( Exception ie) {}

		
//			workerThreadCount.countDown();

			if (!error) {
				synchronized(JobProcessComponent.class) {
					duccComponent.setState(ProcessState.Running);
				}
			}
	   		
	   	}
			
			
		// run forever (or until the process throws IllegalStateException
	   	logger.info("HttpWorkerThread.run()", null, "Processing Work Items - Thread Id:"+Thread.currentThread().getId());
		try {

			while (duccComponent.isRunning()) {  //service.running && ctx.state().process(ctx)) {

				try {
					IMetaCasTransaction transaction = new MetaCasTransaction();
					//System.out.println("Requesting Work from JD");
					// According to HTTP spec, GET may not contain Body in 
					// HTTP request. HttpClient actually enforces this. So
					// do a POST instead of a GET.
					transaction.setType(Type.Get);  // Tell JD you want a CAS
					command = Type.Get.name();
			    	logger.debug("HttpWorkerThread.run()", null, "Thread Id:"+Thread.currentThread().getId()+" Requesting next WI from JD");;
					transaction = httpClient.execute(transaction, postMethod);
                    if ( transaction.getMetaCas()!= null) {
    					logger.info("run", null,"Thread:"+Thread.currentThread().getId()+" Recv'd WI:"+transaction.getMetaCas().getSystemKey());
                    } else {
    					logger.debug("run", null,"Thread:"+Thread.currentThread().getId()+" Recv'd JD Response, however there is no MetaCas. Sleeping for "+duccComponent.getThreadSleepTime());
                    }

					// Confirm receipt of the CAS. 
					transaction.setType(Type.Ack);
					command = Type.Ack.name();
					httpClient.execute(transaction, postMethod); // Ready to process
                    logger.debug("run", null,"Thread:"+Thread.currentThread().getId()+" Sent ACK");
					
                    
					// if the JD did not provide a CAS, most likely the CR is
					// done. In such case, reduce frequency of Get requests
					// by sleeping in between Get's. Eventually the JD will 
					// confirm that there is no more work and this thread
					// can exit.
					if ( transaction.getMetaCas() == null || transaction.getMetaCas().getUserSpaceCas() == null) {
						// the JD says there are no more WIs. Sleep awhile
						// do a GET in case JD changes its mind. The JP will
						// eventually be stopped by the agent
						synchronized (monitor) {
							try {
								monitor.wait(duccComponent.getThreadSleepTime());
							} catch (InterruptedException e) {
							}
						}
						// There is no CAS. It looks like the JD CR is done but there
						// are still WIs being processed. Slow down the rate of requests	
					} else {
						// process the CAS
						try {
							// using java reflection, call process to analyze the CAS
							 
							 List<Properties> metrics = (List<Properties>)processMethod.
							   invoke(processorInstance, transaction.getMetaCas().getUserSpaceCas());
							
							//metrics.add(new Properties());   // empty for now
		                    logger.debug("run", null,"Thread:"+Thread.currentThread().getId()+" process() completed");
							IPerformanceMetrics metricsWrapper =
									new PerformanceMetrics();
							metricsWrapper.set(metrics);
							
							transaction.getMetaCas().setPerformanceMetrics(metricsWrapper);
							
						}  catch( InvocationTargetException ee) {
							// The only way we would be here is if uimaProcessor.process() method failed.
							// In this case, the process method serialized stack trace into binary blob
							// and wrapped it in AnalysisEngineProcessException. The serialized stack 
							// trace is available via getMessage() call.

							// This is process error. It may contain user defined
							// exception in the stack trace. To protect against
						    // ClassNotFound, the entire stack trace was serialized.
							// Fetch the serialized stack trace and pass it on to
							// to the JD. The actual serialized stack trace is wrapped in
							// RuntimeException->AnalysisEngineException.message

							IMetaCas mc = transaction.getMetaCas();
							Method getLastSerializedErrorMethod = processorInstance.getClass().getDeclaredMethod("getLastSerializedError");
							byte[] serializedException =
							    (byte[])getLastSerializedErrorMethod.invoke(processorInstance);
							mc.setUserSpaceException(serializedException);								

							logger.info("run", null, "Work item processing failed - returning serialized exception to the JD");
						} catch( Exception ee) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
						    ObjectOutputStream oos = new ObjectOutputStream( baos );
						    oos.writeObject( ee);
						    oos.close();
							transaction.getMetaCas().setUserSpaceException(baos.toByteArray());
							logger.error("run", null, ee);
						}
						transaction.getMetaCas().setUserSpaceCas(null);
						transaction.setType(Type.End);
						command = Type.End.name();
						httpClient.execute(transaction, postMethod); // Work Item Processed - End
	                    logger.info("run", null,"Thread:"+Thread.currentThread().getId()+" sent END for WI:"+transaction.getMetaCas().getSystemKey());
                        if ( transaction.getMetaCas() != null &&
                        		transaction.getMetaCas().getUserSpaceException() != null ) {
        					duccComponent.getLogger().warn("run", null, "Worker thread exiting due to exception in process()");
        					duccComponent.setState(ProcessState.Stopping);
                        	break;
                        }
					}
				} catch( SocketTimeoutException e) {
					duccComponent.getLogger().warn("run", null, "Timed Out While Awaiting Response from JD for "+command+" Request - Retrying ...");
					System.out.println("Time Out While Waiting For a Reply from JD For "+command+" Request");
				}
				catch (Exception e ) {
					duccComponent.getLogger().warn("run", null, e);
					duccComponent.getLogger().warn("run", null, "Caught Unexpected Exception - Exiting Thread "+Thread.currentThread().getId() );
					e.printStackTrace();
					break; 
				} finally {

				}

			}

		} catch (Throwable t) {
			t.printStackTrace();
			duccComponent.getLogger().warn("run", null, t);
		} finally {
			System.out.println("EXITING WorkThread ID:"
					+ Thread.currentThread().getId());
		    try {
		    	// Determine if the Worker thread has thread affinity to specific AE
		    	// instance. This depends on the process container. If this process
		    	// uses pieces part (not DD), than the thread should call stop on
		    	// process container which will than destroy the AE. User code may
		    	// store stuff in ThreadLocal and use it in the destroy method.
		    	Method useThreadAffinityMethod = processorInstance.getClass().getDeclaredMethod("useThreadAffinity");	
				boolean useThreadAffinity =
						(Boolean)useThreadAffinityMethod.invoke(processorInstance);
				if ( useThreadAffinity) {
					Method stopMethod = processorInstance.getClass().getDeclaredMethod("stop");
					stopMethod.invoke(processorInstance);
				}
		   	} catch( Throwable t) {
		   		t.printStackTrace();
		   	} finally {
				workerThreadCount.countDown();
		   	}
		
		}

	}

}