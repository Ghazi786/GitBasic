package com.jio.crm.dms.clicommands;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.notification.NotificationServiceConfigEnum;

public class DMSMSNotificationHubInfoCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
		 
		StringBuilder sbr = new StringBuilder();
		
		sbr.append("------------------------------------------\n");
		sbr.append("Name : NH info ");
		sbr.append("\nIP : ");
		sbr.append(NotificationServiceConfigEnum.SERVICE_IP.getStringValue());
		sbr.append("\nPORT : ");
		sbr.append(NotificationServiceConfigEnum.SERVICE_PORT.getStringValue());
		sbr.append("\n------------------------------------------\n");
		
		return sbr.toString();
	}

}
