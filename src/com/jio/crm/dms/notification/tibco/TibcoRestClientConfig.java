package com.jio.crm.dms.notification.tibco;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.resttalk.service.impl.RestTalkManager;
import com.jio.resttalk.service.pojo.RestTalkClient;
import com.jio.telco.framework.pool.PoolingManager;

public class TibcoRestClientConfig {

	private static RestTalkBuilder builder;

	private static String url;

	static {

		RestTalkClient restTalkClient = new RestTalkClient();

		if (TibcoConfigEnum.IS_SERVICE_SECURE.getBooleanValue()) {
			restTalkClient.setKeystoreFilePath(TibcoConfigEnum.SERVICE_KEYSTORE.getStringValue());
			restTalkClient.setKeyPhrase(TibcoConfigEnum.SERVICE_KEYPHRASE.getStringValue());
			restTalkClient.setTrustStorePath(TibcoConfigEnum.SERVICE_TRUST_STORE_PATH.getStringValue());
			restTalkClient.setTrustStorePassword(TibcoConfigEnum.TRUST_STORE_PASSWORD.getStringValue());
		}

		restTalkClient.setIdentifier(TibcoConfigEnum.SERVICE_IDENTIFIER.getStringValue());

		try {
			RestTalkManager.getInstance().addNewHTTPClient(restTalkClient);
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException | IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

		try {
			buildTibcoRestClientBuilder();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
	}

	private static void buildTibcoRestClientBuilder() throws Exception {

		if (TibcoConfigEnum.IS_SERVICE_SECURE.getBooleanValue()) {
			url = "https://";
		} else {
			url = "http://";
		}

		url = url + TibcoConfigEnum.SERVICE_URL.getStringValue() + TibcoConfigEnum.SERVICE_CONTEXT.getStringValue();

		builder = ((RestTalkBuilder) PoolingManager.getPoolingManager().borrowObject(RestTalkBuilder.class));
		builder.setEndPointIdentifier(TibcoConfigEnum.SERVICE_IDENTIFIER.getStringValue());

	}

	public static RestTalkBuilder get() {
		return builder.Get(url);
	}

	public static RestTalkBuilder post() {
		return builder.Post(url);
	}

	public static RestTalkBuilder put() {
		return builder.Put(url);
	}

	public static RestTalkBuilder delete() {
		return builder.Delete(url);
	}

	private TibcoRestClientConfig() {
		super();

	}

}
