package com.jio.crm.dms.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;

import com.atom.OAM.Client.Management.OamClientManager;
import com.atom.OAM.Client.ems.pojo.config.ComboValue;
import com.atom.OAM.Client.ems.pojo.config.ConfigParamCategory;
import com.atom.OAM.Client.ems.pojo.config.ConfigParamList;
import com.atom.OAM.Client.ems.pojo.config.Param;
import com.jio.crm.dms.configurationmanager.ConfigParamValidators;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.Constants;

public class ConfigParamHandler extends HttpServlet {

	private static final long serialVersionUID = -2474859531582913422L;

	@Override
	public void service(HttpServletRequest httpservletrequest, HttpServletResponse httpServletResponse)
			throws IOException, ServletException {
		String operation = httpservletrequest.getParameter(Constants.ACTION);

		 DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Received request for config param.");

		
		try {
			if (httpservletrequest.getMethod().equals(HttpMethod.GET.asString())) {
				if (operation.equalsIgnoreCase(Constants.GET_BULK_CONFIG)) {

					OamClientManager.getOamClientForConfiguration()
							.pushConfigurationsToOamServer(this.fetchAllParamsAsXmlForOAM());
				} else {
					DappLoggerService.GENERAL_INFO_LOG
		              .getInfoLogBuilder("Invalid HTTP request action from OAM. operation:").writeLog();
					httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					httpServletResponse.addHeader(Constants.REQUEST_STATUS_REASON, "Invalid action value received");
					return;
				}
			} else if (httpservletrequest.getMethod().equals(HttpMethod.PUT.asString())) {
				if (operation.equalsIgnoreCase(Constants.SET_SINGLE_CONFIG)) {

					String category = httpservletrequest.getHeader("category");
					String scalarparam = httpservletrequest.getHeader("scalarparam");
					String scalarvalue = httpservletrequest.getHeader("scalarvalue");

					if (category.equalsIgnoreCase("scalar")) {
						if (scalarparam != null && !scalarparam.isEmpty() && scalarvalue != null
								&& !scalarvalue.isEmpty()) {
							DappLoggerService.GENERAL_INFO_LOG
			                  .getLogBuilder("scalarparam: " + scalarparam + " scalarvalue: " + scalarvalue,
			                      Constants.CMNCONFIGPARAMHANDLER, Constants.SERVICE)
			                  .writeLog();
							this.handleScalarParamSingleSetRequest( httpServletResponse, scalarparam,
									scalarvalue);
						} else {
							 DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					                  "GEtting illegal scalarparam or scalarvalue " + scalarvalue + "::" + scalarvalue,
					                  "CMNCounterManager", "stopService").writeLog();
							httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							httpServletResponse.addHeader(Constants.REQUEST_STATUS_REASON, "manditory param getting null");
							return;
						}
					} else if (category.equalsIgnoreCase("tabular")) {
						DappLoggerService.GENERAL_INFO_LOG
		                .getLogBuilder("Invalid case for indexer category:" + category, "CMNCounterManager",
		                    "stopService")
		                .writeLog();
						httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						httpServletResponse.addHeader(Constants.REQUEST_STATUS_REASON, "manditory param getting null");
						return;
					} else {
						DappLoggerService.GENERAL_INFO_LOG
		                .getLogBuilder("Invalid HTTP request action from OAM. category:> " + category,
		                    Constants.CMNCONFIGPARAMHANDLER, Constants.SERVICE)
		                .writeLog();
						httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						httpServletResponse.addHeader(Constants.REQUEST_STATUS_REASON,
								"category not matching for set parameter");
						return;
					}
				} else {
					 DappLoggerService.GENERAL_INFO_LOG
		              .getLogBuilder("Invalid HTTP request action from OAM. operation:> " + operation,
		                  Constants.CMNCONFIGPARAMHANDLER, Constants.SERVICE)
		              .writeLog();
					httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					httpServletResponse.addHeader(Constants.REQUEST_STATUS_REASON, "Invalid action value received");
					return;
				}
			}
			
		} catch (Exception e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
	          .getExceptionLogBuilder(e, "Exception occured", Constants.CMNCONFIGPARAMHANDLER, Constants.SERVICE)
	          .writeExceptionLog();
		}
	}

	private ConfigParamList fetchAllParamsAsXmlForOAM() {

		List<String> list = null;
		ConfigParamList configParamList = new ConfigParamList();
		ArrayList<ConfigParamCategory> configParams = new ArrayList<>();
		ConfigParamCategory configParamCategory = new ConfigParamCategory();
		configParamCategory.setParamType(DmsBootStrapper.getInstance().getComponentId());
		// combo box value for boolean parameters
		ArrayList<ComboValue> boolCombo = new ArrayList<>();
		boolCombo.add(new ComboValue("true"));
		boolCombo.add(new ComboValue("false"));

		for (ConfigParamsEnum paramEnum : ConfigParamsEnum.values()) {
			if (paramEnum.isVisibilityInEms()) {
				Param param = new Param(paramEnum.name(), String.valueOf(paramEnum.getStringValue()),
						paramEnum.getValidator(), paramEnum.isReadOnly(), paramEnum.isValueRequired());
				if (paramEnum.name().equalsIgnoreCase(ConfigParamsEnum.APPLICATION_LOG_LEVEL.name())) {
					// combo box value for loglevel
					param.setValidator(ConfigParamValidators.COMBOBOX);
					list = paramEnum.getParamRange().getList();
					for (String element : list)
						param.addToComboValueList(new ComboValue(element));
				}
				param.setRequired(true);
				param.setValidator(param.getValidator());
				configParamCategory.addToParamList(param);
			}
		}
		configParams.add(configParamCategory);
		configParamList.addConfigParams(configParams);
		return configParamList;
	}

	private void handleScalarParamSingleSetRequest( HttpServletResponse httpServletResponse,
			String param, String value) throws IOException {
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
		        "This is a POST request for single set of parameter '" + param + "' with value '" + value + "'",
		        Constants.CMNCONFIGPARAMHANDLER, "handleScalarParamSingleSetRequest").writeLog();
		int resultCode = handleParamSet(param, value, false);
		 DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
			        "Sending httpServletResponse for ParamSingleSetRequest towards OAM withresult:" + resultCode,
			        Constants.CMNCONFIGPARAMHANDLER, "handleScalarParamSingleSetRequest").writeLog();
		httpServletResponse.sendError(resultCode);
	}

	private int handleParamSet(String param, String value, boolean isBulkSet) {
		int result = DmsBootStrapper.getInstance().getConfigEngine().updateParam(param, value, false, isBulkSet);
		// update into excel
		switch (result) {
		case 0:
			return Constants.SUCCESS_ERROR;
		case 1:
			return Constants.SERVER_ERROR;
		case 2:
			return Constants.CLIENT_ERROR;
		default:
			return Constants.SERVER_ERROR;
		}

	}
}
