package com.jio.crm.dms.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.elastic.search.config.ElasticConfig;
import com.elastic.search.config.IndexConfig;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.subscriptions.modules.bean.APIKey;
import com.unboundid.util.Base64;

/**
 * @author Alpesh.Sonar
 *
 */
public class EncryptUtils {

	private static EncryptUtils encryptUtils = new EncryptUtils();

	private static final String DEFAULTENCODING = "UTF-8";

	public static EncryptUtils getInstance() {

		return encryptUtils;
	}

	private EncryptUtils() {
	}

	/**
	 * @param text
	 * @return
	 */
	public String base64encode(String text) {
		try {
			return Base64.encode(text.getBytes(DEFAULTENCODING)).replace("\n", "^").replace("\r", "~");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * @param text
	 * @return
	 */
	public String base64decode(String text) {
		try {
			text = text.replace("^", "\n").replace("~", "\r");
			return new String(Base64.decode(text), DEFAULTENCODING);
		} catch (IOException | ParseException e) {
			return null;
		}
	}

	/**
	 * @param siteId
	 * @param vendorId
	 * @param month
	 * @return
	 */
	public String jwtEncode(String siteId, String vendorId, int month) {
		try {

			DateTime now = DateTime.now();
			DateTime sixMonthsLater = now.plusMonths(month);
			Algorithm algorithm = Algorithm.HMAC256("secret");
			return JWT.create().withIssuer(siteId).withIssuedAt(new Date()).withClaim("vendor-id", vendorId)
					.withClaim(Constants.MONTH, month).withExpiresAt(sixMonthsLater.toDate()).sign(algorithm);

		} catch (JWTCreationException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					EncryptUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		return null;
	}

	/**
	 * @param token
	 * @return
	 */
	public Map<String, Object> jwtDecode(String token) {

		Map<String, Object> decode = new HashMap<>();

		try {
			DecodedJWT jwt = JWT.decode(token);
			decode.put(Constants.MONTH, jwt.getClaim(Constants.MONTH).asInt());
			decode.put("vendorId", jwt.getClaim("vendor-id").asString());
			decode.put("siteId", jwt.getIssuer());
			decode.put("expires-at", jwt.getExpiresAt());
			return decode;
		} catch (JWTCreationException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					EncryptUtils.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		return null;
	}

	/**
	 * @param token
	 * @return
	 */
	public String jwtRenew(String token) {
		Map<String, Object> decode = jwtDecode(token);
		return jwtEncode((String) decode.get("siteId"), (String) decode.get("vendorId"),
				Integer.parseInt((String) decode.get(Constants.MONTH)));
	}

	// api key encryptor
	/**
	 * @param data
	 * @return
	 */
	public String apiKeyEncryotor(String data) {
		return base64encode(data);

	}

	// api key decryotor
	/**
	 * @param data
	 * @return
	 */
	public String apiKeyDecryotor(String data) {
		return base64decode(data);

	}

	public static void main(String[] args) throws Exception {

		EncryptUtils encryptUtils = getInstance();

		IndexConfig indexConfig = new IndexConfig();
		indexConfig.setIndexName("dev_subscriptionengine");

		ElasticConfig elasticConfig = new ElasticConfig();
		elasticConfig.setClusterName("development_cluster");
		elasticConfig.setUserName("elastic");
		elasticConfig.setPassword("changeme");
		elasticConfig.setHost("10.64.216.92:9333");

		///// BASE 64 ENCODE
		String base = encryptUtils.base64encode("alpesh.sonar@ril.com:password123");

		APIKey key1 = new APIKey();
		key1.setExpireOn("On changed password");
		key1.setSiteId(Constants.SETSITEID);
		key1.setType("BASIC");
		key1.setKey(base);

		// API KEY
		String vendorId = "2679a600-4f90-106c-a827-9cb6548b78e0";
		String siteId = Constants.SETSITEID;
		String data = siteId + ":" + vendorId;
		String apiKey = encryptUtils.apiKeyEncryotor(data);

		key1 = new APIKey();
		key1.setExpireOn("Never Expire");
		key1.setSiteId(Constants.SETSITEID);
		key1.setType("API_KEY");
		key1.setKey(apiKey);

		// JWT
		String token = encryptUtils.jwtEncode(siteId, vendorId, 1);

		key1 = new APIKey();
		key1.setExpireOn("Expiry date");
		key1.setSiteId(Constants.SETSITEID);
		key1.setType("BEARER");
		key1.setKey(token);

	}
}
