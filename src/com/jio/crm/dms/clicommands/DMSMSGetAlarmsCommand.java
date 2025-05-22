package com.jio.crm.dms.clicommands;

import java.util.HashSet;

import org.apache.http.HttpStatus;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLICommandExecutionException;
import com.jio.crm.dms.cli.DMSMSCLIOptionEnum;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSGetAlarmsCommand extends DMSMSAbstractCLICommand {

	private static HashSet<String> severities = new HashSet<>();

	static {
		severities.add("minor");
		severities.add("major");
		severities.add("critical");
		severities.add("indeterminate");
		severities.add("warning");
		severities.add("all");
	}
	
	@Override
	public String execute(DMSMSCLIPojo cliData) {

		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
		String severity = null;
		try {
			severity = cliData.getRequest().getHeader(DMSMSCLIOptionEnum.SEVERITY.getName());
			if (!severities.contains(severity.toLowerCase())) {
				printInvalidOptionValueMessage(
						DMSMSCLIOptionEnum.SEVERITY.getName(), severity);
				throw new DMSMSCLICommandExecutionException("Invalid value '"
						+ severity + "' for option '"
						+ DMSMSCLIOptionEnum.SEVERITY.getName(),
						HttpStatus.SC_BAD_REQUEST);
			}
			return OamClientManager.getOamClientForAlarm().getAlarmsString(
					severity);
		} catch (Exception e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
			.getExceptionLogBuilder(e, "get alarms error", this.getClass().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeExceptionLog();
			printJsonParsingException(e);
			throw new DMSMSCLICommandExecutionException("Error parsing JSON",
					HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		
	}

}
