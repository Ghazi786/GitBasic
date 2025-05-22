package com.jio.crm.dms.kafka;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;

public class ConsumerBootstraper {

	private static ConsumerBootstraper instance;

	private ConsumerBootstraper() {
	}

	public static ConsumerBootstraper getInstance() {
		if (instance == null) {
			instance = new ConsumerBootstraper();
		}
		return instance;
	}

	public void startConsumer() {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}

}
