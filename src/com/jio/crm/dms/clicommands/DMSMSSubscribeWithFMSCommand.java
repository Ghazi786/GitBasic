package com.jio.crm.dms.clicommands;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.exceptions.InvalidRequestParamValueException;
import com.jio.crm.dms.fms.FMSIntegration;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSSubscribeWithFMSCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
		try {
			FMSIntegration.getInstance().registerWithFMS();
			
			
		} catch (InvalidRequestParamValueException e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
			.getExceptionLogBuilder(e, "subscribe with FMS error", this.getClass().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeExceptionLog();
			
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			 Thread.currentThread().interrupt();
		}
		return Boolean.toString(InterfaceStatus.status.isFmsIntegrationConfigurationModule()).toUpperCase();
		
	}

}
