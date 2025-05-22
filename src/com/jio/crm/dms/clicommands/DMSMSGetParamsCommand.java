package com.jio.crm.dms.clicommands;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSGetParamsCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
		StringBuilder paramDump = new StringBuilder();
		paramDump.append("    List of Configurable Parameters\n").append("######################################\n\n");
		String value;
		String emmtyStr = "<Empty>";
		for (ConfigParamsEnum paramEnum : ConfigParamsEnum.values()) {
			if (paramEnum.getCliArg() == null)
				continue;
			value = paramEnum.getStringValue();
			paramDump.append(String.format("%s = ", paramEnum.name())
					+ ((value == null || value.isEmpty()) ? emmtyStr : value) + "\n");
		}
		return new String(paramDump);
	}
	
}
