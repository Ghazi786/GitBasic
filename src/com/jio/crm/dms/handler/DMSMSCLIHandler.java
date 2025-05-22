package com.jio.crm.dms.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.jio.crm.dms.cli.DMSMSCLICommandEnum;
import com.jio.crm.dms.cli.DMSMSCLICommandExecutionException;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.CommonUtility;

public class DMSMSCLIHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		final String methodName = "doPost";

		try {
			DappLoggerService.GENERAL_INFO_LOG
			.getLogBuilder(
					"received command from CLI",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeLog();
			DMSMSCLIPojo cliData = CommonUtility.getCliDataFromRequest(req);

			DappLoggerService.GENERAL_INFO_LOG
			.getLogBuilder(
					"command",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeLog();
			
			

			DMSMSCLICommandEnum cliCommandEnum = DMSMSCLICommandEnum
					.getEnumFromCommandName(cliData.getCommandName());

			if (cliCommandEnum == null) {
				String errDescription = "Command '" + cliData.getCommandName() + "' is not a valid command";
				
				DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						errDescription,
						this.getClass().getName(), methodName)
				.writeLog();
				
				sendResponse(resp, errDescription, HttpStatus.SC_BAD_REQUEST);
				return;
			}
			
			DappLoggerService.GENERAL_INFO_LOG
			.getLogBuilder(
					" executing command : " + cliData.getCommandName(),
					this.getClass().getName(), methodName)
			.writeLog();

			sendResponse(resp, cliCommandEnum.execute(cliData), HttpStatus.SC_OK);

		} catch (DMSMSCLICommandExecutionException e) {
			sendResponse(resp, e);
		} catch (Exception e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
			.getExceptionLogBuilder(e, "Exception in sending ", this.getClass().getName(),
					methodName)
			.writeExceptionLog();
		}
	}

	private void sendResponse(HttpServletResponse response, DMSMSCLICommandExecutionException e) {
		sendResponse(response, e.getMessage(), e.getStatus());
	}

	private void sendResponse(HttpServletResponse response, String body, int status) {
		try {
			response.getWriter().write(body);
			response.getWriter().flush();
			response.setStatus(status);
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
			.getExceptionLogBuilder(e, "IO Exception : ", this.getClass().getName(),
					"Send Response ")
			.writeExceptionLog();
		}
			}
	

	
}
