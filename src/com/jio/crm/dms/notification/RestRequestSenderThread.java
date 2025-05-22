package com.jio.crm.dms.notification;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.resttalk.service.custom.exceptions.RestTalkInvalidURLException;
import com.jio.resttalk.service.custom.exceptions.RestTalkServerConnectivityError;
import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.resttalk.service.response.RestTalkResponse;

/**
 * Thread to send rest request. it will use rest talk builder
 * {@link RestTalkBuilder}
 * 
 * @author Kiran.Jangid
 *
 */

public class RestRequestSenderThread implements Runnable {

	private RestTalkBuilder builder;
	private String msgBody;
	private String identifier;
	private Map<String, String> queryParams;
	private Map<String, String> headers;
	private RestResponseListener responseListener;

	public RestRequestSenderThread(RestTalkBuilder builder, Map<String, String> queryParams,
			Map<String, String> headers, String msgBody, String identifier, RestResponseListener responseListener) {
		this.builder = builder;
		this.msgBody = msgBody;
		this.identifier = identifier;
		this.queryParams = queryParams;
		this.headers = headers;
		this.responseListener = responseListener;
	}

	private void sendEvent() {

		RestTalkResponse response;

		try {

			// logger
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.SENDINGREQUEST + this.identifier + ", Method : " + this.builder.getMethod() + "| Url : "
							+ this.builder.getUrl(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			// adding parameter
			if (this.queryParams != null) {
				Iterator<Entry<String, String>> params = this.queryParams.entrySet().iterator();
				while (params.hasNext()) {
					Map.Entry<String, String> entry = params.next();
					this.builder.addQueryParam(entry.getKey(), entry.getValue());
				}
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(Constants.SENDINGREQUEST + this.identifier + "| Params : " + this.queryParams,
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}

			// adding headers
			if (this.headers != null) {
				this.builder.addCustomHeaders(this.headers);
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(Constants.SENDINGREQUEST + this.identifier + "| Headers : " + this.headers,
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}

			// adding body
			if (this.msgBody != null) {
				this.builder.addRequestData(this.msgBody);
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(Constants.SENDINGREQUEST + this.identifier + "| Body : " + this.msgBody,
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}

			this.builder.addCustomHeader("identifier", this.identifier);

			// getting response
			response = this.builder.send();

			CounterNameEnum.CNTR_SEND_REST_REQUEST.increment();

			if (response.getHttpStatusCode() == 200) {
				CounterNameEnum.CNTR_SEND_REST_SUCCESS.increment();
				RestResponse res = new RestResponse(200, null, response.answeredContent().responseString());
				this.responseListener.onComplete(res);
			} else if (response.getHttpStatusCode() == 202) {
				CounterNameEnum.CNTR_SEND_REST_ACCEPTED.increment();
				RestResponse res = new RestResponse(202, null, response.answeredContent().responseString());
				this.responseListener.onComplete(res);
			} else {
				CounterNameEnum.CNTR_SEND_REST_FAILURE.increment();
				RestResponse res = new RestResponse(response.getHttpStatusCode(), null,
						response.answeredContent().responseString());
				this.responseListener.onComplete(res);
			}

		} catch (RestTalkInvalidURLException | RestTalkServerConnectivityError e) {
			CounterNameEnum.CNTR_SEND_REST_FAILURE.increment();
			this.responseListener.onFailure(e);
		} catch (Exception e) {
			CounterNameEnum.CNTR_SEND_REST_FAILURE.increment();
			this.responseListener.onFailure(e);
		}

	}

	@Override
	public void run() {
		sendEvent();
	}

}
