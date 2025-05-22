package com.jio.crm.dms.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jio.crm.dms.core.BaseEventBean;
import com.jio.crm.dms.core.BaseModal;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.HttpParamConstants;
import com.jio.crm.dms.core.RequestHeaderEnum;
import com.jio.crm.dms.core.RestApiCalls;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;

/**
 * Utility class which will have useful methods to perform processing in Devops
 * 
 * @author Barun.Rai
 *
 */
public class SubscriptionEngineUtils {

	private SubscriptionEngineUtils() {

	}

	/**
	 * A method which will take a list and method as inputs and provide list of
	 * Group
	 * 
	 * @param array
	 * @param method
	 * @return List
	 */
	public static List<Group> groupByMultiple(List<? extends BaseModal> array, I method) {
		List<Group> res = new ArrayList<>();
		array.forEach(o -> {
			String groupName = method.myMethod(o);
			int index = -1;
			if (!res.isEmpty()) {
				Group gr = res.stream().filter(x -> x.getKey().equalsIgnoreCase(groupName)).collect(Collectors.toList())
						.isEmpty() ? null
								: res.stream().filter(x -> x.getKey().equalsIgnoreCase(groupName))
										.collect(Collectors.toList()).get(0);
				index = res.indexOf(gr);
			}

			if (index == -1) {
				Group group = new Group(groupName);
				group.getMembers().add(o);
				res.add(group);
			} else {
				res.get(index).getMembers().add(o);
			}
		});
		return res;
	}

	public static Map<String, List<BaseModal>> groupBy(List<BaseModal> list, I method) {
		Map<String, List<BaseModal>> map = new HashMap<>();
		list.forEach(x -> {
			String groupName = method.myMethod(x);

			if (map.containsKey(groupName)) {
				List<BaseModal> members = map.get(groupName);
				members.add(x);
				map.put(groupName, members);
			} else {
				List<BaseModal> members = new ArrayList<>();
				members.add(x);
				map.put(groupName, members);
			}
		});

		return map;
	}

	/**
	 * Method to extract DevopsEvent object form request
	 * 
	 * @param httpRequest
	 * @return
	 */
	public static BaseEventBean getEventTrackingObject(HttpServletRequest httpRequest) {

		try {

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

			// api caller workaround
			if (httpRequest.getHeader(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue()) == null) {
				String host = httpRequest.getHeader("Host");
				if (host != null) {
					host = host.trim();
					eventTracking.setLbUrl(host);
				}
			}

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
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"Error in converting event pojo to json string :", "DevopsUtils", "getEventTrackingObject");
			return null;
		}

	}

	/**
	 * @param response
	 * @param responseBody
	 * @param status
	 * @param responseMessage
	 * @param cursor
	 */
	public static void sendResponse(BaseEventBean eventPojo, HttpServletResponse response, Object responseBody,
			int status, String responseMessage) {

		try {
			if (responseBody instanceof String) {
				String SEPERATOR = ",";
				String ENCLOSED = "\"";
				String SEPERATOR_COL = ":";

				StringBuilder sb = new StringBuilder();
				sb.append("{");
				if (responseBody != null)
					sb.append(ENCLOSED).append(Constants.RESPONSE_BODY).append(ENCLOSED).append(SEPERATOR_COL)
							.append(responseBody.toString()).append(SEPERATOR);

				sb.append(ENCLOSED).append(Constants.RESPONSE_MESSAGE).append(ENCLOSED).append(SEPERATOR_COL)
						.append(ENCLOSED).append(responseMessage).append(ENCLOSED).append(SEPERATOR);
				sb.append(ENCLOSED).append(Constants.RESPONSE_CODE).append(ENCLOSED).append(SEPERATOR_COL)
						.append(status).append(SEPERATOR);
				sb.append(ENCLOSED).append(Constants.CREATED_TIME_STAMP).append(ENCLOSED).append(SEPERATOR_COL)
						.append(System.currentTimeMillis());
				sb.append("}");
				response.setStatus(status);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				PrintWriter printWriter;
				printWriter = response.getWriter();
				printWriter.print(sb.toString());
				printWriter.flush();
				printWriter.close();
				ESConnection.getInstance().getHaOperation()
						.removeEventData(eventPojo.getFlowId() + "_" + eventPojo.getBranchId());

			} else {

				Map<String, Object> jsonObj = new HashMap<>();
				if (responseBody != null)
					jsonObj.put(Constants.RESPONSE_BODY, responseBody);
				jsonObj.put(Constants.RESPONSE_MESSAGE, responseMessage);
				jsonObj.put(Constants.RESPONSE_CODE, status);
				jsonObj.put(Constants.CREATED_TIME_STAMP, System.currentTimeMillis());

				response.setStatus(status);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				PrintWriter printWriter;
				printWriter = response.getWriter();
				printWriter.print(ObjectMapperHelper.getInstance().getEntityObjectMapper().writeValueAsString(jsonObj));
				printWriter.flush();
				printWriter.close();
			}
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

	}

	/**
	 * 
	 * @param eventPojo
	 * @param appData
	 * @param inputStream
	 */

	public static void sendAsyncResponseToELB(BaseEventBean eventPojo, BaseResponse<?> appData) {

		String methodName = "sendResponseToMS";
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder("Execution Started of " + methodName + " " + eventPojo.toString(),
						SubscriptionEngineUtils.class.getSimpleName(), methodName)
				.writeLog();

		try {

			String data = null;
			if (appData.getData() instanceof String) {
				String SEPERATOR = ",";
				String ENCLOSED = "\"";
				String SEPERATOR_COL = ":";

				StringBuilder sb = new StringBuilder();
				sb.append("{");
				if (appData.getData() != null)
					sb.append(ENCLOSED).append(Constants.RESPONSE_BODY).append(ENCLOSED).append(SEPERATOR_COL)
							.append(appData.getData().toString()).append(SEPERATOR);

				sb.append(ENCLOSED).append(Constants.RESPONSE_MESSAGE).append(ENCLOSED).append(SEPERATOR_COL)
						.append(ENCLOSED).append(appData.getMessage()).append(ENCLOSED).append(SEPERATOR);
				sb.append(ENCLOSED).append(Constants.RESPONSE_CODE).append(ENCLOSED).append(SEPERATOR_COL).append(200)
						.append(SEPERATOR);
				sb.append(ENCLOSED).append(Constants.CREATED_TIME_STAMP).append(ENCLOSED).append(SEPERATOR_COL)
						.append(System.currentTimeMillis());
				sb.append("}");
				data = sb.toString();

			} else {

				Map<String, Object> jsonObj = new HashMap<>();
				if (appData.getData() != null)
					jsonObj.put(Constants.RESPONSE_BODY, appData.getData());
				jsonObj.put(Constants.RESPONSE_MESSAGE, appData.getMessage());
				jsonObj.put(Constants.RESPONSE_CODE, 200);
				jsonObj.put(Constants.CREATED_TIME_STAMP, System.currentTimeMillis());
				jsonObj.put(Constants.RESPONSE_CODE, 200);
				data = ObjectMapperHelper.getInstance().getEntityObjectMapper().writeValueAsString(jsonObj);
			}

			

			Map<String, String> headerMap = new HashMap<>();
			headerMap.put(HttpParamConstants.FLOW_ID, eventPojo.getFlowId());
			headerMap.put(HttpParamConstants.BRANCH_ID, eventPojo.getBranchId());
			headerMap.put(HttpParamConstants.EVENT_NAME, eventPojo.getEventName());
			headerMap.put(HttpParamConstants.HOP_COUNT, eventPojo.getHopCount());
			headerMap.put(HttpParamConstants.SRC_MS_IP_PORT, eventPojo.getServiceIp());
			headerMap.put(HttpParamConstants.SRC_MS_CONTEXT, eventPojo.getServiceContext());
			headerMap.put(HttpParamConstants.MSG_TYPE, HttpParamConstants.MSG_TYPE_EVENT_ACK_VALUE);
			headerMap.put(HttpParamConstants.PUBLISHER_NAME, eventPojo.getPublisherName());

			// header introduced in 2.1 ELB
			if (eventPojo.getLbUrl() != null)
				headerMap.put(RequestHeaderEnum.HEADER_NAME_LB_URL.getValue(), eventPojo.getLbUrl());
			if (eventPojo.getSocketAddress() != null)
				headerMap.put(RequestHeaderEnum.HEADER_NAME_SOCKET_ADDRESS.getValue(), eventPojo.getSocketAddress());
			if (eventPojo.getUsername() != null)
				headerMap.put(RequestHeaderEnum.HEADER_NAME_USERNAME.getValue(), eventPojo.getUsername());

			String requestURI = ServiceCommunicator.url(eventPojo.getLbUrl(), Constants.EDGELB_WEBSOCKET.substring(1));
			RestApiCalls.post(requestURI, data, headerMap);

			// Removing Event from Dump
			ESConnection.getInstance().getHaOperation()
					.removeEventData(eventPojo.getFlowId() + "_" + eventPojo.getBranchId());

		}

		catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(), "",
					Thread.currentThread().getStackTrace()[1].getMethodName()).writeExceptionLog();
		}
	}

}
