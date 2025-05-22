package com.jio.crm.dms.core;

/**
 * @author Barun.Rai
 *
 */
public enum RequestHeaderEnum {


	// Header Name

	HEADER_NAME_EVENT_NAME("X-Event-Name"),
	
	HEADER_NAME_ASYNC_FLOW("X-Async-Flow"),

	HEADER_NAME_BRANCH_ID("X-Branch-Id"), 
	
	HEADER_NAME_SERVICE_CONTEXT("X-Service-Context"), 
	
	HEADER_NAME_FLOW_ID("X-Flow-Id"), 
	
	HEADER_NAME_HOP_COUNT("X-HOP-COUNT"), 
	
	HEADER_NAME_MESSAGE_TYPE("X-Message-Type"), 
	
	HEADER_NAME_SERVICE_IP("X-Service-IP"),
	
	HEADER_NAME_PUBLISHER_NAME("X-Publisher-Name"),
	
	// Introduced in v2.1 ERM and ELB
	
	HEADER_NAME_ERM_IDENTIFIER("X-ERM-IDENTIFIER"),
	
	HEADER_NAME_TARGET_ERM("X-TARGET-ERM"),
	
	HEADER_NAME_LB_URL("X-LB-URL"),
	
	HEADER_NAME_SOCKET_ADDRESS("X-SOCKET-ADDRESS"),
	
	HEADER_NAME_USERNAME("X-USERNAME"),
	
	HEADER_NAME_PARENT_BRANCH_ID("X-PARENT-BRANCH-ID"),
	
	HEADER_NAME_EXECUTION_TYPE("X-Execution-Type");

	private String value;

	private RequestHeaderEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}


}
