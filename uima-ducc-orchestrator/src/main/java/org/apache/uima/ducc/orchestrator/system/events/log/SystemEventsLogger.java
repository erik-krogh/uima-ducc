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
package org.apache.uima.ducc.orchestrator.system.events.log;

/*
 * Use the methods of this class to record system events to the system-event.log file.
 * This class is located in the Orchestrator since it alone is given responsibility for this task.
 */
import java.util.Properties;

import org.apache.uima.ducc.common.admin.event.DuccAdminEvent;
import org.apache.uima.ducc.common.admin.event.RmAdminReply;
import org.apache.uima.ducc.common.utils.id.DuccId;
import org.apache.uima.ducc.transport.event.AServiceRequest;
import org.apache.uima.ducc.transport.event.CancelJobDuccEvent;
import org.apache.uima.ducc.transport.event.CancelJobReplyDuccEvent;
import org.apache.uima.ducc.transport.event.CancelReservationDuccEvent;
import org.apache.uima.ducc.transport.event.CancelReservationReplyDuccEvent;
import org.apache.uima.ducc.transport.event.CancelServiceDuccEvent;
import org.apache.uima.ducc.transport.event.CancelServiceReplyDuccEvent;
import org.apache.uima.ducc.transport.event.DaemonDuccEvent;
import org.apache.uima.ducc.transport.event.DuccEvent;
import org.apache.uima.ducc.transport.event.IDuccContext.DuccContext;
import org.apache.uima.ducc.transport.event.ServiceReplyEvent;
import org.apache.uima.ducc.transport.event.SubmitJobDuccEvent;
import org.apache.uima.ducc.transport.event.SubmitJobReplyDuccEvent;
import org.apache.uima.ducc.transport.event.SubmitReservationDuccEvent;
import org.apache.uima.ducc.transport.event.SubmitReservationReplyDuccEvent;
import org.apache.uima.ducc.transport.event.SubmitServiceDuccEvent;
import org.apache.uima.ducc.transport.event.SubmitServiceReplyDuccEvent;
import org.apache.uima.ducc.transport.event.cli.JobReplyProperties;
import org.apache.uima.ducc.transport.event.cli.JobRequestProperties;
import org.apache.uima.ducc.transport.event.cli.ReservationReplyProperties;
import org.apache.uima.ducc.transport.event.cli.ReservationRequestProperties;
import org.apache.uima.ducc.transport.event.cli.ServiceReplyProperties;
import org.apache.uima.ducc.transport.event.cli.ServiceRequestProperties;
import org.apache.uima.ducc.transport.event.common.IDuccTypes.DuccType;
import org.apache.uima.ducc.transport.event.common.IDuccWork;
import org.apache.uima.ducc.transport.event.common.IDuccWorkJob;
import org.apache.uima.ducc.transport.event.common.IDuccWorkReservation;
import org.apache.uima.ducc.transport.event.common.IDuccWorkService;
import org.apache.uima.ducc.transport.event.common.IRationale;
import org.apache.uima.ducc.transport.event.common.IDuccWorkService.ServiceDeploymentType;

public class SystemEventsLogger {

	private static DuccLogger duccLogger = getEventLogger(SystemEventsLogger.class.getName());
	private static DuccId jobid = null;
	
	static public DuccLogger makeLogger(String claz, String componentId) {
        return DuccLogger.getLogger(claz, componentId);
	}
	
	static public DuccLogger getEventLogger(String claz)
    {
        return makeLogger("org.apache.uima.ducc.system.events.log", "event");
    }
	
	/*
	 * log a warning - nominally daemon start or stop
	 */
	public static void warn(String daemon, String state, String text) {
		String user = System.getProperty("user.name");
		String type = state;
		Object[] event = { };
		duccLogger.event_warn(daemon, user, type, event);
	}
	
	/*
	 * convenience method to get a property from a properties file if it exists,
	 * else return the default value specified
	 */
	private static String getProperty(Properties properties, String key, String defaultValue) {
		String retVal = defaultValue;
		if(properties != null) {
			if(key != null) {
				if(properties.containsKey(key)) {
					retVal = properties.getProperty(key);
				}
			}
		}
		return retVal;
	}
	
	/*
	 * convenience method to get a property from a properties file if it exists,
	 * else return the default value of "N/A"
	 */
	private static String getProperty(Properties properties, String key) {
		return getProperty(properties, key, "N/A");
	}
	
	private static boolean isTypeManagedReservation(String type) {
		boolean retVal = false;
		if(type != null) {
			if(type.equals(DuccEvent.EventType.SUBMIT_MANAGED_RESERVATION.toString())) {
				retVal = true;
			}
			else if(type.equals(DuccEvent.EventType.END_OF_MANAGED_RESERVATION.toString())) {
				retVal = true;
			}
			else if(type.equals(DuccEvent.EventType.CANCEL_MANAGED_RESERVATION.toString())) {
				retVal = true;
			}
		}
		return retVal;
	}
	
	/*
	 * derive type based on state change for Job
	 */
	private static String getType(String state, IDuccWorkJob job) {
		String type = state;
		if(type != null) {
			if(type.equals("Completed")) {
				type = DuccEvent.EventType.END_OF_JOB.toString();
			}
		}
		return type;
	}
	
	/*
	 * derive type based on state change for Reservation
	 */
	private static String getType(String state, IDuccWorkReservation reservation) {
		String type = state;
		if(type != null) {
			if(type.equals("Completed")) {
				type = DuccEvent.EventType.END_OF_RESERVATION.toString();
			}
		}
		return type;
	}
	
	/*
	 * derive type based on state change for Service
	 */
	private static String getType(String state, IDuccWorkService service) {
		String type = state;
		if(type != null) {
			if(type.equals("Completed")) {
				ServiceDeploymentType sdt = service.getServiceDeploymentType();
				if(sdt != null) {
					switch(sdt) {
					case other:
						type = DuccEvent.EventType.END_OF_MANAGED_RESERVATION.toString();
						break;
					default:
						type = DuccEvent.EventType.END_OF_SERVICE.toString();
						break;
					}
				}
			}
		}
		return type;
	}
	
	/*
	 * derive type (SERVICE or MANAGED_RESERVATION) for submit
	 */
	private static String getType(SubmitServiceDuccEvent request) {
		String location = "getType.SubmitServiceDuccEvent";
		String type = request.getEventType().name();
		duccLogger.debug(location, jobid, "type:"+type);
		DuccContext context = request.getContext();
		if(context != null) {
			duccLogger.debug(location, jobid, "context:"+context.toString());
			switch(context) {
			case ManagedReservation:
				type = DuccEvent.EventType.SUBMIT_MANAGED_RESERVATION.toString();
				break;
			default:
				break;
			}
		}
		return type;
	}
	
	/*
	 * derive type (SERVICE or MANAGED_RESERVATION) for cancel
	 */
	private static String getType(CancelServiceDuccEvent request) {
		String type = request.getEventType().name();
		DuccContext context = request.getContext();
		if(context != null) {
			switch(context) {
			case ManagedReservation:
				type = DuccEvent.EventType.CANCEL_MANAGED_RESERVATION.toString();
				break;
			default:
				break;
			}
		}
		return type;
	}
		
	public static void info(String daemon, String state, IDuccWork dw) {
		DuccType type = dw.getDuccType();
		if(type != null) {
			switch(type) {
			case Job:
				IDuccWorkJob job = (IDuccWorkJob) dw;
				info(daemon, state, job);
				break;
			case Service:
				IDuccWorkService service = (IDuccWorkService) dw;
				info(daemon, state, service);
				break;
			case Pop:
				IDuccWorkJob pop = (IDuccWorkJob) dw;
				info(daemon, state, pop);
				break;
			case Reservation:
				IDuccWorkReservation reservation = (IDuccWorkReservation) dw;
				info(daemon, state, reservation);
				break;
			case Undefined:
				IDuccWorkJob undefined = (IDuccWorkJob) dw;
				info(daemon, state, undefined);
				break;
			default:
			}
		}
		else {
			IDuccWorkJob job = (IDuccWorkJob) dw;
			info(daemon, state, job);
		}
	}
	
	/*
	 * log a job submit request
	 */
	public static void info(String daemon, SubmitJobDuccEvent request, SubmitJobReplyDuccEvent response) {
		Properties qprops = request.getProperties();
		String user = getProperty(qprops, JobRequestProperties.key_user);
		String type = request.getEventType().name();
		String id = getProperty(qprops, JobRequestProperties.key_id);
		String sclass = getProperty(qprops, JobRequestProperties.key_scheduling_class);
		String size = getProperty(qprops, JobRequestProperties.key_process_memory_size);
		Properties rprops = response.getProperties();
		String message = getProperty(rprops, JobReplyProperties.key_message, "");
		Object[] event = { "id:"+id, "class:"+sclass, "size:"+size, message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a job cancel request
	 */
	public static void info(String daemon, CancelJobDuccEvent request, CancelJobReplyDuccEvent response) {
		Properties qprops = request.getProperties();
		String user = getProperty(qprops, JobRequestProperties.key_user);
		String type = request.getEventType().name();
		String id = getProperty(qprops, JobRequestProperties.key_id);
		Properties rprops = response.getProperties();
		String message = getProperty(rprops, JobReplyProperties.key_message);
		Object[] event = { "id:"+id, message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a job state change - nominally Completed only is logged
	 */
	private static void info(String daemon, String state, IDuccWorkJob job) {
		String user = job.getStandardInfo().getUser();
		String type = getType(state,job);
		String id = job.getId();
		String reason = job.isCompleted() ? job.getCompletionType().name() : "";
		String rationale = "";
		IRationale completionRationale = job.getCompletionRationale();
		if(completionRationale != null) {
			rationale = completionRationale.getText();
		}
		Object[] event = { "id:"+id,reason,rationale };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a reservation submit request
	 */
	public static void info(String daemon, SubmitReservationDuccEvent request, SubmitReservationReplyDuccEvent response) {
		Properties qprops = request.getProperties();
		String user = getProperty(qprops, ReservationRequestProperties.key_user);
		String type = request.getEventType().name();
		String id = getProperty(qprops, ReservationRequestProperties.key_id);
		String sclass = getProperty(qprops, ReservationRequestProperties.key_scheduling_class);
		String size = getProperty(qprops, ReservationRequestProperties.key_memory_size);
		Properties rprops = response.getProperties();
		String message = getProperty(rprops, ReservationReplyProperties.key_message, "");
		Object[] event = { "id:"+id, "class:"+sclass, "size:"+size, message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a reservation cancel request
	 */
	public static void info(String daemon, CancelReservationDuccEvent request, CancelReservationReplyDuccEvent response) {
		Properties qprops = request.getProperties();
		String user = getProperty(qprops, ReservationRequestProperties.key_user);
		String type = request.getEventType().name();
		String id = getProperty(qprops, ReservationRequestProperties.key_id);
		Properties rprops = response.getProperties();
		String message = getProperty(rprops, ReservationReplyProperties.key_message);
		Object[] event = { "id:"+id, message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a reservation state change - nominally Completed only is logged
	 */
	private static void info(String daemon, String state, IDuccWorkReservation reservation) {
		String user = reservation.getStandardInfo().getUser();
		String type = getType(state,reservation);
		String id = reservation.getId();
		String reason = reservation.isCompleted() ? reservation.getCompletionType().name() : "";
		String rationale = "";
		IRationale completionRationale = reservation.getCompletionRationale();
		if(completionRationale != null) {
			rationale = completionRationale.getText();
		}
		Object[] event = { "id:"+id,reason,rationale };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a service submit request
	 */
	public static void info(String daemon, SubmitServiceDuccEvent request, SubmitServiceReplyDuccEvent response) {
		Properties properties = request.getProperties();
		String sclass = getProperty(properties, ServiceRequestProperties.key_scheduling_class);
		String size = getProperty(properties, ServiceRequestProperties.key_process_memory_size);
		Properties rprops = response.getProperties();
		String message = getProperty(rprops, ServiceReplyProperties.key_message, "");
		String user = getProperty(properties, ServiceRequestProperties.key_user);
		//
		String type = getType(request);
		if(isTypeManagedReservation(type)) {
			String id = getProperty(properties, ServiceRequestProperties.key_id);
			Object[] event = { "id:"+id, "class:"+sclass, "size:"+size, message };
			duccLogger.event_info(daemon, user, type, event);
		}
		else {
			String id = getProperty(properties, ServiceRequestProperties.key_service_id);
			String instance = getProperty(properties, ServiceRequestProperties.key_id);
			String name = getProperty(properties, ServiceRequestProperties.key_service_request_endpoint);
			Object[] event = { "id:"+id, "instance:"+instance, "name:"+name, "class:"+sclass, "size:"+size, message };
			duccLogger.event_info(daemon, user, type, event);
		}
	}
	
	/*
	 * log a service cancel request
	 */
	public static void info(String daemon, CancelServiceDuccEvent request, CancelServiceReplyDuccEvent response) {
		Properties properties = request.getProperties();
		Properties rprops = response.getProperties();
		String message = getProperty(rprops, ReservationReplyProperties.key_message);
		String user = properties.getProperty(ServiceRequestProperties.key_user);
		//
		String type = getType(request);
		if(isTypeManagedReservation(type)) {
			String id = properties.getProperty(ServiceRequestProperties.key_id);
			Object[] event = { "id:"+id, message };
			duccLogger.event_info(daemon, user, type, event);
		}
		else {
			String id = getProperty(properties, ServiceRequestProperties.key_service_id);
			String instance = properties.getProperty(ServiceRequestProperties.key_id);
			String name = getProperty(properties, ServiceRequestProperties.key_service_request_endpoint);
			Object[] event = { "id:"+id, "instance:"+instance, "name:"+name, message };
			duccLogger.event_info(daemon, user, type, event);
		}
	}
	
	/*
	 * log a service state change - nominally Completed only is logged
	 */
	private static void info(String daemon, String state, IDuccWorkService service) {
		String user = service.getStandardInfo().getUser();
		String type = getType(state,service);
		if(isTypeManagedReservation(type)) {
			String id = service.getId();
			String reason = "";
			String rationale = "";
			Object[] event = { "id:"+id,reason,rationale };
			duccLogger.event_info(daemon, user, type, event);
		}
		else {
			String id = service.getServiceId();
			String instance = service.getId();
			String name = service.getServiceEndpoint();
			String reason = "";
			//String reason = service.isCompleted() ? service.getCompletionType().name() : "";
			String rationale = "";
			//IRationale completionRationale = service.getCompletionRationale();
			//if(completionRationale != null) {
			//	rationale = completionRationale.getText();
			//}
			Object[] event = { "id:"+id,"instance:"+instance,"name:"+name,reason,rationale };
			duccLogger.event_info(daemon, user, type, event);
		}
	}
	
	/*
	 * log a service manager request
	 */
	public static void info(String daemon, AServiceRequest request) {
		String user = request.getUser();
		String type = request.getEventType().name();
		String message = "request";
		Object[] event = { message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a service manager response
	 */
	public static void info(String daemon, AServiceRequest request, ServiceReplyEvent response) {
		String user = request.getUser();
		String type = request.getEventType().name();
		long id = response.getId();
		boolean rc = response.getReturnCode();
		String message = response.getMessage();
		Object[] event = { "id:"+id, "rc:"+rc, message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a resource manager request
	 */
	public static void info(String daemon, DuccAdminEvent request) {
		String user = request.getUser();
		String type = request.getClass().getSimpleName();
		String message = "request";
		Object[] event = { message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * log a resource manager response
	 */
	public static void info(String daemon, DuccAdminEvent request, RmAdminReply response) {
		String user = request.getUser();
		String type = request.getClass().getSimpleName();
		String message = normalize(response.getMessage());
		Object[] event = { "response: "+message };
		duccLogger.event_info(daemon, user, type, event);
	}
	
	/*
	 * normalize message
	 */
	private static String normalize(String message) {
		String text = "";
		if(message != null) {
			text = message.trim();
		}
		return text;
	}
	
	/*
	 * log a daemon event
	 */
	public static void info(DaemonDuccEvent dde) {
		String daemon = dde.getDaemon().getAbbrev();
		String user = System.getProperty("user.name");
		String type = dde.getEventType().name();
		Object[] event = { "node: "+dde.getNodeIdentity().getName(), "tod: "+dde.getTod() };
		duccLogger.event_info(daemon, user, type, event);
	}
}
