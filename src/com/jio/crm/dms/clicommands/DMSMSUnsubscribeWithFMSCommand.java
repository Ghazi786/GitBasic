package com.jio.crm.dms.clicommands;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.fms.FMSIntegration;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSUnsubscribeWithFMSCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {
		
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();

		FMSIntegration.getInstance().unSubscribeWithFMS();
		
		if(!InterfaceStatus.status.isFmsIntegrationConfigurationModule())
			return Boolean.toString(true).toUpperCase();
		else
			return Boolean.toString(false).toUpperCase();

	}

}
