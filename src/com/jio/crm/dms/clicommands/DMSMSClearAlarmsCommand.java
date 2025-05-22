package com.jio.crm.dms.clicommands;

import org.apache.http.HttpStatus;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLICommandExecutionException;
import com.jio.crm.dms.cli.DMSMSCLIOptionEnum;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;

public class DMSMSClearAlarmsCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		try {
			String identifier = cliData.getRequest().getHeader(DMSMSCLIOptionEnum.ID.getName());
			if (OamClientManager.getOamClientForAlarm().getAlarmByName(identifier) == null) {
				printInvalidOptionValueMessage(DMSMSCLIOptionEnum.ID.getName(), identifier);
				throw new DMSMSCLICommandExecutionException(
						"Invalid value '" + identifier + "' for option '" + DMSMSCLIOptionEnum.ID.getName(),
						HttpStatus.SC_BAD_REQUEST);
			}

			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					identifier + "__" + DmsBootStrapper.getInstance().getComponentId() + this.getClass().getName() + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
			OamClientManager.getOamClientForAlarm()
					.clearAlarmById(identifier + "__" + DmsBootStrapper.getInstance().getComponentId(), true);
			return identifier + " --alarm cleared";
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "clear alarm  error",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			printJsonParsingException(e);
			throw new DMSMSCLICommandExecutionException("Internal server error:JSON parsing",
					HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}

	}

}
