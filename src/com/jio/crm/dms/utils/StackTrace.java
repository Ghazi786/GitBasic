package com.jio.crm.dms.utils;

import com.jio.crm.dms.logger.DappLoggerService;

/**
 * this class used to print message to console for debugging purpose
 * 
 * @author Arun2.Maurya
 *
 */
public class StackTrace {

	/**
	 * 
	 * @param msg
	 */
	public static void printToConsole(String msg) {
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + msg + "." + Thread.currentThread().getStackTrace()[1].getMethodName() + " ]", msg,
				Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
	}

	private StackTrace() {
		super();

	}

}
