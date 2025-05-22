package com.jio.crm.dms.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.elastic.search.enums.Levels;
import com.elastic.search.service.Session;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jio.resttalk.service.custom.exceptions.RestTalkInvalidURLException;
import com.jio.resttalk.service.custom.exceptions.RestTalkServerConnectivityError;
import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.resttalk.service.impl.RestTalkManager;
import com.jio.resttalk.service.pojo.RestTalkClient;
import com.jio.resttalk.service.response.RestTalkResponse;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.configurationmanager.ELBConfigParamEnum;
import com.jio.crm.dms.core.BaseEventBean;
import com.jio.crm.dms.core.HttpParamConstants;
import com.jio.crm.dms.core.RequestHeaderEnum;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.telco.framework.pool.PoolingManager;

public class ServiceCommunicator {

	public static final String IDENTIFIER = "inter-service";

	static {
		RestTalkClient restTalkClient = new RestTalkClient();
		restTalkClient.setKeystoreFilePath("./../configuration/client_keystore.jks");
		restTalkClient.setKeyPhrase("123456");
		restTalkClient.setTrustStorePath("./../configuration/client_truststore.jks");
		restTalkClient.setTrustStorePassword("123456");
		restTalkClient.setIdentifier(IDENTIFIER);
		try {
			RestTalkManager.getInstance().addNewHTTPClient(restTalkClient);
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException | IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}

	}

	private ServiceCommunicator() {

	}

	private static RestTalkBuilder getRestTalkBuilder() throws Exception {
		return ((RestTalkBuilder) PoolingManager.getPoolingManager().borrowObject(RestTalkBuilder.class));
	}

	public static BaseEventBean getBaseEventBeanObject(HttpServletRequest httpRequest) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			BaseEventBean eventTracking = new BaseEventBean();
			eventTracking.setBranchId(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_BRANCH_ID.getValue()));
			eventTracking.setEventName(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_EVENT_NAME.getValue()));
			eventTracking
					.setServiceContext(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_SERVICE_CONTEXT.getValue()));
			eventTracking.setFlowId(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_FLOW_ID.getValue()));
			eventTracking.setHopCount(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_HOP_COUNT.getValue()));
			eventTracking.setMessageType(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_MESSAGE_TYPE.getValue()));
			eventTracking.setServiceIp(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_SERVICE_IP.getValue()));
			eventTracking
					.setPublisherName(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_PUBLISHER_NAME.getValue()));

			eventTracking
					.setExecutionType(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_EXECUTION_TYPE.getValue()));

			eventTracking.setTimeStamp(System.currentTimeMillis());
			eventTracking.setRequest(httpRequest);

			// header introduced in 2.1 ELB
			eventTracking.setLbUrl(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue()));
			eventTracking
					.setSocketAddress(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue()));
			eventTracking.setUsername(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue()));

			// header introduced in 2.1 ERM
			eventTracking
					.setErmIdentifier(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_ERM_IDENTIFIER.getValue()));
			eventTracking.setTargetErm(httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_TARGET_ERM.getValue()));

			// adding all query parameters
			Enumeration<String> en = httpRequest.getParameterNames();
			Map<String, String> params = new HashMap<>();

			while (en.hasMoreElements()) {
				String paramName = en.nextElement();
				params.put(paramName, httpRequest.getParameter(paramName));
			}

			eventTracking.setRequestParams(params);

			// adding all headers in headers map
			Enumeration<String> headerKeys = httpRequest.getHeaderNames();
			Map<String, String> headers = new HashMap<>();

			while (headerKeys.hasMoreElements()) {
				String headerName = headerKeys.nextElement();
				headers.put(headerName, httpRequest.getHeader(headerName));
			}

			eventTracking.setRequestHeaders(headers);

			return eventTracking;

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName());
			return null;
		}

	}

	private static Map<String, String> buildHeader(BaseEventBean eventPojo, String serviceName, String eventName) {

		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(HttpParamConstants.FLOW_ID, UUID.randomUUID().toString());
		headerMap.put(HttpParamConstants.BRANCH_ID, eventPojo.getBranchId());
		headerMap.put(HttpParamConstants.EVENT_NAME, eventName);
		headerMap.put(HttpParamConstants.HOP_COUNT, eventPojo.getHopCount());
		headerMap.put(HttpParamConstants.SRC_MS_IP_PORT, eventPojo.getServiceIp());
		headerMap.put(HttpParamConstants.SRC_MS_CONTEXT, serviceName);
		headerMap.put(HttpParamConstants.MSG_TYPE, HttpParamConstants.MSG_TYPE_EVENT_VALUE);
		headerMap.put(HttpParamConstants.PUBLISHER_NAME, eventPojo.getPublisherName());
		headerMap.put(HttpParamConstants.EXECUTION_TYPE_NAME, HttpParamConstants.EXECUTION_TYPE);

		// header introduced in 2.1 ELB
		if (eventPojo.getLbUrl() != null)
			headerMap.put(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue(), eventPojo.getLbUrl());
		if (eventPojo.getSocketAddress() != null)
			headerMap.put(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue(), eventPojo.getSocketAddress());
		if (eventPojo.getUsername() != null)
			headerMap.put(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue(), eventPojo.getUsername());

		return headerMap;
	}

	public static Object get(BaseEventBean eventPojo, MS serviceName, String eventName,
			Map<String, String> queryParam) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			// get builder
			RestTalkBuilder builder = getRestTalkBuilder();

			// build header
			Map<String, String> headerMap = buildHeader(eventPojo, serviceName.name(), eventName);

			// url
			String url = url(eventPojo.getLbUrl(), serviceName.name());

			// set query parameters
			if (queryParam != null) {
				for (String key : queryParam.keySet()) {
					builder.addQueryParam(key, queryParam.get(key));
				}
			}

			// build client
			builder.setEndPointIdentifier(IDENTIFIER).addCustomHeaders(headerMap).Get(url);

			return processRequest(builder);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
	}

	public static Object post(BaseEventBean eventPojo, MS serviceName, String eventName, Map<String, String> queryParam,
			String requestData) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			// get builder
			RestTalkBuilder builder = getRestTalkBuilder();

			// build header
			Map<String, String> headerMap = buildHeader(eventPojo, serviceName.name(), eventName);

			// set query parameters
			if (queryParam != null) {
				for (String key : queryParam.keySet()) {
					builder.addQueryParam(key, queryParam.get(key));
				}
			}

			// url
			String url = url(eventPojo.getLbUrl(), serviceName.name());

			// build client
			builder.setEndPointIdentifier(IDENTIFIER).Post(url).addRequestData(requestData).addCustomHeaders(headerMap);
			return processRequest(builder);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
	}

	public static Object triggerWebHook(BaseEventBean eventPojo, Session session, Class<?> clss, String id,
			String siteId, String createdBy, String type) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			// fetch complete records
			session.setSearchLevel(Levels.COMPLETE);
			Object data = session.getObject(clss, id);

			String eventName = "INVOKE_WEBHOOKS";
			// get builder
			RestTalkBuilder builder = getRestTalkBuilder();

			// build header
			Map<String, String> headerMap = buildHeader(eventPojo, MS.INTEGRATIONS_MS.name(), eventName);

			// url
			String url = url(eventPojo.getLbUrl(), MS.INTEGRATIONS_MS.name());

			Map<String, Object> requestData = new HashMap<>();

			requestData.put(Constants.SITEID, siteId);
			requestData.put("content", data);
			requestData.put("createdBy", createdBy);
			requestData.put("type", type);

			// build client
			builder.setEndPointIdentifier(IDENTIFIER).Post(url)
					.addRequestData(
							ObjectMapperHelper.getInstance().getEntityObjectMapper().writeValueAsString(requestData))
					.addCustomHeaders(headerMap);

			return processRequest(builder);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
	}

	public static Object triggerWebHook(BaseEventBean eventPojo, String siteId, Object data, String createdBy,
			String type) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			String eventName = "INVOKE_WEBHOOKS";
			// get builder
			RestTalkBuilder builder = getRestTalkBuilder();

			// build header
			Map<String, String> headerMap = buildHeader(eventPojo, MS.INTEGRATIONS_MS.name(), eventName);

			// url
			String url = url(eventPojo.getLbUrl(), MS.INTEGRATIONS_MS.name());

			Map<String, Object> requestData = new HashMap<>();

			requestData.put(Constants.SITEID, siteId);
			requestData.put("content", data);
			requestData.put("createdBy", createdBy);
			requestData.put("type", type);

			// build client
			builder.setEndPointIdentifier(IDENTIFIER).Post(url)
					.addRequestData(
							ObjectMapperHelper.getInstance().getEntityObjectMapper().writeValueAsString(requestData))
					.addCustomHeaders(headerMap);

			return processRequest(builder);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
	}

	public static Object logActivity(BaseEventBean eventPojo, String siteId, String subscriberId, String name,
			String description) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			String eventName = "SUBSCRIBER_ACTIVITY";
			// get builder
			RestTalkBuilder builder = getRestTalkBuilder();

			// build header
			Map<String, String> headerMap = buildHeader(eventPojo, MS.SUBSCRIBERS_MS.name(), eventName);

			// url
			String url = url(eventPojo.getLbUrl(), MS.SUBSCRIBERS_MS.name());

			Map<String, Object> requestData = new HashMap<>();

			requestData.put(Constants.SITEID, siteId);
			requestData.put("subscriberId", subscriberId);
			requestData.put("name", name);
			requestData.put("description", description);

			// build client
			builder.setEndPointIdentifier(IDENTIFIER).Post(url)
					.addRequestData(
							ObjectMapperHelper.getInstance().getEntityObjectMapper().writeValueAsString(requestData))
					.addCustomHeaders(headerMap);

			return processRequest(builder);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
	}

	public static Object put(BaseEventBean eventPojo, MS serviceName, String eventName, Map<String, String> queryParam,
			String requestData) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			// get builder
			RestTalkBuilder builder = getRestTalkBuilder();

			// build header
			Map<String, String> headerMap = buildHeader(eventPojo, serviceName.name(), eventName);

			// set query parameters
			if (queryParam != null) {
				for (String key : queryParam.keySet()) {
					builder.addQueryParam(key, queryParam.get(key));
				}
			}

			// url
			String url = url(eventPojo.getLbUrl(), serviceName.name());

			// build client
			builder.setEndPointIdentifier(IDENTIFIER).Put(url).addCustomHeaders(headerMap).addRequestData(requestData);

			return processRequest(builder);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
	}

	public static Object delete(BaseEventBean eventPojo, MS serviceName, String eventName,
			Map<String, String> queryParam, String requestData) {

		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			// get builder
			RestTalkBuilder builder = getRestTalkBuilder();

			// build header
			Map<String, String> headerMap = buildHeader(eventPojo, serviceName.name(), eventName);

			// set query parameters
			if (queryParam != null) {
				for (String key : queryParam.keySet()) {
					builder.addQueryParam(key, queryParam.get(key));
				}
			}

			// url
			String url = url(eventPojo.getLbUrl(), serviceName.name());

			// build client
			builder.setEndPointIdentifier(IDENTIFIER).Delete(url).addCustomHeaders(headerMap)
					.addRequestData(requestData);

			return processRequest(builder);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
	}

	private static Object processRequest(RestTalkBuilder builder) {
		RestTalkResponse res;
		JsonObject jsonData = null;
		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + Constants.SERVICECOMMUNICATOR + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			res = builder.send();
			jsonData = new JsonParser().parse(res.answeredContent().responseString()).getAsJsonObject();
			if (jsonData.get("body") != null)
				jsonData = (JsonObject) jsonData.get("body");
		} catch (RestTalkInvalidURLException | RestTalkServerConnectivityError e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.SERVICECOMMUNICATOR, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return e.getMessage();
		}
		return jsonData;
	}

	public static String url(String elbUrl, String serviceName) {

		String protocol = ConfigParamsEnum.ELB_HOSTED_PROTOCOL.getStringValue();
		String port = ConfigParamsEnum.ELB_HOSTED_PORT.getStringValue();
		String ip = ConfigParamsEnum.ELB_HOSTED_IP.getStringValue();

		// url
		String requestURI = "http://" + elbUrl + "/" + serviceName;

		if (protocol != null && ip != null && port != null) {
			requestURI = protocol + "://" + ip + ":" + port + "/" + serviceName;
		}

		return requestURI;

	}

	public static String elbInfo() {
		String protocol = ConfigParamsEnum.ELB_HOSTED_PROTOCOL.getStringValue();
		String port = ConfigParamsEnum.ELB_HOSTED_PORT.getStringValue();
		String ip = ConfigParamsEnum.ELB_HOSTED_IP.getStringValue();
		// url
		return protocol + "://" + ip + ":" + port;

	}

	public static String elbDmsInfo() {
		String protocol = ELBConfigParamEnum.ELB_DMS_MS.getProtocol();
		String port = Integer.toString(ELBConfigParamEnum.ELB_DMS_MS.getPort());
		String ip = ELBConfigParamEnum.ELB_DMS_MS.getIp();
		// url
		return protocol + "://" + ip + ":" + port;

	}

	public static String gethttpsDMSELBInfo() {
		String protocol = ELBConfigParamEnum.ELB_DMS_MS_SECURE.getProtocol();
		String port = Integer.toString(ELBConfigParamEnum.ELB_DMS_MS_SECURE.getPort());
		String ip = ELBConfigParamEnum.ELB_DMS_MS_SECURE.getIp();
		// url
		return protocol + "://" + ip + ":" + port;

	}
}