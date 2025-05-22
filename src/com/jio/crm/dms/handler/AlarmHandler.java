package com.jio.crm.dms.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.alarmmanager.HttpParametersAndHeadersIntf;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;

public class AlarmHandler extends HttpServlet {

	private static final long serialVersionUID = 1547827605072194968L;
	private final String className = this.getClass().getName();

	@Override
	public void service(HttpServletRequest servletRequest, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			String level = servletRequest.getParameter(HttpParametersAndHeadersIntf.PARAMETER_LEVEL);
			String id = servletRequest.getParameter(HttpParametersAndHeadersIntf.PARAMETER_ID);
			String methodName = servletRequest.getMethod();
			boolean valid = true;

			// Exactly one of these should be present
			if ((level == null && id == null) || (level != null && id != null)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			if (servletRequest.getMethod().equalsIgnoreCase("GET")) {
				if (level != null) {
					DappLoggerService.GENERAL_INFO_LOG
							.getLogBuilder("Got a GET request from EMS for Alarms with level = " + level, className,
									Constants.HANDLE)
							.writeLog();
					String xml;
					xml = OamClientManager.getOamClientForAlarm().getAlarmAsXML(level);
					OamClientManager.getOamClientForAlarm().raiseBulkAlarms(xml, "1", "bulk");
					DappLoggerService.GENERAL_INFO_LOG
							.getLogBuilder("Connection released for POST", className, Constants.HANDLE).writeLog();
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					valid = false;
				}
			} else if (methodName.equals(HttpMethod.DELETE.asString())) {
				if (servletRequest.getParameter("action").equals("clearAlarms")) {
					if (level != null) {
						if (processDeleteAlarmByLevel(level))
							response.setStatus(HttpServletResponse.SC_OK);
						else {
							DappLoggerService.GENERAL_INFO_LOG
									.getLogBuilder("DELETE request couldn't be processed successfully.", className,
											Constants.HANDLE)
									.writeLog();
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

						}
					} else {
						DappLoggerService.GENERAL_INFO_LOG
								.getLogBuilder("Got a DELETE request from EMS for Alarm with id = " + id, className,
										Constants.HANDLE)
								.writeLog();
						if (processDeleteAlarmById(id))
							response.setStatus(HttpServletResponse.SC_OK);
						else {
							DappLoggerService.GENERAL_INFO_LOG
									.getLogBuilder("DELETE request couldn't be processed successfully.", className,
											Constants.HANDLE)
									.writeLog();
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						}
					}
				} else {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

					return;
				}
			} else {
				valid = false;
			}
			if (!valid) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), className, Constants.HANDLE).writeExceptionLog();
		}
	}

	private boolean processDeleteAlarmByLevel(String level) {
		return OamClientManager.getOamClientForAlarm().clearAlarmByLevel(level, false);

	}

	private boolean processDeleteAlarmById(String id) {
		if (id != null) {

			return OamClientManager.getOamClientForAlarm().clearAlarmByName(id, false);
		}
		return false;
	}
}
