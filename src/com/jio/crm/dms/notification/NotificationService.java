package com.jio.crm.dms.notification;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.alarmmanager.AlarmNameIntf;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.resttalk.service.custom.exceptions.RestTalkInvalidURLException;
import com.jio.resttalk.service.custom.exceptions.RestTalkServerConnectivityError;
import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.resttalk.service.impl.RestTalkManager;
import com.jio.resttalk.service.pojo.RestTalkClient;
import com.jio.resttalk.service.response.RestTalkResponse;
import com.jio.telco.framework.pool.PoolingManager;

/**
 * 
 * This class is used for Notification service. it initialize notification
 * service and configuration
 * 
 * 
 * @author Kiran.Jangid
 *
 */

public class NotificationService {

	private static NotificationService mailService = new NotificationService();
	private String mailUrl;
	private String accessKey;
	private ExecutorService mailExecutor = Executors.newFixedThreadPool(4);
	private ExecutorService smsExecutor = Executors.newFixedThreadPool(4);

	private NotificationService() {
	}

	public static NotificationService getInstance() {
		if (mailService == null) {
			mailService = new NotificationService();
		}
		return mailService;
	}

	/**
	 * to initialize notification service with configuration stored in
	 * {@link NotificationServiceConfigEnum}
	 * 
	 * @return
	 */

	public boolean init() {

		try {
			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Initialising Notification Services ... ").writeLog();
			setClientConfiguration();
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException | IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			return false;
		}

		try {
			buildRestTalkBuilderForMailNotification();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			return false;
		}

		try {
			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Subscribing Marketplace to Notification Service ... ")
					.writeLog();
			subscribeMSForNotificationHub();
		} catch (RestTalkInvalidURLException | RestTalkServerConnectivityError e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			// to run local host comment this line .
			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(AlarmNameIntf.NH_REGISTRATION_FAILED,
						AlarmNameIntf.NH_REGISTRATION_FAILED);
			}
			return false;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			// to run local host comment this line .
			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(AlarmNameIntf.NH_REGISTRATION_FAILED,
						AlarmNameIntf.NH_REGISTRATION_FAILED);
			}
			return false;
		}

		// to run local host comment this line .

		String profile = System.getProperty(Constants.PROFILE);
		if (profile == null || !profile.equals("dev")) {
			OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(AlarmNameIntf.NH_REGISTRATION_SUCCESS,
					AlarmNameIntf.NH_REGISTRATION_SUCCESS);
		}

		return true;
	}

	/**
	 * this method subscribe marketplace project to mail service
	 * 
	 * @throws Exception
	 */

	private void subscribeMSForNotificationHub() throws Exception {

		RestTalkBuilder builder = ((RestTalkBuilder) PoolingManager.getPoolingManager()
				.borrowObject(RestTalkBuilder.class));

		String productInstance = ConfigParamsEnum.HTTP_STACK_HOST_IP.getStringValue() + ":"
				+ ConfigParamsEnum.HTTP_STACK_PORT.getIntValue();

		// adding header for subscribe MS to mail server
		builder.addCustomHeader("product_name", NotificationServiceConfigEnum.PRODUCT_NAME.getStringValue());
		builder.addCustomHeader("product_instance", productInstance);

		// adding end point and context to subscribe ms
		builder.Get(this.mailUrl + "?" + NotificationServiceConfigEnum.MAIL_SERVICE_OPERATION_SUBSCRIBE_MS.getValue());

		// sending subscription request
		RestTalkResponse response = builder.send();

		this.accessKey = response.getResponsHeaders().get("access_key");

		DappLoggerService.GENERAL_INFO_LOG
				.getInfoLogBuilder(
						"Marketplace Subscribed with Notification Service ( Access Key = " + this.accessKey + " )")
				.writeLog();

	}

	private void buildRestTalkBuilderForMailNotification() {
		this.mailUrl = "http://" + NotificationServiceConfigEnum.SERVICE_IP.getStringValue() + ":"
				+ NotificationServiceConfigEnum.SERVICE_PORT.getIntValue()
				+ NotificationServiceConfigEnum.SERVICE_CONTEXT.getStringValue();
	}

	private void setClientConfiguration() throws KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {

		RestTalkClient restTalkClient = new RestTalkClient();

		if (NotificationServiceConfigEnum.IS_SERVICE_SECURE.getBooleanValue()) {
			restTalkClient.setKeystoreFilePath(NotificationServiceConfigEnum.SERVICE_KEYSTORE.getStringValue());
			restTalkClient.setKeyPhrase(NotificationServiceConfigEnum.SERVICE_KEYPHRASE.getStringValue());
			restTalkClient.setTrustStorePath(NotificationServiceConfigEnum.SERVICE_TRUST_STORE_PATH.getStringValue());
			restTalkClient.setTrustStorePassword(NotificationServiceConfigEnum.TRUST_STORE_PASSWORD.getStringValue());
			restTalkClient.setIdentifier(NotificationServiceConfigEnum.SERVICE_IDENTIFIER.getStringValue());
			RestTalkManager.getInstance().addNewHTTPClient(restTalkClient);
		}
	}

	public boolean sendMail(List<String> toList, List<String> ccList, String subject, String content, String signature,
			String note, File file) throws Exception {
		this.mailExecutor.execute(new MailSendorThread(toList, ccList, subject, content, signature, note, file));
		return true;
	}

	public boolean sendMailWithListOfAttachments(List<String> toList, List<String> ccList, String subject,
			String content, String signature, String note, List<File> files) throws Exception {
		this.mailExecutor.execute(new MailSendorThread(toList, ccList, subject, content, signature, note, files));
		return true;
	}

	public boolean sendSMS(String mobileNumber, String message) throws Exception {
		this.smsExecutor.execute(new SMSSendorThread(mobileNumber, message));
		return true;
	}

	public String getAccessKey() {
		return this.accessKey;
	}

	public String getMailAndSMSUrl() {
		return this.mailUrl;
	}

	public void unRegisterWithNH() {

		this.accessKey = null;
		InterfaceStatus.status.setNotificationConfigurationModule(false);

	}

	void sendSampleMail() {
		// setting up to send invoice in mail to subscriber
		final List<String> toList = new ArrayList<>();
		toList.add("ashish14.gupta@ril.com");

		final String subject = "Jio MarketPlace";

		final String mailBody = "Jio MarketPlace - Your Invoice for Plan : 200";

		try {
			NotificationService.getInstance().sendMail(toList, toList, subject, mailBody, null, null, null);
		} catch (final Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
	}

}
