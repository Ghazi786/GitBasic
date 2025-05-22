package com.jio.crm.dms.notification;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.notification.exceptions.ContactNumberMissingException;

/**
 * this thread class is used to send SMS on mobile number
 * 
 * @author Kiran.Jangid
 *
 */

public class SMSSendorThread implements Runnable {

	private String mobileNumber;
	private String message;
	private String smsUrl;

	public SMSSendorThread(String mobileNumber, String message) {
		super();
		this.mobileNumber = mobileNumber;
		this.message = message;
		setSmsUrl();
	}

	@Override
	public void run() {

		CloseableHttpClient httpClient = null;
		try {

			if (this.mobileNumber == null || !checkValidity(this.mobileNumber)) {
				throw new ContactNumberMissingException("Mobile Number is Invalid or Missing");
			}

			String productInstance = ConfigParamsEnum.HTTP_STACK_HOST_IP.getStringValue() + ":"
					+ ConfigParamsEnum.HTTP_STACK_PORT.getIntValue();
			httpClient = HttpClients.createDefault();
			HttpPost uploadFile = new HttpPost(this.smsUrl);
			uploadFile.addHeader("product_name", NotificationServiceConfigEnum.PRODUCT_NAME.getStringValue());
			uploadFile.addHeader("product_instance", productInstance);
			uploadFile.addHeader("mobile_number", this.mobileNumber);
			uploadFile.addHeader("message", this.message);
			uploadFile.addHeader("access_key", NotificationService.getInstance().getAccessKey());
			CloseableHttpResponse response;

			response = httpClient.execute(uploadFile);

			if (response.getStatusLine().getStatusCode() == 200) {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("SMS Send Successfully").writeLog();
			} else {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getErrorLogBuilder("SMS Sending Failure : " + response.getStatusLine().getStatusCode(),
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}

		} catch (IOException | ContactNumberMissingException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder("SMS Send Failure",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		} finally {
			try {
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}
		}

	}

	private boolean checkValidity(String mobileNumber) {
		Pattern p = Pattern.compile("(0/91)?[7-9][0-9]{9}");
		Matcher m = p.matcher(mobileNumber);
		return (m.find() && m.group().equals(mobileNumber));
	}

	public String getSmsUrl() {
		return smsUrl;
	}

	public void setSmsUrl() {
		this.smsUrl = "http://" + NotificationServiceConfigEnum.SERVICE_IP.getStringValue() + ":"
				+ NotificationServiceConfigEnum.SERVICE_PORT.getIntValue()
				+ NotificationServiceConfigEnum.SERVICE_CONTEXT.getStringValue() + "?"
				+ NotificationServiceConfigEnum.MAIL_SERVICE_OPERATION_SEND_SMS.getValue();
	}

}
