package com.jio.crm.dms.core;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.jio.crm.dms.logger.DappLoggerService;

/**
 * 
 * @author barun.rai
 *
 */

public class BaseEventBean {

	private String flowId;
	private String branchId;
	private String hopCount;
	private String eventName;
	private String messageType;
	private String serviceIp;
	private String serviceContext;
	private String publisherName;
	private Map<String, String> requestParams;
	private Map<String, String> requestHeaders;
	private JSONObject requestJson;
	private byte[] requestStream;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ProcessListner listener;
	private long timeStamp;
	private static final String HEADERS = "headers";
	private static final String QUERY_PARAMETERS = "parameters";
	private static final String REQUEST_BODY = "data";
	private static final String REQUEST_HEADER = "request_headers";

	// header introduced in 2.1 ELB
	private String parentBranchId;
	private String lbUrl;
	private String socketAddress;
	private String username;

	// header introduced in 2.1 ERM
	private String ermIdentifier;
	private String targetErm;

	// to make stric call
	private String executionType;
	
	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public String getBranchId() {
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	public String getHopCount() {
		return hopCount;
	}

	public void setHopCount(String hopCount) {
		this.hopCount = hopCount;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getServiceIp() {
		return serviceIp;
	}

	public void setServiceIp(String serviceIp) {
		this.serviceIp = serviceIp;
	}

	public String getServiceContext() {
		return serviceContext;
	}

	public void setServiceContext(String serviceContext) {
		this.serviceContext = serviceContext;
	}

	public JSONObject getRequestJson() {
		return requestJson;
	}

	public void setRequestJson(JSONObject requestJson) {
		this.requestJson = requestJson;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public Map<String, String> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(Map<String, String> requestParams) {
		this.requestParams = requestParams;
	}

	public byte[] getRequestStream() {
		return requestStream;
	}

	public void setRequestStream(byte[] requestStream) {
		this.requestStream = requestStream;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	public ProcessListner getListener() {
		return listener;
	}

	public void setListener(ProcessListner listener) {
		this.listener = listener;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public String toString() {

		try {

			JSONObject headers = new JSONObject();

			// v1.0 ERM
			headers.put(RequestHeaderEnum.HEADER_NAME_BRANCH_ID.getValue(), this.getBranchId());
			headers.put(RequestHeaderEnum.HEADER_NAME_EVENT_NAME.getValue(), this.getEventName());
			headers.put(RequestHeaderEnum.HEADER_NAME_SERVICE_CONTEXT.getValue(), this.getServiceContext());
			headers.put(RequestHeaderEnum.HEADER_NAME_FLOW_ID.getValue(), this.getFlowId());
			headers.put(RequestHeaderEnum.HEADER_NAME_HOP_COUNT.getValue(), this.getHopCount());
			headers.put(RequestHeaderEnum.HEADER_NAME_MESSAGE_TYPE.getValue(), this.getMessageType());
			headers.put(RequestHeaderEnum.HEADER_NAME_SERVICE_IP.getValue(), this.getServiceIp());
			headers.put(RequestHeaderEnum.HEADER_NAME_PUBLISHER_NAME.getValue(), this.getPublisherName());
			headers.put(RequestHeaderEnum.HEADER_NAME_EXECUTION_TYPE.getValue(), this.getExecutionType());

			// v2.1 ELB
			if (this.getLbUrl() != null)
				headers.put(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue(), this.getLbUrl());
			if (this.getSocketAddress() != null)
				headers.put(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue(), this.getSocketAddress());
			if (this.getUsername() != null)
				headers.put(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue(), this.getUsername());

			// v2.1 ERM
			if (this.getErmIdentifier() != null)
				headers.put(RequestHeaderEnum.HEADER_NAME_ERM_IDENTIFIER.getValue(), this.getErmIdentifier());
			if (this.getTargetErm() != null)
				headers.put(RequestHeaderEnum.HEADER_NAME_TARGET_ERM.getValue(), this.getTargetErm());

			JSONObject eventDataStr = new JSONObject();
			eventDataStr.put(HEADERS, headers);

			if (this.getRequestJson() != null) {
				JSONObject data = new JSONObject(this.getRequestJson());
				eventDataStr.put(REQUEST_BODY, data);
			}

			if (this.getRequestParams() != null) {
				JSONObject param = new JSONObject(this.getRequestParams());
				eventDataStr.put(QUERY_PARAMETERS, param);
			}

			if (this.getRequestHeaders() != null) {
				JSONObject headersTemp = new JSONObject(this.getRequestHeaders());
				eventDataStr.put(REQUEST_HEADER, headersTemp);
			}

			return eventDataStr.toString();

		} catch (Exception e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilderWithFlowID(e, "Error in converting event pojo to json string :", this.flowId, this.getClass().getName(), "toString");
			return "";
		}
	}

	/**
	 * 
	 * Method to create DevopsEvent from dump ES data
	 * 
	 * @param eventDataStr
	 * @return
	 * @throws JSONException
	 */

	public BaseEventBean getEventPojo(String eventDataStr) throws JSONException {

		BaseEventBean eventData = new BaseEventBean();

		// Complete Event Data Str
		JSONObject eventJson = new JSONObject(eventDataStr);

		// Getting header, query parameter and Body
		JSONObject headersStr = eventJson.getJSONObject(HEADERS);

		eventData.setEventName(headersStr.getString(RequestHeaderEnum.HEADER_NAME_EVENT_NAME.getValue()));
		eventData.setFlowId(headersStr.getString(RequestHeaderEnum.HEADER_NAME_FLOW_ID.getValue()));
		eventData.setBranchId(headersStr.getString(RequestHeaderEnum.HEADER_NAME_BRANCH_ID.getValue()));
		eventData.setPublisherName(headersStr.getString(RequestHeaderEnum.HEADER_NAME_PUBLISHER_NAME.getValue()));
		eventData.setServiceContext(headersStr.getString(RequestHeaderEnum.HEADER_NAME_SERVICE_CONTEXT.getValue()));
		eventData.setServiceIp(headersStr.getString(RequestHeaderEnum.HEADER_NAME_SERVICE_IP.getValue()));
		eventData.setHopCount(headersStr.getString(RequestHeaderEnum.HEADER_NAME_HOP_COUNT.getValue()));
		eventData.setMessageType(headersStr.getString(RequestHeaderEnum.HEADER_NAME_MESSAGE_TYPE.getValue()));
		eventData.setExecutionType(headersStr.getString(RequestHeaderEnum.HEADER_NAME_EXECUTION_TYPE.getValue()));


		// header introduced in 2.1 ELB
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue()))
			eventData.setLbUrl(headersStr.getString(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue()));
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue()))
			eventData.setSocketAddress(headersStr.getString(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue()));
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue()))
			eventData.setUsername(headersStr.getString(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue()));

		// header introduced in 2.1 ERM
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_ERM_IDENTIFIER.getValue()))
			eventData.setErmIdentifier(headersStr.getString(RequestHeaderEnum.HEADER_NAME_ERM_IDENTIFIER.getValue()));
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_TARGET_ERM.getValue()))
			eventData.setTargetErm(headersStr.getString(RequestHeaderEnum.HEADER_NAME_TARGET_ERM.getValue()));

		if (eventJson.has(QUERY_PARAMETERS)) {
			JSONObject parametersStr = eventJson.getJSONObject(QUERY_PARAMETERS);
			Gson gson = new Gson();
			Map<String, String> params = gson.fromJson(parametersStr.toString(), Map.class);
			eventData.setRequestParams(params);
		}

		if (eventJson.has(REQUEST_HEADER)) {
			JSONObject parametersStr = eventJson.getJSONObject(REQUEST_HEADER);
			Gson gson = new Gson();
			Map<String, String> params = gson.fromJson(parametersStr.toString(), Map.class);
			eventData.setRequestHeaders(params);
		}

		if (eventJson.has(REQUEST_BODY)) {
			JSONObject datasStr = eventJson.getJSONObject(REQUEST_BODY);
			eventData.setRequestJson(datasStr);
		}

		return eventData;

	}

	/**
	 * 
	 * @param eventDataStr
	 * @return
	 * @throws JSONException
	 */

	public void buildEventPojo(String eventDataStr) throws JSONException {

		// Complete Event Data Str
		JSONObject eventJson = new JSONObject(eventDataStr);

		// Getting header, query parameter and Body
		JSONObject headersStr = eventJson.getJSONObject(HEADERS);

		this.setEventName(headersStr.getString(RequestHeaderEnum.HEADER_NAME_EVENT_NAME.getValue()));
		this.setFlowId(headersStr.getString(RequestHeaderEnum.HEADER_NAME_FLOW_ID.getValue()));
		this.setBranchId(headersStr.getString(RequestHeaderEnum.HEADER_NAME_BRANCH_ID.getValue()));
		this.setPublisherName(headersStr.getString(RequestHeaderEnum.HEADER_NAME_PUBLISHER_NAME.getValue()));
		this.setServiceContext(headersStr.getString(RequestHeaderEnum.HEADER_NAME_SERVICE_CONTEXT.getValue()));
		this.setServiceIp(headersStr.getString(RequestHeaderEnum.HEADER_NAME_SERVICE_IP.getValue()));
		this.setHopCount(headersStr.getString(RequestHeaderEnum.HEADER_NAME_HOP_COUNT.getValue()));
		this.setMessageType(headersStr.getString(RequestHeaderEnum.HEADER_NAME_MESSAGE_TYPE.getValue()));
		this.setExecutionType(headersStr.getString(RequestHeaderEnum.HEADER_NAME_EXECUTION_TYPE.getValue()));

		// header introduced in 2.1 ELB
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue()))
			this.setLbUrl(headersStr.getString(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue()));
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue()))
			this.setSocketAddress(headersStr.getString(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue()));
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue()))
			this.setUsername(headersStr.getString(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue()));

		// header introduced in 2.1 ERM
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_ERM_IDENTIFIER.getValue()))
			this.setErmIdentifier(headersStr.getString(RequestHeaderEnum.HEADER_NAME_ERM_IDENTIFIER.getValue()));
		if (headersStr.has(RequestHeaderEnum.HEADER_NAME_TARGET_ERM.getValue()))
			this.setTargetErm(headersStr.getString(RequestHeaderEnum.HEADER_NAME_TARGET_ERM.getValue()));

		if (eventJson.has(QUERY_PARAMETERS)) {
			JSONObject parametersStr = eventJson.getJSONObject(QUERY_PARAMETERS);
			Gson gson = new Gson();
			Map<String, String> params = gson.fromJson(parametersStr.toString(), Map.class);
			this.setRequestParams(params);
		}

		if (eventJson.has(REQUEST_HEADER)) {
			JSONObject parametersStr = eventJson.getJSONObject(REQUEST_HEADER);
			Gson gson = new Gson();
			Map<String, String> params = gson.fromJson(parametersStr.toString(), Map.class);
			this.setRequestHeaders(params);
		}

		if (eventJson.has(REQUEST_BODY)) {
			JSONObject datasStr = eventJson.getJSONObject(REQUEST_BODY);
			this.setRequestJson(datasStr);
		}

	}

	/**
	 * @return the parentBranchId
	 */
	public String getParentBranchId() {
		return parentBranchId;
	}

	/**
	 * @param parentBranchId
	 *            the parentBranchId to set
	 */
	public void setParentBranchId(String parentBranchId) {
		this.parentBranchId = parentBranchId;
	}

	/**
	 * @return the lbUrl
	 */
	public String getLbUrl() {
		return lbUrl;
	}

	/**
	 * @param lbUrl
	 *            the lbUrl to set
	 */
	public void setLbUrl(String lbUrl) {
		this.lbUrl = lbUrl;
	}

	/**
	 * @return the socketAddress
	 */
	public String getSocketAddress() {
		return socketAddress;
	}

	/**
	 * @param socketAddress
	 *            the socketAddress to set
	 */
	public void setSocketAddress(String socketAddress) {
		this.socketAddress = socketAddress;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the targetErm
	 */
	public String getTargetErm() {
		return targetErm;
	}

	/**
	 * @param targetErm
	 *            the targetErm to set
	 */
	public void setTargetErm(String targetErm) {
		this.targetErm = targetErm;
	}

	/**
	 * @return the ermIdentifier
	 */
	public String getErmIdentifier() {
		return ermIdentifier;
	}

	/**
	 * @param ermIdentifier
	 *            the ermIdentifier to set
	 */
	public void setErmIdentifier(String ermIdentifier) {
		this.ermIdentifier = ermIdentifier;
	}

	/**
	 * @return the requestHeaders
	 */
	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	/**
	 * @param requestHeaders
	 *            the requestHeaders to set
	 */
	public void setRequestHeaders(Map<String, String> requestHeaders) {
		this.requestHeaders = requestHeaders;
	}

	public String getExecutionType() {
		return executionType;
	}

	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}
}
