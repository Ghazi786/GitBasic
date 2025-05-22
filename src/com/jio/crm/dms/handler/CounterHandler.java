package com.jio.crm.dms.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.countermanager.CounterManager;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;



public class CounterHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String type = req.getHeader(Constants.PARAMETER_RESET);
	    String reset = req.getHeader(Constants.PARAMETER_TYPE);

	    DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Got a GET request from OAM for '" + type
	        + "' performance counters with reset parameter set to = " + reset);

	    try {

	      String counterXml = CounterManager.getInstance().fetchCounterForSpecificTypeAsXml(type);

	      resp.sendError(HttpStatus.SC_OK);

	      int result = OamClientManager.getOamClientForCounter().pushCountersToOamServer(counterXml, reset, type);

	      DappLoggerService.GENERAL_INFO_LOG
	          .getInfoLogBuilder("Response received for POST request sent for counter reset : " + result);

	      if (result == HttpStatus.SC_OK && "1".equals(reset))
	        CounterManager.getInstance().resetCounterForSpecificType(type, false);

	    } catch (Exception e) {
	      DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "Error in Counter fetch request ");

	    }

	  }
	}
	
	
	
