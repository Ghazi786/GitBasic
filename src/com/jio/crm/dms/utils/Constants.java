package com.jio.crm.dms.utils;

import java.io.File;

import com.atom.OAM.Client.Management.OamClientCacheManager;

public interface Constants {
	public static final String COMMA_DELIMITER = ",";
	public static final String DAPP = "Dapp";
	public static final String CMN_PRIORITY_DETAILS = "PriorityDetails";
	public static final String IS_ACTIVE = "active";

	public static final String NETTY_PORT = "nettyPort";
	public static final String OAM_PORT = "oamport";
	public static final String OAM_NETTY_PORT = "oamnettyport";

	public static final String IP = "ip";
	public static final String PORT = "port";
	public static final String HTTP = "http";
	public static final String HTTPS = "https";
	public static final String HTTPPORT = "httpport";
	public static final String HTTPSPORT = "httpsPort";
	public static final String RE_REGISTER = "reRegister";
	public static final String PARAMETER_ID = "id";

	public static final String DMS_MS = "DMS_MS";
	public static final String INTEGRATIONS_MS = "INTEGRATIONS_MS";
	public static final String UI_HANDLER = "/cmn";
	public static final String ACTION = "action";
	public static final String TAG_HTTP = "https://";
	public static final String OP_COLON = ":";
	public static final String POST = "POST";
	public static final String PUT = "PUT";
	public static final String GET = "GET";
	public static final String DELETE = "DELETE";
	public static final String X_SUBSCRIBER_NAME = "X-Subscriber-Name";
	public static final String X_EVENT_NAME = "X-Event-Name";
	public static final String X_SERVICE_IP = "X-Service-IP";
	public static final String X_SERVICE_CONTEXT = "X-Service-Context";
	public static final String X_FLOW_ID = "X-Flow-Id";
	public static final String X_BRANCH_ID = "X-Branch-Id";
	public static final String MSG_TYPE_EVENT_VALUE = "event";
	public static final String X_HOP_COUNT = "X-Hop-Count";
	public static final String X_HOP_COUNT_VALUE = "20";
	public static final String X_PUBLISHER_NAME = "X-Publisher-Name";
	public static final String X_MESSAGE_TYPE = "X-Message-Type";
	public static final String MSG_TYPE_EVENT_ACK_VALUE = "eventACK";
	public static final String HTTP_IP = "HTTP_IP";
	public static final String HTTP_PORT = "HTTP_PORT";
	public static final String ERM_IP = "ERM_IP";
	public static final String ERM_PORT = "ERM_PORT";
	public static final String NODEID = "node-id";
	public static final String TENANTID = "tenant-id";
	public static final String TXNID = "txn-id";
	public static final String TXNVER = "txn-ver";
	public static final String TXNTYPE = "txn-type";
	public static final String TXNINITIATORNAME = "txn-initiator-name";
	public static final String NETWORKID = "network-id";
	public static final String LEDGERID = "ledger-id";
	public static final String TXNRESULT = "txn-result";
	public static final String TXNINITIATORID = "txn-initiator-node-id";
	public static final String TOKENBALANCE = "token-balance-redeem";
	public static final String CUSTOMERID = "customeRid";
	public static final String TXNNODEID = "node-id-of-token-to-be-redemmed";
	public static final String REQNODEID = "initiator-node-id";
	public static final String ISSSLSTRICT = "issslstrict";
	public static final String KEYSTOREFILE = "keyStoreFile";
	public static final String KEYPHRASE = "keyPhrase";
	public static final String TRUSTSTOREPATH = "trustStorePath";
	public static final String TRUSTSTOREPASSWORD = "trustStorePassword";
	public static final String NODETOKEN = "nodetoken";
	public static final String NODENAME = "nodename";
	public static final String REST_PORT = "restport";
	public static final String JMX_SERVER_PORT = "jmxserverport";
	public static final String JMX_WEB_SERVER_PORT = "jmxwebserverport";
	public static final String PRODUCTNAME = "productname";
	public static final String PRODUCTVERSION = "productversion";

	public static final String CNTR_IN_HTTP_REQUEST = "cntr_in_http_request";
	public static final String CNTR_IN_HTTP_RESPONSE = "cntr_in_http_response";
	public static final String CNTR_OUT_HTTP_REQUEST = "cntr_out_http_request";
	public static final String CNTR_OUT_HTTP_RESPONSE = "cntr_out_http_response";
	public static final String CNTR_OUT_HTTP_FAILURE = "cntr_out_http_failure";
	public static final String BULK = "bulk";
	public static final String GET_BULK_CONFIG = "getConfigParam";
	public static final String SET_SINGLE_CONFIG = "setConfigParam";

	public static final int CLIENT_ERROR = 400;
	public static final int SERVER_ERROR = 500;
	public static final int SUCCESS_ERROR = 200;
	public static final int ACCEPTED_ERROR = 202;

	public static final String X_SCROLL_ID = "X-Scroll-Id";
	public static final String PARAMETER_RESET = "reset";
	public static final String PARAMETER_TYPE = "type";
	public static final String RESET_TRUE = "1";

	// components
	public static final String COMPONENT_TYPE_EDGELB = "EdgeLB";
	public static final String COMPONENT_TYPE_ERM = "ERM";
	public static final String ACTIVE = "active";
	public static final String SUBSCRIBE_COMPONENT_TYPE = "subscribecomponenttype";
	public static final String COMPONENT_TYPE = "componenttype";
	public static final String ID1 = "_id";
	public static final String REQUEST = "request";
	public static final String TIMESTAMP = "timestamp";
	public static final String BODY = "body";
	public static final String HEADERS = "headers";

	/**************************************************************/
	/******************* LOGGER CONSTANTS ***************************/
	/**************************************************************/

	public static final String LOG_LEVEL_ERROR = "ERROR";
	public static final String LOG_LEVEL_DEBUG = "DEBUG";
	public static final String LOG_LEVEL_INFO = "INFO";
	public static final String LOG_LEVEL_WARN = "WARN";
	public static final String LOG_LEVEL_TRACE = "TRACE";
	public static final String LOG_LEVEL_FATAL = "FATAL";
	public static final String LOG_LEVEL_OFF = "OFF";

	public static final String FAILURE = "FAILURE";
	public static final String SUCCESS_STATUS = "SUCCESS";

	/*************************************************************/
	/******************** HANDLER ***********************************/
	/*************************************************************/
	public static final String REGISTRY_MANAGER = "/dapp/registrymanager";
	public static final String TRANSACTION_MANAGER = "/dapp/transaction";
	public static final String COUNTER_MANAGER = "/dapp/counter";
	public static final String ALARM_MANAGER = "/dapp/alarm";
	public static final String CONFIG_PARAM_MANAGER = "/dapp/configparam";
	public static final String PARAM_CONTEXT_ERM_EVENT_HANDLER = "/dapp/eventHandler";

	/******************** DUMP FILE PATH ***********************************/
	public static final String DUMP_PATH_JETTY = System.getProperty("user.dir") + File.separator + ".." + File.separator
			+ "dumps" + File.separator + "JettyStats";

	public static final String DUMP_PATH_OBJECTPOOL = System.getProperty("user.dir") + File.separator + ".."
			+ File.separator + "dumps" + File.separator + "ObjectPoolStats";

	// Replication components
	public static final String CLUSTER_NAME = "clusterName";
	public static final String N_NAME = "nodeName";
	public static final String NODE_IP = "nodeIp";
	public static final String CLUSTER_ADDRESSS_COMMA_SEPARATED = "ips";
	public static final String N_NAMES_COMMA_SEPARATED = "nodeNames";
	public static final String TIME_RECONNECT = "localReconnectionInterval";
	public static final String REMOTEPORT = "remotePort";
	public static final String ISGEOPORT = "isgeoNode";

	// counters
	public static final String CNTR_CSV_FETCH_REQUEST_SUCCESS = "CNTR_CSV_FETCH_REQUEST_SUCCESS";
	public static final String CNTR_CSV_FETCH_REQUEST_FAILURE = "CNTR_CSV_FETCH_REQUEST_FAILURE";
	public static final String CNTR_CSV_ES_RECORDS_PROCESS_FAILURE = "CNTR_CSV_ES_RECORDS_PROCESS_FAILURE";
	public static final String CNTR_CSV_FILE_ES_SUCCESS = "CNTR_CSV_FILE_ES_SUCCESS";
	public static final String CNTR_ES_TRANSACTION_CREATION_FAILURE = "CNTR_ES_TRANSACTION_CREATION_FAILURE";
	public static final String CNTR_UI_REQUESTS = "CNTR_UI_REQUESTS";
	public static final String CNTR_UI_REQUESTS_RETURN = "CNTR_UI_REQUESTS_RETURN";
	public static final String CNTR_ES_SEARCH_REQUESTS = "CNTR_ES_SEARCH_REQUESTS";
	public static final String CNTR_ES_SEARCH_SUCCESS = "CNTR_ES_SEARCH_SUCCESS";
	public static final String CNTR_ES_SEARCH_FAILURE = "CNTR_ES_SEARCH_FAILURE";
	public static final String CNTR_ERM_FAILED_EVENT_REQUESTS_RECEIVE = "CNTR_ERM_FAILED_EVENT_REQUESTS_RECEIVE";
	public static final String CNTR_ERM_INVALID_EVENT_REQUESTS_RECEIVE = "CNTR_ERM_INVALID_EVENT_REQUESTS_RECEIVE";
	public static final String CNTR_ERM_TOTAL_EVENT_ACK_REQUESTS_RECEIVE = "CNTR_ERM_TOTAL_EVENT_ACK_REQUESTS_RECEIVE";
	public static final String CNTR_ERM_SUCCESS_EVENT_ACK_REQUESTS_RECEIVE = "CNTR_ERM_SUCCESS_EVENT_ACK_REQUESTS_RECEIVE";
	public static final String CNTR_ERM_INVALID_EVENT_ACK_REQUESTS_RECEIVE = "CNTR_ERM_INVALID_EVENT_ACK_REQUESTS_RECEIVE";
	public static final String CNTR_ERM_FAILED_EVENT_ACK_REQUESTS_RECEIVE = "CNTR_ERM_FAILED_EVENT_ACK_REQUESTS_RECEIVE";
	public static final String CNTR_ERM_FAILED_SUBSCRIBE_REQUESTS = "CNTR_ERM_FAILED_SUBSCRIBE_REQUESTS";
	public static final String CNTR_ERM_TOTAL_SUBSCRIBE_REQUESTS = "CNTR_ERM_TOTAL_SUBSCRIBE_REQUESTS";
	public static final String CNTR_ERM_FAILED_UNSUBSCRIBE_REQUESTS = "CNTR_ERM_FAILED_UNSUBSCRIBE_REQUESTS";
	public static final String CNTR_ERM_SUCCESSFUL_SUBSCRIBE_REQUESTS = "CNTR_ERM_SUCCESSFUL_SUBSCRIBE_REQUESTS";
	public static final String CNTR_ERM_TOTAL_UNSUBSCRIBE_REQUESTS = "CNTR_ERM_TOTAL_UNSUBSCRIBE_REQUESTS";
	public static final String CNTR_ERM_SUCCESSFUL_UNSUBSCRIBE_REQUESTS = "CNTR_ERM_SUCCESSFUL_UNSUBSCRIBE_REQUESTS";
	public static final String CNTR_ERM_TOTAL_EVENT_REQUESTS_SEND = "CNTR_ERM_TOTAL_EVENT_REQUESTS_SEND";
	public static final String CNTR_ERM_SUCCESS_EVENT_REQUESTS_SEND = "CNTR_ERM_SUCCESS_EVENT_REQUESTS_SEND";
	public static final String CNTR_ERM_FAILED_EVENT_REQUESTS_SEND = "CNTR_ERM_FAILED_EVENT_REQUESTS_SEND";
	public static final String CNTR_ERM_INVALID_EVENT_REQUESTS_SEND = "CNTR_ERM_INVALID_EVENT_REQUESTS_SEND";
	public static final String CNTR_ERM_TOTAL_EVENT_ACK_REQUESTS_SEND = "CNTR_ERM_TOTAL_EVENT_ACK_REQUESTS_SEND";
	public static final String CNTR_ERM_SUCCESS_EVENT_ACK_REQUESTS_SEND = "CNTR_ERM_SUCCESS_EVENT_ACK_REQUESTS_SEND";
	public static final String CNTR_ERM_INVALID_EVENT_ACK_REQUESTS_SEND = "CNTR_ERM_INVALID_EVENT_ACK_REQUESTS_SEND";
	public static final String CNTR_ERM_FAILED_EVENT_ACK_REQUESTS_SEND = "CNTR_ERM_FAILED_EVENT_ACK_REQUESTS_SEND";

	public static final String SALESINR = "salesINR";
	public static final String SALEQUANTITY = "saleQuantity";
	public static final String PRODUCTWEIGHT = "productWeight";
	public static final String JIOPHONE = "JioPhone";
	public static final String MIFI = "MIFI";
	public static final String RECHARGE = "Recharge";
	public static final String MNP = "MNP";
	public static final String TOKENS = "Token";

	// http response
	public static final String RESPONSE_BODY = "app-data";
	public static final String RESPONSE_MESSAGE = "error-message";
	public static final String RESPONSE_CODE = "statusCode";
	public static final String CREATED_TIME_STAMP = "timeStamp";

	// API Handler Headers
	public static final String HEADER_USERID = "userId";
	public static final String HEADER_TASKID = "taskId";
	public static final String HEADER_GROUPON = "groupOn";
	public static final String HEADER_MEMBERNAME = "memberName";
	public static final String HEADER_PROJECTNAME = "projectName";
	public static final String HEADER_CURSOR = "cursor";

	// API Handler Methods
	public static final String GET_USERS = "getUsers";
	public static final String GET_USERDETAILSBYID = "getUserDetailsById";
	public static final String GET_TASKS = "getTasks";
	public static final String GET_TASKDETAILSBYID = "getTaskDetailsById";
	public static final String GET_TASKSBYUSER = "getTasksByUser";
	public static final String GET_TASKSBYUSERGROUPED = "getTasksByUserGrouped";
	public static final String GET_TASKSBYGROUPED = "getAllTaskGrouped";
	public static final String GET_PROJECTWITHTASK = "getProjectWithTask";
	public static final String GET_TASKSOFPROJECT = "getTasksOfProject";
	public static final String GET_ALLPROJECTS = "getallProjects";
	public static final String GET_USERTASKCOUNTGROUPEDBY = "getUserTaskCountGrouped";
	public static final String GET_TASKBYUSERGROUPED = "getTaskByUserGrouped";
	public static final String GET_TASKBYUSERID = "getTaskByUserId";
	// API Handler Errors
	public static final String ERROR_METHOD_NOT_FOUND = "method not found";

	// BaseServicePhabricatorImpl methods
	public static final String BASESERVICE_PHAB_METHOD_GET = "get";
	public static final String BASESERVICE_PHAB_METHOD_GETBYID = "getById";
	public static final String BASESERVICE_PHAB_METHOD_GETBYPARAMS = "getByParams";
	public static final String EDGELB_WEBSOCKET = "/EdgeLBWebsocket";

	public static final String DEVOPS_EVENT_ACK_POOL = "DevopsEventACKPool";
	public static final String DEVOPS_CLEAR_CODE_POOL = "DevopsClearCodePool";

	public static final String DUMP_EVENT_TASK_POOL = "EventDumpTask";
	public static final String DUMP_EVENT_PROCESS_TASK_POOL = "DumpEventProcessTask";

	public static final String UOL_PRODUCER_TASK_POOL = "UOLProducerTask";

	// for Protocol
	public static final String PROTOCOL = "protocol";
	public static final String HTTPS_PROTOCOL = "httpsPort";

	// alarm constants
	public static final String ALARM_NAME_FIELD = "alarm_name";
	public static final String ALARM_TS_FIELD = "time_stamp";
	public static final String ALARM_COUNT_FIELD = "occurrence_count";
	public static final String ALARMMANAGERINDEXNAME = "alarm_" + OamClientCacheManager.getInstance().getComponentId();
	public static final int INDEX_MAX_RESULT_WINDOW = 25000;
	public static final String DELETED = "DELETED";
	public static final String CREATED = "CREATED";

	public static final String PARAMETER_TYPES = "Parameter Type";
	public static final String PARAMETER_VALIDATOR = "Parameter Validator";
	public static final String PARAMETER_NAME = "Parameter Name";
	public static final String CONFIGURABLE = "Startup Configurable";
	public static final String RECORDABLE = "RecordProcessThread";
	public static final String RUNTIMECONFIGURABLE = "Runtime Configurable";
	public static final String PARAMTER_VALUE = "Parameter Value";
	public static final String DESCRIPTION = "Description";
	public static final String PARAMETER_RANGE = "Paramenter Range";
	public static final String COMBOBOX = "Combobox";
	public static final String RESULT = "result";
	public static final String HANDLE = "handle";
	public static final String REQUEST_STATUS_REASON = "REQUEST_STATUS_REASON";
	public static final String CMNCONFIGPARAMHANDLER = "CMNConfigParamHandler";
	public static final String SERVICE = "service";
	public static final String REGISTRYBROADCASTHANDLER = "RegistryBroadcastHandler";
	public static final String ELBCACHEINFORMATIONUPDATE = "elbCacheInformationUpdate";
	public static final String EXECUTING = "Executing [ ";
	public static final String FILENAME = "fileName";
	public static final String FILEPATH = "filePath";

	public static final String FILEUPLOADEDSUCCESSFULLY = "File Uploaded Successfully";
	public static final String ORDERID = "orderId";
	public static final String PRODUCTNAME1 = "productName";
	public static final String ACTUALFILENAME = "actualFileName";
	public static final String VALIDATION = "validation";
	public static final String CUSTOMER_ID = "customerId";
	public static final String PROFILE = "profile";
	public static final String CAFNUMBER = "cafNumber";
	public static final String ELBDMS = "/DMS_MS";
	public static final String SUBSCRIPTIONENGINE = "subscriptionengine_";
	public static final String SUCCESS = "Success";
	public static final String SERVICECOMMUNICATOR = "ServiceCommunicator";
	public static final String SITEID = "siteId";
	public static final String REQUESTTOBEANMAPPER = "RequestToBeanMapper";
	public static final String INVALIDREQUESTPARAMVALUE = "Invalid request parameter value";
	public static final String MONTH = "month";
	public static final String ATROW = " at row [";
	public static final String PARAMETER = "Parameter ";
	public static final String NOTDECLAREDANDDEFINED = "] has not been declared and defined.";
	public static final String POSSIBLEVALUESNOTSUPPLIED = "Possible values not supplied for ";
	public static final String LOADTIBCOINTEGRATIONCONFIGSHEET = "loadTibcoIntegrationConfigSheet";
	public static final String DOCUMENTCATEGORYVALUE = "Please enter document category value";
	public static final String FILEZEROBYTESENDCORRECTFILE = "File size is zero byte, Please send correct file.";
	public static final String ERRORINUPLOADFILE = "Error in uploadFile";
	public static final String UPLOADFILE = "uploadFile";
	public static final String FILEWRITTINGEXCEPTION = "File writting Exception";
	public static final String FILENOTFOUND = "File not found";
	public static final String FILEGOTDELETEDSUCCESSFULLY = "File got deleted successfully";
	public static final String CREATECONNECTIONWITHCLUSTER = "createConnectionWithCluster";
	public static final String NEWLINE = "\n+++++++++++++++++++++++++++++++++";
	public static final String RTJIOOBSERVER = "RtJioObserver";
	public static final String UPDATE = "update";
	public static final String SENDINGREQUEST = "Sending Request ";
	public static final String SETSITEID = "4ff24f00-13ef-106c-a827-9cb6548b78e0";
	public static final String DELIMITER = "/";
	public static final double THOUSAND = 1000;
	public static final String CONFIGURATION = "/configuration/";
	public static final Object STRICTHOSTKEYCONFIGURATION = "StrictHostKeyChecking";
	public static final String HOSTCONNECTED = "Host connected.";
	public static final String SFTPCHANNELCONNECTEDOPENED = "sftp channel opened and connected.";
	public static final String EXCEPTIONWHILETRANSFER = "Exception found while tranfer the response.";
	public static final String SFTPCHANNELEXITED = "sftp Channel exited.";
	public static final String CHANNELDISCONNECTED = "Channel disconnected.";
	public static final String HOSTSESSIONDISCONNECTED = "Host Session disconnected.";
	public static final String PURGING = "Purging";

}
