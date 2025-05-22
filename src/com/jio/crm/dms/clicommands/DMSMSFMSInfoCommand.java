package com.jio.crm.dms.clicommands;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.fms.FMSIntegrationConfigEnum;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSFMSInfoCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
		String[] s = FMSIntegrationConfigEnum.SERVICE_URL.getStringValue().split(":");
		StringBuilder sbr = new StringBuilder();
		
		sbr.append("------------------------------------------\n");
		sbr.append("FMS info : ");
		sbr.append("\nIP : ");
		sbr.append(s[0]);
		sbr.append("\nPORT : ");
		sbr.append(s[1]);
		sbr.append("\n------------------------------------------\n");
		
		return sbr.toString();
	}

}
