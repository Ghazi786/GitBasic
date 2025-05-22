
package com.jio.crm.dms.handler;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.alarmmanager.HttpParametersAndHeadersIntf;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.configurationmanager.ELBConfigParamEnum;
import com.jio.crm.dms.core.BaseEventBean;
import com.jio.crm.dms.ha.DumpEventProcessTask;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.Constants;

/**
 * This class handle all the Broadcast messages of other Microservices from OAM.
 * This class also handle the Re- registration of TRM with OAM in case of OAM
 * restart.
 *
 */

public class RegistryBroadcastHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doPost(HttpServletRequest httpservletrequest, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			if (httpservletrequest.getMethod().equalsIgnoreCase(HttpMethod.POST.asString())) {

				InputStream inputStream = httpservletrequest.getInputStream();
				String msgBody = IOUtils.toString(inputStream, httpservletrequest.getCharacterEncoding());

				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder("Executing [ " + this.getClass().getName() + "Registry Broadcast msg body : \n"
								+ msgBody + "\n" + Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();

				JSONObject broadcastObj = new JSONObject(msgBody);

				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								"Executing [ " + this.getClass().getName() + "Registry Broadcast broadcastObj : \n"
										+ broadcastObj + "\n"
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();

				// Processing ELB Instance Data
				if (broadcastObj.has(Constants.COMPONENT_TYPE_EDGELB)) {
					elbcacheinformationupdate(msgBody);
					return;
				}

				// Processing USER_SUBSCRIPTION_MS Instance Data
				if (broadcastObj.has(Constants.INTEGRATIONS_MS)) {
					parseboardcastjson(broadcastObj);
					return;
				}

				DmsBootStrapper.getInstance().getDappExecutor().submit(() -> this.elbcacheinformationupdate(msgBody));

			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "doPost").writeExceptionLog();
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {

			if (request.getParameter("operation").equals(Constants.RE_REGISTER)) {

				reRegisterWithOam(request, response);

				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Re Registaration with OAM done Successfully")
						.writeLog();
			} else {

				response.setStatus(SC_BAD_REQUEST);
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "doGet").writeExceptionLog();
		}

	}

	private void reRegisterWithOam(HttpServletRequest request, HttpServletResponse response) throws IOException {

		if (OamClientManager.getInstance().isAlreadyConnectedToOAM()) {
			response.setStatus(SC_BAD_REQUEST);
			return;
		}
		String ip = request.getHeader(Constants.HTTP_IP);
		String port = request.getHeader(Constants.HTTP_PORT);
		String nettyPort = request.getHeader(Constants.NETTY_PORT);

		OamClientManager.getInstance().setOAMDetails(ip, port, nettyPort);
		response.setStatus(SC_OK);
		DmsBootStrapper.getInstance().registerWithOAM();

	}

	private void elbcacheinformationupdate(String broadcastJson) {

		try {

			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("Updating EdgeLB's cache information",
					Constants.REGISTRYBROADCASTHANDLER, Constants.ELBCACHEINFORMATIONUPDATE).writeLog();

			Thread.currentThread().setName("MicroservicName_FQDNMapping_Cache_Update_Thread");
			JSONObject broadcastJsonObj = new JSONObject(broadcastJson);

			if (broadcastJsonObj.has(Constants.COMPONENT_TYPE_EDGELB)) {

				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("COMPONENT_TYPE_EDGELB found in broadcast json ",
						Constants.REGISTRYBROADCASTHANDLER, Constants.ELBCACHEINFORMATIONUPDATE).writeLog();

				JSONArray listOfedgeLBJson = broadcastJsonObj.getJSONArray(Constants.COMPONENT_TYPE_EDGELB);

				for (int edgeLbJson = 0; edgeLbJson < listOfedgeLBJson.length(); edgeLbJson++) {

					JSONObject elbInstanceDetails = listOfedgeLBJson.getJSONObject(edgeLbJson);

					boolean isActiveElb = elbInstanceDetails.getBoolean(Constants.ACTIVE);
					if (!isActiveElb) {
						continue;
					}

					String edgeLBIp = null;
					if (elbInstanceDetails.has(Constants.IP)) {
						edgeLBIp = elbInstanceDetails.getString(Constants.IP);
					} else {
						DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("IP not found in EdgeLB json",
								Constants.REGISTRYBROADCASTHANDLER, Constants.ELBCACHEINFORMATIONUPDATE).writeLog();
					}

					int edgeLBPort = elbInstanceDetails.getInt(Constants.PORT);

					ConfigParamsEnum.ELB_HOSTED_IP.setParamValue(edgeLBIp);
					ConfigParamsEnum.ELB_HOSTED_PORT.setParamValue(String.valueOf(edgeLBPort));
					ConfigParamsEnum.ELB_HOSTED_PROTOCOL.setParamValue("http");

					String protocol = null;
					String ip = null;
					Integer port = null;

					if (elbInstanceDetails.has("optionalfieldname")) {
						JSONArray optionalfieldnameJson = elbInstanceDetails.getJSONArray("optionalfieldname");

						for (int iJson = 0; iJson < optionalfieldnameJson.length(); iJson++) {
							JSONObject optionalFieldJson = optionalfieldnameJson.getJSONObject(iJson);
							if (optionalFieldJson.has(Constants.PROTOCOL)) {
								protocol = optionalFieldJson.getString(Constants.PROTOCOL);
							} else if (optionalFieldJson.has(Constants.IP)) {
								ip = optionalFieldJson.getString(Constants.IP);
							} else if (optionalFieldJson.has(Constants.HTTPSPORT)) {
								port = optionalFieldJson.getInt(Constants.HTTPSPORT);
							}
						}
						if (Constants.HTTPS.equals(protocol) && port != null && ip != null) {
							ConfigParamsEnum.ELB_HOSTED_PORT_SECURE.setParamValue(String.valueOf(port));
							ConfigParamsEnum.ELB_HOSTED_PROTOCOL_SECURE.setParamValue(protocol);
							ConfigParamsEnum.ELB_HOSTED_IP.setParamValue(ip);

						}
					}

					JSONArray subscribeComponentTypeArray = new JSONArray();
					if (elbInstanceDetails.has(Constants.SUBSCRIBE_COMPONENT_TYPE)) {
						subscribeComponentTypeArray = elbInstanceDetails
								.getJSONArray(Constants.SUBSCRIBE_COMPONENT_TYPE);

					} else {
						DappLoggerService.GENERAL_INFO_LOG
								.getLogBuilder("subscribecomponenttype is not found....",
										Constants.REGISTRYBROADCASTHANDLER, Constants.ELBCACHEINFORMATIONUPDATE)
								.writeLog();
					}

					boolean dmsMsFlag = false;

					for (int i = 0; i < subscribeComponentTypeArray.length(); i++) {

						// check for ELB of DMS
						if (subscribeComponentTypeArray.getString(i).equals(Constants.DMS_MS)) {
							dmsMsFlag = true;
						}

						if (subscribeComponentTypeArray.getString(i)
								.equals(HttpParametersAndHeadersIntf.COMPONENT_TYPE_CRM_MS)) {

							DappLoggerService.GENERAL_INFO_LOG
									.getLogBuilder("Storing ERM edgeLb info in cache",
											Constants.REGISTRYBROADCASTHANDLER, Constants.ELBCACHEINFORMATIONUPDATE)
									.writeLog();

						}

					}

					if (dmsMsFlag) {
						DappLoggerService.GENERAL_INFO_LOG
								.getLogBuilder("Storing PSC edgeLb info in cache", Constants.REGISTRYBROADCASTHANDLER,
										"elbCacheInformationUpdate:" + edgeLBIp + ":" + edgeLBPort)
								.writeLog();

						ELBConfigParamEnum.ELB_DMS_MS.setIp(edgeLBIp);
						ELBConfigParamEnum.ELB_DMS_MS.setPort(edgeLBPort);
						ELBConfigParamEnum.ELB_DMS_MS.setProtocol("http");

						if (Constants.HTTPS.equals(protocol) && port != null && ip != null) {

							ELBConfigParamEnum.ELB_DMS_MS_SECURE.setIp(ip);
							ELBConfigParamEnum.ELB_DMS_MS_SECURE.setPort(port);
							ELBConfigParamEnum.ELB_DMS_MS_SECURE.setProtocol(protocol);

						}

					}

				}

			} else {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder("COMPONENT_TYPE_EDGELB not found in broadcast json",
								Constants.REGISTRYBROADCASTHANDLER, Constants.ELBCACHEINFORMATIONUPDATE)
						.writeLog();
			}

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.REGISTRYBROADCASTHANDLER, Constants.ELBCACHEINFORMATIONUPDATE).writeExceptionLog();
		}
	}

	/**
	 * Parsing RMR Broadcast Data
	 * 
	 * @return
	 * @throws JSONException
	 */

	void parseboardcastjson(JSONObject broadCastJson) throws JSONException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder("Inside parseBoardCastJson  : ", this.getClass().getName(), "parseBoardCastJson")
				.writeLog();

		JSONArray msInstanceList = broadCastJson.getJSONArray(Constants.INTEGRATIONS_MS);

		boolean isCallForInstanceDown = false;

		for (int k = 0; k < msInstanceList.length(); k++) {

			JSONObject eObj = msInstanceList.getJSONObject(k);

			boolean activeInstance = eObj.getBoolean(Constants.ACTIVE);

			if (!activeInstance) {
				isCallForInstanceDown = true;
			}

		}

		if (isCallForInstanceDown) {

			int highestPriority = 0;

			for (int k = 0; k < msInstanceList.length(); k++) {

				JSONObject eObj = msInstanceList.getJSONObject(k);

				boolean activeInstance = eObj.getBoolean(Constants.ACTIVE);

				if (!activeInstance) {
					continue;
				}

				int instancePriority = eObj.getInt(Constants.CMN_PRIORITY_DETAILS);

				if (highestPriority < instancePriority) {
					highestPriority = instancePriority;
				}

			}

			for (int k = 0; k < msInstanceList.length(); k++) {

				JSONObject eObj = msInstanceList.getJSONObject(k);

				boolean activeInstance = eObj.getBoolean(Constants.ACTIVE);

				if (!activeInstance) {
					continue;
				}

				int instancePriority = eObj.getInt(Constants.CMN_PRIORITY_DETAILS);

				if ((highestPriority == instancePriority) && eObj.getString(Constants.PARAMETER_ID)
						.equalsIgnoreCase(DmsBootStrapper.getInstance().getComponentId())) {

					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"ID is Processing Dump Queued Event  : " + eObj.getString(Constants.PARAMETER_ID),
							this.getClass().getName(), "parseBoardCastJson").writeLog();

					// Processing Dump Event if Any
					processDumpEvent();
				}

			}

		}

	}

	/**
	 * Method to Process All Dump Event
	 */

	private void processDumpEvent() {

		List<String> eventString = ESConnection.getInstance().getHaOperation().getAllEventData();

		Iterator<String> eventList = eventString.iterator();

		BaseEventBean eventPojo = new BaseEventBean();

		while (eventList.hasNext()) {

			String eventStr = eventList.next();

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder("Processing Data = " + eventStr, this.getClass().getName(), "processDumpEvent")
					.writeLog();

			BaseEventBean eventData;

			try {

				eventData = eventPojo.getEventPojo(eventStr);
				DmsBootStrapper.getInstance().getDappExecutor().execute(new DumpEventProcessTask(eventData));

			} catch (JSONException e) {
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder("Error in parsing Event Json to build Event Pojo " + eventStr,
								this.getClass().getName(), "processDumpEvent")
						.writeLog();
			}

		}

	}
}
