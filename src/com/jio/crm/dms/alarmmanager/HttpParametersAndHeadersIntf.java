package com.jio.crm.dms.alarmmanager;

/**
 * 
 * @author Milindkumar.S
 *
 */

public interface HttpParametersAndHeadersIntf {

	public static final String PARAMETER_ACTION = "action";

	public static final String PARAMETER_TYPE = "type";

	public static final String PARAMETER_RESET = "reset";

	public static final String PARAMETER_PARAM = "param";

	public static final String PARAMETER_PARAM_NAME = "paramname";

	public static final String PARAMETER_VALUE = "value";

	public static final String PARAMETER_PARAM_VALUE = "paramvalue";

	public static final String PARAMETER_LEVEL = "level";

	public static final String PARAMETER_ID = "id";

	public static final String PARAMETER_CATEGORY = "category";

	public static final String PARAMETER_TABLENAME = "tablename";

	public static final String PARAMETER_INDEX = "index";

	public static final String HEADER_TEMPLATE_REQUEST_ID = "applyTemplateRequestId";

	public static final String HEADER_TEMPLATE_REQUEST_STATUS = "applyTemplateRequestStatus";

	public static final String HEADER_TEMPLATE_REQUEST_STATUS_REASON = "applyTemplateRequestStatusReason";

	public static final String HEADER_TEMPLATE_REQUEST_TIMESTAMP = "applyTemplateRequestTimestamp";

	public static final String HEADER_TEMPLATE_PRODUCT_VERSION = "applyTemplateRequestNodeVersion";

	public static final String HEADER_PRODUCT_VERSION = "product_version";

	public static final String HEADER_URL = "url";

	public static final String HEADER_IP = "ip";

	public static final String HEADER_PORT = "port";

	public static final String HEADER_CATEGORY = "category";

	public static final String HEADER_TIMESTAMP = "timestamp";

	public static final String TAG_HTTP = "http://";

	public static final String PARAMETER_LEVEL_VALUE_MINOR = "Minor";

	public static final String PARAMETER_ACTION_VALUE_BULK = "bulk";

	public static final String PARAMETER_CATEGORY_VALUE_TABULAR = "Tabular";

	public static final String PARAMETER_ACTION_VALUE_SETCONFIGPARAM = "setConfigParam";

	public static final String PARAMETER_ACTION_VALUE_GETCONFIGPARAM = "getConfigParam";

	public static final String PARAMETER_ACTION_VALUE_ADDCONFIGPARAM = "addConfigParam";

	public static final String PARAMETER_ACTION_VALUE_DELETECONFIGPARAM = "deleteConfigParam";

	public static final String PARAMETER_ACTION_VALUE_GETCOUNTERS = "getCounters";

	public static final String PARAMETER_ACTION_VALUE_GETCOUNTERCATEGORY = "getCounterCategory";

	public static final String COMPONENT_TYPE_CRM_MS = "CRM_MS";

	public static final String TEMPLATE_REQUEST_STATUS_INITIATED = "initiated";
	public static final String PARAMETER_FILEPATH = "filepath";

	public final String SCALARPARAM = "scalarparam";
	public final String SCALARVALUE = "scalarvalue";

	public static final String ATOMCONTROLLER = "atomController";

	public static final String BSS = "bss";

	public static final String RESETTRUE = "1";
	public static final String RESESTFALSE = "0";

}
