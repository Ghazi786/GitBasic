package com.jio.crm.dms.alarmmanager;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.rjil.management.alarm.AlarmLoggingCallbackHandler;


/**
 * 
 * @author Milindkumar.S
 *
 */

public class AlarmLoggingListener implements AlarmLoggingCallbackHandler {

	@Override
	public void debug(String arg0, String arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public void error(String arg0, String arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public void fatal(String arg0, String arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public void info(String arg0, String arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public void trace(String arg0, String arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public void warn(String arg0, String arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

}
