package com.jio.crm.dms.node.es;

import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;

public interface ESConstants {

	// Connection
	public String ES_XPACK_SECURITY_PROPERTY = "xpack.security.user";
	

	// Creating Index
	String INDEX_DOC_LIMIT = "index.max_result_window";
	long INDEX_DOC_LIMIT_VALUE = 250000;

	String RECORD_INDEX_NAME = ConfigParamsEnum.ES_INDEXNAME.getStringValue();
	public String RECORD_INDEX_TYPE = ConfigParamsEnum.ES_TYPENAME.getStringValue();
	String EVENT_NAME = "eventName";
	String ES_CLUSTER_NAME = "cluster.name";
	String HA_TYPE ="typehaintegration";

	// Maping
	String DYNAMIC = "dynamic";
	String PROPERTIES = "properties";
	String KEYWORD_TYPE = "keyword";
	String TYPE = "type";
	String INTEGER = "integer";

	String PUBLISHER_NAME = "publisherName";
	String MS_FQDN = "publisherFQDN";
	String MS_CONTEXT = "Context";

	String MODE_OF_NOTIFICATION = "modeOfNotification";
	String ACKNOWLEDGED = "acknowledged";
	String IS_DEFFEREDENABLE = "isDefferedEnable";
	String IS_NOTIFICATIONENABLE = "isNotificationEnable";
	String EVENT_ACK_TIMEOUT = "eventAckTimeout";
	String SUBSCRIBERS = "subscribers";
	String DESCRIPTION = "description";
	String BOOLEAN = "boolean";
	String ERM_DEFFERED_EVENT = "erm_deffered_event";
	String ERM_DEFFERED_EVENT_TYPE = "erm_deffered_event_type";

	String ERM_ARCHIVE_EVENT = "erm_archive_event";
	String ERM_ARCHIVE_EVENT_TYPE = "erm_archive_event_type";

	String ERM_TIMER_STATUS = "erm_timer_status";
	String ERM_TIMER_STATUS_TYPE = "erm_timer_status_type";
	String DEVENTSTATUS = "deventstatus";

	String CREATED = "CREATED";
	String UPDATED = "UPDATED";
	String DELETED = "DELETED";
	String NESTED_TYPE = "nested";
	String SUBSRIBER_FQDN_NAME = "fqdn";
	String SUBSUCRIBER_CONTEXT = "context";
	String MICROSERVICE_NAME = "microServiceName";

	String EVENT_STATUS_TYPE = "eventstatus_type";
	String EVENT_STATUS = "eventstatus";
	String STATUS = "status";
	String MSELBINFO_INDEX_NAME = "ms_elb_info";
	String MSELBINFO_INDEX_TYPE = "ms_elb_info_type";

	public String BY_EVENT_NAME = "eventname";
	public String BY_MS_NAME = "msname";
	public String BY_EVENT_TYPE = "eventtype";
	public String BY_FLOW_ID = "flowid";
	public String BY_DEFFERED_DATE = "deffereddate";

	// archive events
	public String TIME_STAMP = "timeStamp";
	public long ONE_DAY = 86400000;// one day in milliseconds

	// mapping variable

	public String RANK = "rank";
	public String LONG = "long";
	public String NAME = "name";
	public String PRM_ID = "prm_id";
	public String PARENT_PRM_ID = "parent_prm_id";
	public String CIRCLE = "circle";
	public String CITY = "city";
	public String JIO_POINT = "jio_point";
	public String JIOCENTRE = "jio_centre";
	public String NO_OF_SALE = "no_of_sale";
	public String TOLTAL_AMOUNT = "total_amount";
	public String PRODUCTTYPE = "product_type";
	public String PARENT_NAME = "parent_name";
	public String DATE = "date";

	public String CACHE_INDEX = "cacheindex";

	public String CACHE_TYPE = "cachetype";

	public String TOKEN_INDEX_TYPE = "token";

	public String TOKENASSIGNED = "tokenassigned";

}
