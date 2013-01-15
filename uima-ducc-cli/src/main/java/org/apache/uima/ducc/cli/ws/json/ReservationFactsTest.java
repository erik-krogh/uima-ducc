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
package org.apache.uima.ducc.cli.ws.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

public class ReservationFactsTest {

	private Random random = new Random();
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss EEE");
	
	private String[] rClassSet = { "fixed", "reserve", "reserve-p7", "JobDriver" };
	private String[] stateSet = { "Assigned", "Completed", "WaitingForResources" };
	private String[] reasonSet = { "", "CanceledByUser", "CanceledBySystem", "ResourcesUnavailable" };
	private String[] descriptionSet = { "", "java-remote2", "Trainer:BlueJTrain", "ducc!", "Job Driver", "DistributionalSimilaritySemanticAccessServer1", "wics" };
	
	private String createId() {
		return ""+random.nextInt(10000);
	}
	
	private String createStart() {
		return simpleDateFormat.format(new Date());
	}
	
	private String createEnd() {
		return simpleDateFormat.format(new Date());
	}
	
	private String createUser() {
		return "user"+random.nextInt(10);
	}
	
	private String createRclass() {
		int index = random.nextInt(rClassSet.length);
		return rClassSet[index];
	}
	
	private String createState() {
		int index = random.nextInt(stateSet.length);
		return stateSet[index];
	}
	
	private String createReason() {
		int index = random.nextInt(reasonSet.length);
		return reasonSet[index];
	}
	
	private String createAllocation() {
		return ""+random.nextInt(4);
	}

	private String createSize() {
		return ""+(1+random.nextInt(50))+".0";
	}
	
	private List<NodePidList> createUserProcesses() {
		ArrayList<NodePidList> list = new ArrayList<NodePidList>();
		int nodeCount = random.nextInt(5);
		for(int i=0; i<nodeCount; i++) {
			String node = "bluej"+random.nextInt(801);
			int pidCount = random.nextInt(10);
			ArrayList<String> pidList = new ArrayList<String>();
			for(int j=0; j<pidCount; j++) {
				String pid = ""+random.nextInt(99999);
				pidList.add(pid);
			}
			NodePidList npl = new NodePidList(node,pidList);
			list.add(npl);
		}
		return list;
	}
	
	private List<String> createList() {
		List<String> list = new ArrayList<String>();
		return list;
	}
	
	private String createDescription() {
		int index = random.nextInt(descriptionSet.length);
		return descriptionSet[index];
	}
	
	private ReservationFacts createReservationFacts() {
		String id = createId();
		String start = createStart();
		String end = createEnd();
		String user = createUser();
		String rclass = createRclass();
		String state = createState();
		String reason = createReason();
		String allocation = createAllocation();
		List<NodePidList> userProcesses = createUserProcesses();
		String size = createSize();
		List<String> list = createList();
		String description = createDescription();
		
		ReservationFacts reservationFacts = new ReservationFacts(id,start,end,user,rclass,state,reason,allocation,userProcesses,size,list,description);
		return reservationFacts;
	}
	
	private ReservationFactsList createReservationFactsList() {
		ReservationFactsList reservationFactsList = new ReservationFactsList();
		for(int i=0; i<10; i++) {
			reservationFactsList.add(createReservationFacts());
		}
		return reservationFactsList;
	}
	
	public static void main(String[] args) {
		ReservationFactsTest reservationFactsTest = new ReservationFactsTest();
		ReservationFactsList reservationFactsList = reservationFactsTest.createReservationFactsList();
		Gson gson = new Gson();
		String jSon = gson.toJson(reservationFactsList);
		System.out.println(jSon);
		ReservationFactsList reconstituted = gson.fromJson(jSon, ReservationFactsList.class);
		for(ReservationFacts reservation : reconstituted) {
			System.out.println(reservation.id);
		}
	}

}
