package com.jio.crm.dms.fms;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.resttalk.service.custom.exceptions.RestTalkInvalidURLException;
import com.jio.resttalk.service.custom.exceptions.RestTalkServerConnectivityError;
import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.resttalk.service.impl.RestTalkManager;
import com.jio.resttalk.service.pojo.RestTalkClient;
import com.jio.resttalk.service.response.RestTalkResponse;
import com.jio.telco.framework.pool.PoolingManager;

public class FMSRestClientConfig {

	private static RestTalkBuilder builder;

	private static String url;

	static {

		RestTalkClient restTalkClient = new RestTalkClient();

		if (FMSIntegrationConfigEnum.IS_SERVICE_SECURE.getBooleanValue()) {
			restTalkClient.setKeystoreFilePath(FMSIntegrationConfigEnum.SERVICE_KEYSTORE.getStringValue());
			restTalkClient.setKeyPhrase(FMSIntegrationConfigEnum.SERVICE_KEYPHRASE.getStringValue());
			restTalkClient.setTrustStorePath(FMSIntegrationConfigEnum.SERVICE_TRUST_STORE_PATH.getStringValue());
			restTalkClient.setTrustStorePassword(FMSIntegrationConfigEnum.TRUST_STORE_PASSWORD.getStringValue());
		}

		restTalkClient.setIdentifier(FMSIntegrationConfigEnum.SERVICE_IDENTIFIER.getStringValue());

		try {
			RestTalkManager.getInstance().addNewHTTPClient(restTalkClient);
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}

		try {
			buildTibcoRestClientBuilder();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
	}

	private static void buildTibcoRestClientBuilder() throws Exception {

		if (FMSIntegrationConfigEnum.IS_SERVICE_SECURE.getBooleanValue()) {
			url = "https://";
		} else {
			url = "http://";
		}

		url = url + FMSIntegrationConfigEnum.SERVICE_URL.getStringValue();

		builder = ((RestTalkBuilder) PoolingManager.getPoolingManager().borrowObject(RestTalkBuilder.class));
		builder.setEndPointIdentifier(FMSIntegrationConfigEnum.SERVICE_IDENTIFIER.getStringValue());

	}

	public static RestTalkBuilder get() {
		return builder.Get(url);
	}

	public static RestTalkBuilder post() {
		return builder.Post(url);
	}

	public static RestTalkBuilder post(String contextSubscribeFms) {
		return builder.Post(url + contextSubscribeFms);
	}

	public static RestTalkBuilder put() {
		return builder.Put(url);
	}

	public static RestTalkBuilder delete() {
		return builder.Delete(url);
	}

	public static Object post(String contextSubscribeFms, Map<String, String> headerMap, String requestData)
			throws RestTalkInvalidURLException, RestTalkServerConnectivityError {
		// url
		String requestUrl = url + contextSubscribeFms;

		// build client
		builder.Post(requestUrl).addRequestData(requestData).addCustomHeaders(headerMap);
		return processRequest();
	}

	private static Object processRequest() throws RestTalkInvalidURLException, RestTalkServerConnectivityError {
		RestTalkResponse res;
		JsonObject jsonData = null;
		res = builder.send();
		jsonData = new JsonParser().parse(res.answeredContent().responseString()).getAsJsonObject();
		if (jsonData.get("body") != null)
			jsonData = (JsonObject) jsonData.get("body");
		return jsonData;
	}

	private FMSRestClientConfig() {
		super();
	}

}
