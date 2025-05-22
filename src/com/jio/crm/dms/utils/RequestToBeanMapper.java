package com.jio.crm.dms.utils;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.exceptions.InvalidRequestParamValueException;
import com.jio.crm.dms.logger.DappLoggerService;

/**
 * Utility is responsible for populating the request params into mentioned class
 * bean. If error occurs while populating request params into bean it throws
 * InvalidRequestParamValueException
 * 
 * @author Samrudhi.Gandhe
 *
 */
public class RequestToBeanMapper {

	public static <T> T getBeanFromRequest(final HttpServletRequest req, final Class<T> clazz)
			throws InvalidRequestParamValueException {
		String json;
		try {
			json = IOUtils.toString(req.getReader());
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Executing [ json: " + json + Constants.REQUESTTOBEANMAPPER + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							Constants.REQUESTTOBEANMAPPER, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			return ObjectMapperHelper.getInstance().getEntityObjectMapper().readValue(json, clazz);
		} catch (final IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), Constants.REQUESTTOBEANMAPPER, "getBeanFromRequest")
					.writeExceptionLog();
			throw new InvalidRequestParamValueException(422, Constants.INVALIDREQUESTPARAMVALUE);
		}
	}

	public static <T> T getBeanFromString(final String json, final Class<T> clazz)
			throws InvalidRequestParamValueException {
		try {
			return ObjectMapperHelper.getInstance().getEntityObjectMapper().readValue(json, clazz);
		} catch (final IOException e) {
			throw new InvalidRequestParamValueException(422, Constants.INVALIDREQUESTPARAMVALUE);
		}
	}

	public static BaseResponse<?> getErrorResponse(final int errorCode, final String message) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final BaseResponse<?> response = new BaseResponse(null, false, message, errorCode, null);
		return response;
	}

	public static String getCookie(final HttpServletRequest req, String key) {
		if (req.getCookies() != null) {
			for (Cookie cookie : req.getCookies()) {
				if (cookie.getName().equals(key))
					return cookie.getValue();
			}
		}
		return null;
	}

	public static String getSiteId(final HttpServletRequest req) {
		return req.getHeader("site-id");
	}

	public static String getVendorId(final HttpServletRequest req) {
		return req.getHeader("vendor-id");
	}

	public static void setCookie(final HttpServletResponse res, String value, String key) {
		Cookie cookie = new Cookie(key, value);
		res.addCookie(cookie);

	}

	public static String getSubscriberId(final HttpServletRequest req) {
		return req.getHeader("X-Subscriber-Id");
	}

	public static String getJsonFromBean(Object obj) throws InvalidRequestParamValueException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = null;
		try {
			json = ow.writeValueAsString(obj);
		} catch (JsonProcessingException e) {

			throw new InvalidRequestParamValueException(422, Constants.INVALIDREQUESTPARAMVALUE);
		}

		return json;
	}

	public static <T> T getDataFromRequest(final HttpServletRequest req) throws InvalidRequestParamValueException {
		String json;
		try {
			json = IOUtils.toString(req.getReader());
			return ObjectMapperHelper.getInstance().getEntityObjectMapper().readValue(json,
					new TypeReference<Map<String, String>>() {
					});
		} catch (final IOException e) {
			throw new InvalidRequestParamValueException(422, Constants.INVALIDREQUESTPARAMVALUE);
		}
	}

	private RequestToBeanMapper() {
		super();
	}

}
