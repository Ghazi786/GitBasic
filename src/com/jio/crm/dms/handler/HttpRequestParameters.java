package com.jio.crm.dms.handler;

public class HttpRequestParameters {

	public static final String GET_ALL_TRANSACTION_DATA = "getAllTransactionData";
	public static final String GET_ALL_SYSTEM_TRANSACTION = "getAllSystemTransaction";
	public static final String APPROVE_TRANSACTION = "approvetransaction";
	public static final String REJECTS_TRANSACTION = "rejectstransaction";
	public static final String CREATE_TRANSACTION = "createTransaction";
	public static final String ACTION = "action";

	public static final String HEADER_TENANT_ID = "tenant-id";
	public static final String HEADER_NETWORK_ID = "X-NetworkId";

	public static final String APP_DATA = "appData";
	public static final String COMMAND_NAME = "commandName";
	public static final String CONTENT_TYPE_JSON = "text/Json";
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	public static final String TRUE = "true";
	public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
	public static final String ALLOW_METHODS = "GET, POST , PUT, OPTIONS, DELETE";

	private HttpRequestParameters() {
		super();
	}

}
