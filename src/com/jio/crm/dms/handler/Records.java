package com.jio.crm.dms.handler;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESOperationImpl;
import com.jio.crm.dms.utils.Constants;

public class Records extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest httpservletrequest, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handlerequest(httpservletrequest);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}

	}

	@Override
	protected void doPost(HttpServletRequest httpservletrequest, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handlerequest(httpservletrequest);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
	}

	@Override
	protected void doDelete(HttpServletRequest httpservletrequest, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handlerequest(httpservletrequest);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
	}

	@Override
	protected void doPut(HttpServletRequest httpservletrequest, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handlerequest(httpservletrequest);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
	}

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		super.service(arg0, arg1);
	}

	/*
	 * common handler for all request to send response to receiver object
	 */
	public void handlerequest(HttpServletRequest httpservletrequest){
		try {

			switch (httpservletrequest.getParameter(Constants.ACTION)) {
			case "submit":
				submitTransaction(httpservletrequest);

				break;

			case "get":

				break;

			default:
				break;
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

	}

	private void submitTransaction(HttpServletRequest httpservletrequest) {
		try {
			InputStream inputStream = httpservletrequest.getInputStream();

			JSONObject bodyJson = new JSONObject(
					IOUtils.toString(inputStream, httpservletrequest.getCharacterEncoding()));
			boolean csvInfoUpdate = ESOperationImpl.getInstance().putCSVCredits(bodyJson);
			if (csvInfoUpdate) {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("information updated in es");
			} else {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("unable to upadate information in es");

			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage());
		}

	}

}
