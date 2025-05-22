package com.jio.crm.dms.core;

public interface HttpParamConstants {

	/**
	 * Constants for ERM
	 */
	public static final String EVENT_NAME = "X-Event-Name";
	public static final String SRC_MS_IP_PORT = "X-Service-IP";
	public static final String SRC_MS_CONTEXT = "X-Service-Context";
	public static final String FLOW_ID = "X-Flow-Id";
	public static final String BRANCH_ID = "X-Branch-Id";
	public static final String HOP_COUNT = "X-Hop-Count";
	public static final String MSG_TYPE = "X-Message-Type";
	public static final String MS_NAME = "X-Microservice-Name";
	public static final String PUBLISHER_NAME = "X-Publisher-Name";
	public static final String SUBSCRIBER_NAME = "X-Subscriber-Name";
	public static final String EXECUTION_TYPE_NAME = "X-Execution-Type";
	public static final String EXECUTION_TYPE = "SYNC";
	public static final String ASYNC_EXECUTION_TYPE = "ASYNC";

	public static final String LB_ID = "lbId";

	public static final String QUERY_PARAM = "cron_name";
	public static final String QUERY_VALUE = "cron1hr";
	public static final String TARGET_MS_PSC = "PSC";
	public static final String TARGET_MS_INDEXER = "Indexer";

	public static final String MY_EVENT = "event_name";
	public static final String MSG_TYPE_EVENT_VALUE = "event";
	public static final String MSG_TYPE_EVENT_ACK_VALUE = "eventACK";
	public static final String MSG_TYPE_EVENT_DELIVERY_REPORT = "deliveryReport";
	public static final String MSG_TYPE_EVENT_ACK_REPORT = "ackReport";
	
	/**
	 * Query Parameters
	 */
	public static final String ACTION = "action";
	public static final String ACTION_SUBSCRIBE = "subscribe";
	public static final String ACTION_UNSUBSCRIBE = "unsubscribe";

	/**
	 * OAM JSON body parameters
	 */
	public static final String EDGELB = "EdgeLB";
	public static final String SUBSCRIBECOMPONENTTYPE = "subscribecomponenttype";
	public static final String ERM_ID = "id";
	public static final String ERM_IP = "ip";
	public static final String ERM_PORT = "port";
	public static final String ERM_ACTIVE = "active";
	public static final String MS_ERM = "ERM";
	public static final String MS_RMR = "RMR";

	/**
	 * Event names for ERM
	 */
	public static final String CREATE_TASK_FTP = "CREATE_TASK_FTP";
	public static final String MODIFY_TASK_FTP = "MODIFY_TASK_FTP";
	public static final String DELETE_TASK_FTP = "DELETE_TASK_FTP";
	public static final String ADD_PRODUCT = "ADD_PRODUCT";
	public static final String ADD_METDATA = "ADD_METADATA";
	public static final String ADD_RULE = "ADD_RULE";
	public static final String GET_VNF_COUNTER_DICTIONARY = "GET_VNF_COUNTER_DICTIONARY";

	/**
	 * Context names for ERM
	 */
	public static final String ERM_EVENT_MESSAGE_CTX = "erm/event/";
	public static final String ERM_EVENT_SUBSCRIPTION_CTX = "/erm/event/subscribe/";
	

	/**
	 * TASK JSON FIELD and VALUES
	 */



}
