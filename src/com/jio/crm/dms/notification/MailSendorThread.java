package com.jio.crm.dms.notification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.jio.resttalk.service.custom.exceptions.RestTalkInvalidURLException;
import com.jio.resttalk.service.custom.exceptions.RestTalkServerConnectivityError;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.notification.exceptions.RecipientMissingException;

public class MailSendorThread implements Runnable {

	private List<String> toList;
	private List<String> ccList;
	private String subject;
	private String content;
	private String signature;
	private String note;
	private File file;
	private List<File> files;
	private MultipartEntityBuilder multiPartbuilder;
	private String mailUrl;

	public MailSendorThread(List<String> toList, List<String> ccList, String subject, String content, String signature,
			String note, File file) {
		super();
		this.toList = toList;
		this.ccList = ccList;
		this.subject = subject;
		this.content = content;
		this.signature = signature;
		this.note = note;
		this.file = file;
		this.files = null;
		setMailUrl();
	}

	public MailSendorThread(List<String> toList, List<String> ccList, String subject, String content, String signature,
			String note, List<File> files) {
		super();
		this.toList = toList;
		this.ccList = ccList;
		this.subject = subject;
		this.content = content;
		this.signature = signature;
		this.note = note;
		this.files = files;
		this.file = null;
		setMailUrl();
	}

	@Override
	public void run() {

		try {

			this.multiPartbuilder = MultipartEntityBuilder.create();
			this.multiPartbuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			addProductInstance();
			addAccessKey();
			addRecipient(this.toList, this.ccList);
			addSubject(this.subject);
			addMailBody(this.content);
			if (this.signature != null) {
				addSignature(signature);
			}
			if (this.note != null) {
				addNote(note);
			}
			if (this.file != null) {
				try {
					attachFile(file);
				} catch (FileNotFoundException e) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
							.writeExceptionLog();

				}
			}
			if (this.files != null) {
				try {
					attachFiles(files);
				} catch (FileNotFoundException e) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
							.writeExceptionLog();

				}
			}

			send();

		} catch (RestTalkInvalidURLException | RestTalkServerConnectivityError|RecipientMissingException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
	}

	private void addProductInstance() {
		String ipAddress = ConfigParamsEnum.HTTP_STACK_HOST_IP.getStringValue() + ":"
				+ ConfigParamsEnum.HTTP_STACK_PORT.getIntValue();
		this.multiPartbuilder.addTextBody("product_instance", ipAddress, ContentType.DEFAULT_TEXT);
	}

	private void addAccessKey() {
		this.multiPartbuilder.addTextBody("access_key", NotificationService.getInstance().getAccessKey(),
				ContentType.DEFAULT_TEXT);
	}

	private void addRecipient(List<String> toList, List<String> ccList) throws RecipientMissingException {

		if (toList == null || toList.isEmpty()) {
			throw new RecipientMissingException("Mail To Recipient is Missing");
		}

		if (ccList == null || ccList.isEmpty()) {
			throw new RecipientMissingException("Mail CC Recipient is Missing");
		}

		String toListStr = "";
		Iterator<String> iterToList = toList.iterator();
		while (iterToList.hasNext()) {
			String string =  iterToList.next();
			if (toListStr.equalsIgnoreCase("")) {
				toListStr = string;
			}
			toListStr = toListStr + "," + string;
		}

		this.multiPartbuilder.addTextBody("toList", toListStr, ContentType.DEFAULT_TEXT);

		String ccListStr = "";
		Iterator<String> iterccList = ccList.iterator();
		while (iterccList.hasNext()) {
			String string =  iterccList.next();
			if (ccListStr.equalsIgnoreCase("")) {
				ccListStr = string;
			}
			ccListStr = ccListStr + "," + string;
		}

		this.multiPartbuilder.addTextBody("ccList", ccListStr, ContentType.TEXT_PLAIN);

	}

	private void addSubject(String subject) {
		this.multiPartbuilder.addTextBody("subject", subject, ContentType.DEFAULT_TEXT);
	}

	private void addMailBody(String body) {
		this.multiPartbuilder.addTextBody("mailBody", body, ContentType.TEXT_HTML);
	}

	private void addSignature(String signature) {
		this.multiPartbuilder.addTextBody("signature", signature, ContentType.TEXT_HTML);
	}

	private void addNote(String note) {
		this.multiPartbuilder.addTextBody("note", note, ContentType.TEXT_HTML);
	}

	private void attachFile(File file) throws FileNotFoundException {
		this.multiPartbuilder.addBinaryBody("Indentifier", new FileInputStream(file),
				ContentType.APPLICATION_OCTET_STREAM, file.getName());
	}

	private void attachFiles(List<File> files) throws FileNotFoundException {
		Iterator<File> iterator = files.iterator();
		while (iterator.hasNext()) {
			File file = iterator.next();
			this.multiPartbuilder.addBinaryBody("Indentifier", new FileInputStream(file),
					ContentType.APPLICATION_OCTET_STREAM, file.getName());
		}
	}

	private void send() throws Exception {
		CloseableHttpClient httpClient = null;
		try {
			httpClient = HttpClients.createDefault();
			HttpPost uploadFile = new HttpPost(this.mailUrl);

			String productInstance = ConfigParamsEnum.HTTP_STACK_HOST_IP.getStringValue() + ":"
					+ ConfigParamsEnum.HTTP_STACK_PORT.getIntValue();

			this.multiPartbuilder.addTextBody("product_name",
					NotificationServiceConfigEnum.PRODUCT_NAME.getStringValue(), ContentType.DEFAULT_TEXT);

			this.multiPartbuilder.addTextBody("product_instance", productInstance, ContentType.DEFAULT_TEXT);

			HttpEntity multipart = this.multiPartbuilder.build();

			uploadFile.setEntity(multipart);

			CloseableHttpResponse response = httpClient.execute(uploadFile);

			if (response.getStatusLine().getStatusCode() == 200) {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Mail Send Successfully").writeLog();
			} else {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder("Mail Send Failure",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}

		}

	}

	public String getMailUrl() {
		return this.mailUrl;
	}

	public void setMailUrl() {
		this.mailUrl = "http://" + NotificationServiceConfigEnum.SERVICE_IP.getStringValue() + ":"
				+ NotificationServiceConfigEnum.SERVICE_PORT.getIntValue()
				+ NotificationServiceConfigEnum.SERVICE_CONTEXT.getStringValue() + "?"
				+ NotificationServiceConfigEnum.MAIL_SERVICE_OPERATION_SEND_SMS.getStringValue();
	}

}
