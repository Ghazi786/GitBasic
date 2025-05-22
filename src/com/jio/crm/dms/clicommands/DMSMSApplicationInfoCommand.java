package com.jio.crm.dms.clicommands;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSApplicationInfoCommand extends DMSMSAbstractCLICommand {

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
		sbr.append("Name    : DMS_MS \nVersion : ");
		sbr.append(OamClientManager.getInstance().getOAMClientParam(
				"productversion"));
		sbr.append("\nID      : ");
		sbr.append(OamClientManager.getInstance().getOAMClientParam("id"));
		sbr.append("\nHostName: ");
		sbr.append(OamClientManager.getInstance().getOAMClientParam("hostname"));
		sbr.append("\nIP      : ");
		sbr.append(ConfigParamsEnum.HTTP_STACK_HOST_IP.getStringValue());
		sbr.append("\nPort    : ");
		sbr.append(ConfigParamsEnum.HTTP_STACK_PORT.getStringValue());
		sbr.append("\n------------------------------------------\n");
		return sbr.toString();
		
	}

}
