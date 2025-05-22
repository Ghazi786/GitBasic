package com.jio.crm.dms.fms;

import java.util.HashMap;
import java.util.Map;

import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.exceptions.InvalidRequestParamValueException;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.notification.RestRequestSenderThread;
import com.jio.crm.dms.notification.RestResponse;
import com.jio.crm.dms.notification.RestResponseListener;
import com.jio.crm.dms.utils.MS;
import com.jio.crm.dms.utils.RequestToBeanMapper;
import com.jio.crm.dms.utils.ServiceCommunicator;

/**
 * This is singleton class that will be used to integrate with FMS Service
 * 
 * @author Kiran.Jangid
 *
 */

public class FMSIntegration {

	private static FMSIntegration fmsIntegration;

	private String authKey;

	private FMSIntegration() {
	}

	public static FMSIntegration getInstance() {
		if (fmsIntegration == null) {
			fmsIntegration = new FMSIntegration();
		}
		return fmsIntegration;
	}

	/**
	 * to register CRM with FMS service
	 * 
	 * @throws InvalidRequestParamValueException
	 */

	public void registerWithFMS() throws InvalidRequestParamValueException {
		@SuppressWarnings("unchecked")
		RestRequestSenderThread reqSender = new RestRequestSenderThread(
				FMSRestClientConfig.post(FMSIntegrationConstants.CONTEXT_SUBSCRIBE_FMS), null,
				buildHeaderForSubscribe(), buildRequestBodyForSubscribe(), null, new RestResponseListener() {

					@Override
					public void onFailure(Exception e) {
						DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
								.writeExceptionLog();
					}

					@Override
					public void onComplete(RestResponse response) {

						DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
								response.getStatus() + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
								.writeLog();
						DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
								response.getAppData() + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
								.writeLog();

						if (response.getStatus() == 200) {
							try {
								HashMap<String, String> map = RequestToBeanMapper
										.getBeanFromString(response.getAppData(), HashMap.class);
								setAuthKey(map.get(FMSIntegrationConstants.AUTH_KEY));
								InterfaceStatus.status.setFmsIntegrationConfigurationModule(true);
							} catch (InvalidRequestParamValueException e) {
								DappLoggerService.GENERAL_ERROR_FAILURE_LOG
										.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(),
												Thread.currentThread().getStackTrace()[1].getMethodName())
										.writeExceptionLog();
							}
						}

					}
				});

		DmsBootStrapper.getInstance().getThreadPoolService().execute(reqSender);
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	private String buildRequestBodyForSubscribe() throws InvalidRequestParamValueException {
		Map<String, String> subscriberFMS = new HashMap<>();
		subscriberFMS.put(FMSIntegrationConstants.SUBSCRIBE_NAME, DmsBootStrapper.getInstance().getComponentId());
		subscriberFMS.put(FMSIntegrationConstants.SUBSCRIBE_ID, DmsBootStrapper.getInstance().getComponentId());
		return RequestToBeanMapper.getJsonFromBean(subscriberFMS);
	}

	@SuppressWarnings("rawtypes")
	private Map buildHeaderForSubscribe() throws InvalidRequestParamValueException {
		Map<String, String> subscriberFMS = new HashMap<>();
		subscriberFMS.put(FMSIntegrationConstants.X_FLOW_ID, DmsBootStrapper.getInstance().getComponentId());
		return subscriberFMS;
	}

	public Map<String, String> buildHeaderForSubscribeForWMPO() {
		Map<String, String> subscriberFMS = new HashMap<>();
		subscriberFMS.put(FMSIntegrationConstants.X_FLOW_ID, DmsBootStrapper.getInstance().getComponentId());
		subscriberFMS.put(FMSIntegrationConstants.IS_MILESTONE, FMSIntegrationConstants.FALSE);
		subscriberFMS.put(FMSIntegrationConstants.X_EVENT_NAME, FMSIntegrationConstants.EVENT_NAME);
		subscriberFMS.put(FMSIntegrationConstants.X_MESSAGE_TYPE, FMSIntegrationConstants.EVENT);
		subscriberFMS.put(FMSIntegrationConstants.AUTH_KEY_WMPO, FMSIntegration.getInstance().getAuthKey());
		subscriberFMS.put(FMSIntegrationConstants.TYPE, FMSIntegrationConstants.IOT);
		subscriberFMS.put(FMSIntegrationConstants.RE_PUSH_ALL, FMSIntegrationConstants.FALSE);
		subscriberFMS.put(FMSIntegrationConstants.RE_PUSH, FMSIntegrationConstants.FALSE);
		subscriberFMS.put("Callback-URL", ServiceCommunicator.elbInfo() + "/" + MS.CRM_MS.name());

		return subscriberFMS;
	}

	public void unSubscribeWithFMS() {
		setAuthKey(null);
		InterfaceStatus.status.setFmsIntegrationConfigurationModule(false);
	}

}
