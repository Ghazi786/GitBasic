package com.jio.crm.dms.core;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jio.blockchain.sdk.constants.Constants;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.resttalk.service.custom.exceptions.RestTalkInvalidURLException;
import com.jio.resttalk.service.custom.exceptions.RestTalkServerConnectivityError;
import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.resttalk.service.impl.RestTalkManager;
import com.jio.resttalk.service.pojo.RestTalkClient;
import com.jio.resttalk.service.response.RestTalkResponse;
import com.jio.telco.framework.pool.PoolingManager;

/**
 * 
 * @author Arun.Tagde
 * 
 */

public class RestApiCalls {
	private static RestTalkBuilder builder;
	static {
		RestTalkClient restTalkClient = new RestTalkClient();
		restTalkClient.setKeystoreFilePath("./../configuration/client_keystore.jks");
		restTalkClient.setKeyPhrase("123456");
		restTalkClient.setTrustStorePath("./../configuration/client_truststore.jks");
		restTalkClient.setTrustStorePassword("123456");
		restTalkClient.setIdentifier(Constants.IDENTIFIER_VALUE);
		try {
			RestTalkManager.getInstance().addNewHTTPClient(restTalkClient);
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException | IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
		try {
			builder = ((RestTalkBuilder) PoolingManager.getPoolingManager().borrowObject(RestTalkBuilder.class));
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
	}

	public static JsonObject get(String url) {

		builder.setEndPointIdentifier(Constants.IDENTIFIER_VALUE).Get(url);

		
		RestTalkResponse res;
		JsonObject jsonData = null;
		try {
			res = builder.send();
			JsonObject jsonObject = new JsonParser().parse(res.answeredContent().responseString()).getAsJsonObject();
			jsonData = (JsonObject) jsonObject.get(com.jio.crm.dms.utils.Constants.RESULT);
		} catch (RestTalkInvalidURLException | RestTalkServerConnectivityError e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}

		return jsonData;

	}

	public static Object post(String url, String requestData, Map<String, String> headers) {

		builder.setEndPointIdentifier(Constants.IDENTIFIER_VALUE).Post(url).addRequestData(requestData)
				.addCustomHeaders(headers);

		RestTalkResponse res;
		JsonObject jsonData = null;
		try {
			res = builder.send();
			JsonObject jsonObject = new JsonParser().parse(res.answeredContent().responseString()).getAsJsonObject();
			jsonData = (JsonObject) jsonObject.get(com.jio.crm.dms.utils.Constants.RESULT);
		} catch (RestTalkInvalidURLException | RestTalkServerConnectivityError e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}

		return jsonData;

	}

	public static Object put(String url, String requestData) {

		builder.setEndPointIdentifier(Constants.IDENTIFIER_VALUE).Put(url).addRequestData(requestData);

		RestTalkResponse res;
		JsonObject jsonData = null;

		try {
			res = builder.send();
			JsonObject jsonObject = new JsonParser().parse(res.answeredContent().responseString()).getAsJsonObject();
			jsonData = (JsonObject) jsonObject.get(com.jio.crm.dms.utils.Constants.RESULT);
		} catch (RestTalkInvalidURLException | RestTalkServerConnectivityError e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}

		return jsonData;

	}

	public static Object delete() {

		JsonObject jsonData = null;

		return jsonData;

	}

	private RestApiCalls() {
		super();
	}

}
