package com.jio.crm.dms.clicommands;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSDeregisterWithOAMCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		String msg;
		boolean returnvalue = false;
		try {
			OamClientManager.getInstance().disconnect();

			InterfaceStatus.status.setRegisterWithOAM(false);

			returnvalue = true;

		} catch (Exception e) {

			returnvalue = false;

			InterfaceStatus.status.setRegisterWithOAM(true);

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "deregister To OAM error",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

		}

		if (returnvalue)
			msg = "DMS MS has been de-registered successfully.";

		else {
			msg = "DMS MS de-registration failed with OAM";
		}
		return msg;
	}

}
