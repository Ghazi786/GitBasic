package com.jio.crm.dms.clicommands;

import org.apache.http.HttpStatus;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLICommandExecutionException;
import com.jio.crm.dms.cli.DMSMSCLIOptionEnum;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.countermanager.CounterManager;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSGetCountersCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {
		
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
		String category = null;

		try {
			category = cliData.getRequest().getHeader(DMSMSCLIOptionEnum.CATEGORY.getName());
			String output = CounterManager.getInstance()
					.fetchCounterDescriptionForSpecificType(category);
			if (output == null) {
				printInvalidOptionValueMessage(
						DMSMSCLIOptionEnum.CATEGORY.getName(), category);
				throw new DMSMSCLICommandExecutionException("Invalid value '"
						+ category + "' for option '"
						+ DMSMSCLIOptionEnum.CATEGORY.getName(),
						HttpStatus.SC_BAD_REQUEST);
			}
			return output;
		} catch (Exception e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
			.getExceptionLogBuilder(e, "get counters  error", this.getClass().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeExceptionLog();
			printJsonParsingException(e);
			throw new DMSMSCLICommandExecutionException(
					"Internal server error:JSON parsing",
					HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}

	}

}
