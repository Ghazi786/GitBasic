package com.jio.crm.dms.clicommands;

import org.apache.http.HttpStatus;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLICommandExecutionException;
import com.jio.crm.dms.cli.DMSMSCLIOptionEnum;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSGetParamCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {

		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		String paramName = null;
		try {
			paramName = cliData.getRequest().getHeader(DMSMSCLIOptionEnum.NAME.getName());
			
			ConfigParamsEnum configEnum = ConfigParamsEnum
					.getEnumFromCliName(paramName);
			if (configEnum == null) {
				printInvalidOptionValueMessage(DMSMSCLIOptionEnum.NAME.getName(),
						paramName);
				throw new DMSMSCLICommandExecutionException("Invalid value '"
						+ paramName + "' for option '"
						+ DMSMSCLIOptionEnum.NAME.getName() + "'",
						HttpStatus.SC_BAD_REQUEST);
			}
			return configEnum.getStringValue();
		} catch (Exception e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
			.getExceptionLogBuilder(e, "get param error", this.getClass().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeExceptionLog();
			printJsonParsingException(e);
			throw new DMSMSCLICommandExecutionException("Error parsing JSON",
					HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		
	}

}
