package com.jio.crm.dms.utils;

import com.jio.crm.dms.logger.DappLoggerService;

public class NumberUtil {

	public static int stringToNumber(String number) {

		try {

			if (number == null)
				return 0;

			return Integer.parseInt(number);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(), "",
					Thread.currentThread().getStackTrace()[1].getMethodName()).writeExceptionLog();
		}

		return 0;
	}

	private NumberUtil() {
		super();

	}

}
