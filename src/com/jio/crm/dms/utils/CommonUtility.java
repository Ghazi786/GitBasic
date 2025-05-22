package com.jio.crm.dms.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import com.jio.crm.dms.cli.DMSMSCLIOptionEnum;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.logger.DappLoggerService;

public class CommonUtility {

	public static DMSMSCLIPojo getCliDataFromRequest(HttpServletRequest httpRequest) {

		try {

			if (httpRequest.getHeader(DMSMSCLIOptionEnum.COMMAND_NAME.getName()) == null) {
				return null;
			}

			DMSMSCLIPojo cliTracking = new DMSMSCLIPojo();
			cliTracking.setCommandName(httpRequest.getHeader(DMSMSCLIOptionEnum.COMMAND_NAME.getName()));
			cliTracking.setStatus(httpRequest.getHeader(DMSMSCLIOptionEnum.STATUS.getName()));
			cliTracking.setModule(httpRequest.getHeader(DMSMSCLIOptionEnum.MODULE.getName()));
			cliTracking.setRequest(httpRequest);

			Enumeration<String> en = httpRequest.getParameterNames();
			Map<String, String> params = new HashMap<>();

			while (en.hasMoreElements()) {
				String paramName = en.nextElement();
				params.put(paramName, httpRequest.getParameter(paramName));
			}

			cliTracking.setRequestParams(params);
			cliTracking.setRequestStream(IOUtils.toByteArray(httpRequest.getInputStream()));

			return cliTracking;

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, "Error in generating cli request data", "CommonUtility",
							Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return null;
		}
	}

	private CommonUtility() {
		super();

	}

}
