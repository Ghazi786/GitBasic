package com.jio.crm.dms.cli;

import com.jio.crm.dms.logger.DappLoggerService;

public abstract class DMSMSAbstractCLICommand {

	protected void printInvalidOptionValueMessage(String optionName, String optionValue) {
		
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"InvalidOptionValue from CLI " + "optionName : " + optionName + "   " + "optionValue : " + optionValue,
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
	}

	protected void printJsonParsingException(Exception e) {
		DappLoggerService.GENERAL_ERROR_FAILURE_LOG
		.getExceptionLogBuilder(e, "JsonParsingException from CLI ", this.getClass().getName(),
				Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeExceptionLog();
	}

	protected void doNothing(Exception e) {
		DappLoggerService.GENERAL_ERROR_FAILURE_LOG
		.getExceptionLogBuilder(e, "doNothing ", this.getClass().getName(),
				Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeExceptionLog();
	}

	/**
	 * executes the CLI command and returns its output
	 * 
	 * @param cliData
	 *            input JSON object
	 * @return the output of the command (if applicable)
	 * 
	 */
	public abstract String execute(DMSMSCLIPojo cliData);

	void setCommandName(String commandName) {
	}
	
}
