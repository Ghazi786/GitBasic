package com.jio.crm.dms.configurationmanager;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.jio.crm.dms.logger.DappLogManager;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.rest.xmlparser.ComboValue;
import com.jio.crm.dms.rest.xmlparser.ConfigParamCategory;
import com.jio.crm.dms.rest.xmlparser.ConfigParamList;
import com.jio.crm.dms.rest.xmlparser.Param;
import com.jio.crm.dms.threadpool.DappThreadAudit;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.DappParameters;
import com.jio.crm.dms.utils.ExcelBookConstants;

/**
 * This class is contains the Configuration Parameter Related to Dapp. On
 * initiation of this class it will find 1. ProceesId of Dapp 2. initialize
 * cliParamMap (Command Line Interface Configuration Parameter Enum) 3. load
 * parameter of Configuration Sheet and DataBase sheet in the
 * DappConfigParamsEnum
 *
 */
public class RuntimeConfigurationEngine implements RuntimeConfigurationEngineMBean {

	private String className = this.getClass().getName();
	private boolean systemLinux;
	private int processId;
	private Map<String, String> cliParamMap;
	private final static int CONFIG_DATABASE_SHEET = 2;
	public static final String COMBOBOX = "combobox";
	private HashMap<String, ConfigParamCategory> paramCategoryMap = new HashMap<>();
	private final Format FORMATTER = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

	public RuntimeConfigurationEngine() {
		try {
			findPID();
			initCliParamMap();
			loadConfigParams();
			ExcelWorkbook.getInstance().loadkafkaconfigsheet();
			ExcelWorkbook.getInstance().loadnotificationconfigsheet();
			ExcelWorkbook.getInstance().loadtibcointegrationconfigsheet();
			ExcelWorkbook.getInstance().loadfmsconfigurationsheet();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage());
		}
	}

	public void printEnum() {

		EnumSet.allOf(ConfigParamsEnum.class).forEach(e -> {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Config Param " + e + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Value :" + e.getValue() + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Description " + e.getDescription() + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Param Range " + e.getParamRange() + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Parameter Type : " + e.getCategory() + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

		});
	}

	/**
	 * Start the mbean server for monitoring and debugging purpose
	 */
	public void startMbean() {

		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName adapterName = new ObjectName(DappParameters.MBEAN_NAME_PARAMS);
			mbeanServer.registerMBean(this, adapterName);

		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder(e.getMessage());
		}
	}

	/**
	 * Unregister the Mbeans Server
	 */
	public void stopMbean() {
		try {

			ManagementFactory.getPlatformMBeanServer()
					.unregisterMBean(new ObjectName(DappParameters.MBEAN_NAME_PARAMS));

		} catch (MalformedObjectNameException | MBeanRegistrationException | InstanceNotFoundException e) {

			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder(e.getMessage());
		}
	}

	private void findPID() {
		this.systemLinux = System.getProperty("os.name").indexOf("Linux") >= 0;
		try {
			if (this.systemLinux) {
				String pid;
				Process pwdxProcess;
				String pwdxOutput;
				String currentDirectory = System.getProperty("user.dir");

				Process jpsProcess = Runtime.getRuntime().exec(new String[] { "bash", "-c",
						"jps | grep " + DmsBootStrapper.class.getSimpleName() + " | awk '{print $1}'" });
				jpsProcess.waitFor();
				BufferedReader reader = new BufferedReader(new InputStreamReader(jpsProcess.getInputStream()));

				while ((pid = reader.readLine()) != null) {
					pwdxProcess = Runtime.getRuntime()
							.exec(new String[] { "bash", "-c", "pwdx " + pid + " | awk '{print $2}'" });
					pwdxProcess.waitFor();
					pwdxOutput = new BufferedReader(new InputStreamReader(pwdxProcess.getInputStream())).readLine();
					if (pwdxOutput.equals(currentDirectory)) {
						this.processId = Integer.parseInt(pid);

						break;
					}
				}
			} else {
				this.processId = 0;
			}

		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder("Error while reading System user.dir ",
					className, "findPID");
		} catch (Exception e) {

			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder(e + "Error occured while finding pid");
		}
	}

	private void initCliParamMap() {

		cliParamMap = EnumSet.allOf(ConfigParamsEnum.class).stream().filter(e -> e.getCliArg() != null)
				.collect(toMap(ConfigParamsEnum::getCliArg, ConfigParamsEnum::name, (oldValue, newValue) -> newValue));

	}

	public Map<String, Object> getAllParameter() {
		Map<String, Object> map = new HashMap<>();
		try {

			EnumSet.allOf(ConfigParamsEnum.class).stream().forEach(enm -> map.put(enm.getCliArg(), enm.getValue()));

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getErrorLogBuilder("CMNRuntimeConfigurationEngine", className, "getAllParameter")
					.writeExceptionLog();
		}

		return map;
	}

	/**
	 * 
	 * @param categoryName
	 */
	public void addParamCategory(String categoryName) {

		if (this.paramCategoryMap.get(categoryName) == null) {
			this.paramCategoryMap.put(categoryName, new ConfigParamCategory());
		}
	}

	private void loadConfigParams() {

		XSSFWorkbook workbook = ExcelWorkbook.getInstance().getWorkbook();
		// Here loading 2 sheets: configuration,database
		for (int i = 0; i < CONFIG_DATABASE_SHEET; i++) {
			try {
				XSSFSheet sheet = workbook.getSheetAt(i);

				Iterator<Row> rowIterator = sheet.iterator();
				String paramName = null;
				String paramValue = null;
				String paramCategory = null;
				String paramValidator = null;
				boolean isReadOnly = false;
				String description = null;
				String possibleValues = null;
				boolean startupConfig = false;
				ConfigParamRange paramRange = null;
				XSSFCell cell = null;
				ConfigParamsEnum paramEnum = null;
				int rowNum;
				while (rowIterator.hasNext()) {
					XSSFRow row = (XSSFRow) rowIterator.next();
					rowNum = row.getRowNum();

					/* Read the Parameter_Type column value from excel sheet */
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_TYPE);
					if (cell == null) {
						emptyCellException("Parameter Type", rowNum);
					} else {

						cell.setCellType(XSSFCell.CELL_TYPE_STRING);
						if (cell.getStringCellValue().trim().isEmpty())
							emptyCellException("Parameter Type", rowNum);

						String[] str = cell.getStringCellValue().trim().split("/");
						if (str.length == 1)
							emptyCellException("Parameter Validator", rowNum);
						paramCategory = str[0].trim();

						if (paramCategory.equals(ConfigParamValidators.LOGGER))
							continue;
						addParamCategory(paramCategory);
						paramValidator = str[1].trim().toLowerCase();
					}
					/* Read the Parameter_Name column value from excel sheet */
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_NAME);

					if (cell == null) {
						emptyCellException("Parameter Name", rowNum);
					} else {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						paramName = cell.getStringCellValue().trim();

						if (paramName.isEmpty())
							emptyCellException("Parameter Name", rowNum);

						try {
							paramEnum = ConfigParamsEnum.valueOf(paramName);
						} catch (IllegalArgumentException e) {
							throw new IllegalArgumentException("Parameter " + paramName + " at row [" + rowNum
									+ "] has not been declared and defined.", e);
						}
					}
					/*
					 * Read the Startup_Configurable column value from excel sheet
					 */
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_STARTUP_CONFIG);
					if (cell == null) {
						emptyCellException("Startup Configurable", rowNum);
					} else {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						if (cell.getStringCellValue().trim().isEmpty())
							emptyCellException("Startup Configurable", rowNum);
						startupConfig = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? true : false;
					}

					/*
					 * Read the Runtime_Configurable column value from excel sheet
					 */
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_RUNTIME_CONFIG);
					if (cell == null) {
						emptyCellException("Runtime Configurable", rowNum);
					} else {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						if (cell.getStringCellValue().trim().isEmpty())
							emptyCellException("Runtime Configurable", rowNum);
						isReadOnly = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? false : true;
					}

					/* Read the Parameter_Value column value from excel sheet */
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_VALUE);
					if (cell == null) {
						emptyCellException("Parameter Value", rowNum);
					} else {
						if (paramEnum != null) {
							if (paramEnum.isValueRequired() && startupConfig) {
								cell.setCellType(Cell.CELL_TYPE_STRING);
								paramValue = cell.getStringCellValue().trim();

								if (paramValue.isEmpty())
									emptyCellException("Parameter Value", rowNum);
							} else {
								cell.setCellType(Cell.CELL_TYPE_STRING);
								paramValue = cell.getStringCellValue();
							}
						}
					}

					/* Read the Description column value from excel sheet */
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_DESCRIPTION);
					if (cell == null) {
						emptyCellException("Description", rowNum);
					} else {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						description = cell.getStringCellValue().trim();
						if (description.isEmpty())
							emptyCellException("Description", rowNum);
					}

					/* Read the Possible_Values column value from excel sheet */
					if (paramValidator != null) {
						cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_POSSIBLE_VALUES);
						if (cell != null) {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							possibleValues = cell.getStringCellValue().trim();
							if (possibleValues.isEmpty() && paramValidator.equals(ConfigParamValidators.COMBOBOX))
								throw new IllegalArgumentException(
										"Possible values not supplied for " + paramName + " at row [" + rowNum + "]");
							String[] arr = null;
							if (possibleValues.indexOf(',', 1) > 0) {
								arr = possibleValues.split(",");
								ArrayList<String> list = new ArrayList<>();
								for (String element : arr)
									list.add(element.trim());
								paramRange = new ConfigParamRange(list);
							} else if (possibleValues.indexOf('-', 1) > 0) {
								arr = possibleValues.split("-");
								if (arr.length != 2)
									invalidCellValueException(Constants.PARAMETER_RANGE, rowNum);
								long minValue = 0L;
								long maxValue = 0L;
								try {
									minValue = Long.parseLong(arr[0].trim());
								} catch (Exception e) {
									invalidCellValueException(Constants.PARAMETER_RANGE, rowNum, e);

								}
								try {
									maxValue = Long.parseLong(arr[1].trim());
								} catch (Exception e) {
									invalidCellValueException(Constants.PARAMETER_RANGE, rowNum, e);

								}
								paramRange = new ConfigParamRange(minValue, maxValue);
							}
						} else {
							if (paramValidator.equals(ConfigParamValidators.COMBOBOX))
								invalidCellValueException("Combobox", rowNum);
						}
					}

					if (paramEnum != null) {
						if (startupConfig) {
							paramEnum.setParamValue(paramValue);
						}
						paramEnum.setValidator(paramValidator);
						paramEnum.setReadOnly(isReadOnly);
						paramEnum.setCategory(paramCategory);
						paramEnum.setDescription(description);
						paramEnum.setParamRange(paramRange);
					}

					paramRange = null;
					description = null;
					paramCategory = null;
					paramValidator = null;
					paramValue = null;
				}

				String message = "Configuration parameters have been successfully loaded from the excel sheets.";
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(message, className, "loadConfigParams").writeLog();
				InterfaceStatus.status.setConfigurationModule(true);
			} catch (Exception e) {

				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
						"Error while loading paramter of Excel Sheet in Enum", className, "loadConfigParams")
						.writeExceptionLog();
				InterfaceStatus.status.setConfigurationModule(false);
			}
		}

	}

	public int getProcessId() {
		return processId;
	}

	private void emptyCellException(String fieldName, int rowNumber) {
		throw new IllegalArgumentException(
				"Field <" + fieldName + "> has been left empty or not specified in row (" + rowNumber + ")");
	}

	private void invalidCellValueException(String fieldName, int rowNumber) {
		throw new IllegalArgumentException(
				"Value(s) for field " + fieldName + "not supplied or invalid in row (" + rowNumber + ").");
	}

	private void invalidCellValueException(String fieldName, int rowNumber, Exception e) {
		throw new IllegalArgumentException(
				"Value(s) for field " + fieldName + "not supplied or invalid in row (" + rowNumber + ").", e);
	}

	@Override
	public byte updateParam(String param, String value, boolean sentByCli, boolean isBulkset) {

		String parameter = param.trim();
		TraceParam traceParam = new TraceParam();
		traceParam.setParamName(parameter);
		byte result = ConfigParamsEnum.valueOf(param).setValue(value, traceParam, sentByCli, isBulkset);
		if (!sentByCli)
			traceParam.setChangingAgent(RuntimeConfigurationParametersAction.PARAM_CHANGING_AGENT_EMS);
		else
			traceParam.setChangingAgent(RuntimeConfigurationParametersAction.PARAM_CHANGING_AGENT_CLI);

		if (result != 0)
			return 1;

		return 0;
	}

	@Override
	public String fetchParam(String param) {

		String paramEnum = null;
		try {
			paramEnum = ConfigParamsEnum.valueOf(param).getStringValue();
		} catch (IllegalArgumentException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"Error while fetching Parameter in fetchParam method");

		}
		return paramEnum;
	}

	@Override
	public Map<String, String> getCliParamMap() {

		return this.cliParamMap;
	}

	@Override
	public String getProductName() {

		return ConfigParamsEnum.PRODUCT_NAME.getStringValue();
	}

	@Override
	public String getProductVersion() {

		return ConfigParamsEnum.PRODUCT_VERSION.getStringValue();
	}

	@Override
	public boolean deleteLogFiles() {

		boolean isSuccess = false;
		try {
			String message = "Deleting rolled-over log files, if available.";

			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder(message).writeLog();
			File folder = new File(ConfigParamsEnum.LOG_FILE_PATH.getStringValue());
			File[] filesList = folder.listFiles();

			if (filesList == null) {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("No log files to delete.").writeLog();
				isSuccess = true;
				return isSuccess;
			}

			/* delete the backup logs */
			for (File file : filesList) {
				if (file.getName().endsWith(".log") && file.delete()) {

					String message2 = "Log File " + file.getName() + " has been deleted.";

					DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder(message2).writeLog();

				}
			}

			isSuccess = true;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					e.getMessage() + " Error while deleting Log Files ");
			isSuccess = false;
		}

		return isSuccess;

	}

	@Override
	public String fetchAllParamsAsXml() {

		StringWriter stringWriter = new StringWriter();
		try {
			Serializer serializer = new Persister();
			ConfigParamList configParamList = new ConfigParamList();
			Param param = null;
			List<String> list;
			for (ConfigParamsEnum paramEnum : ConfigParamsEnum.values()) {
				/** DUmp only if param is visible in EMS **/
				if (paramEnum.isVisibilityInEms()) {
					if (paramEnum.getParamRange() == null) {
						param = new Param(paramEnum.name(), paramEnum.getStringValue(), paramEnum.getValidator(),
								paramEnum.isReadOnly(), paramEnum.isValueRequired());
					} else if (paramEnum.getParamRange().getList() != null
							&& paramEnum.getValidator().equals(COMBOBOX)) {
						param = new Param(paramEnum.name(), paramEnum.getStringValue(), paramEnum.getValidator(),
								paramEnum.isReadOnly(), paramEnum.isValueRequired());
						list = paramEnum.getParamRange().getList();
						for (String element : list)
							param.addToComboValueList(new ComboValue(element));
					} else if (paramEnum.getParamRange().getList() == null) {
						param = new Param(paramEnum.name(), paramEnum.getStringValue(), paramEnum.getValidator(),
								paramEnum.isReadOnly(), paramEnum.isValueRequired(),
								paramEnum.getParamRange().getMinValue(), paramEnum.getParamRange().getMaxValue());
					}

					ConfigParamCategory configParamCategory = this.paramCategoryMap.get(paramEnum.getCategory());

					try {
						if (configParamCategory != null) {
							configParamCategory.addToParamList(param);
						}
					} catch (Exception e) {
						DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
								.writeLog();
					}
				}
			}

			Iterator<Entry<String, ConfigParamCategory>> iterator = this.paramCategoryMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, ConfigParamCategory> entry = iterator.next();
				ConfigParamCategory configParamCategory = entry.getValue();
				if (configParamCategory.getParamList() != null && !configParamCategory.getParamList().isEmpty()) {
					configParamCategory.setParamType(entry.getKey());
					configParamList.addToConfigParamList(configParamCategory);
				}
			}
			try {
				serializer.write(configParamList, stringWriter);
				Set<String> categoryNames = this.paramCategoryMap.keySet();

				// Resetting the ParamCategory map
				for (String categoryName : categoryNames)
					this.paramCategoryMap.put(categoryName, new ConfigParamCategory());
				return stringWriter.toString();
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}
			return null;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"Error occoured while fetching All Param As Xml with message " + e.getMessage(), className,
					"fetchAllParamsAsXml").writeExceptionLog();
		}
		return stringWriter.toString();
	}

	@Override
	public String getLogLevel() {

		return DappLogManager.getInstance().getLogger().getLevel().toString();
	}

	@Override
	public void setLogLevel(String logLevel) {

		DappLogManager.getInstance().setCurrentLogLevel(logLevel);
	}

	@Override
	public boolean dumpConfigParams() {

		boolean isSuccess = false;

		DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Going to dump all configurable parameters...").writeLog();

		// XML FILE
		try {
			Serializer serializer = new Persister();
			String paramsAsXML = this.fetchAllParamsAsXml();
			ConfigParamList configList = serializer.read(ConfigParamList.class, paramsAsXML);
			for (ConfigParamCategory configParamCategory : configList.getConfigParam()) {
				for (Param param : configParamCategory.getParamList()) {
					param.setComboValueList(null);
					param.setMaxRange(null);
					param.setMinRange(null);
					param.removeReadOnlyFlag();
					param.setValidator(null);
					param.setRequired(null);

				}
			}
			Serializer serializer2 = new Persister();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(byteArrayOutputStream);

			serializer2.write(configList, outputStreamWriter);
			String xmlMsg = byteArrayOutputStream.toString();
			File file = new File(DappParameters.DUMP_PATH_PARAMS);
			if (!file.exists())
				file.mkdir();

			String fileFormatXML = DappParameters.NODE_NAME + DappParameters.DUMP_PARAMS_FILE_PREFIX
					+ DappParameters.XML + FORMATTER.format(new Date()) + DappParameters.DUMP_FILE_EXTENSION_XML;
			try (FileWriter fileWriter = new FileWriter(
					DappParameters.DUMP_PATH_PARAMS + File.separator + fileFormatXML, false)) {
				fileWriter.write(xmlMsg);
			}

			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Dumped all configurable parameters in XML format.")
					.writeLog();

			isSuccess = true;
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage() + " in ConfigurationEngine : dumpConfigParams")
					.writeExceptionLog();
			isSuccess = false;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage() + " in ConfigurationEngine : dumpConfigParams")
					.writeExceptionLog();
			isSuccess = false;
		}
		return isSuccess;

	}

	@Override
	public String getAndDumpThreadStats() {

		return DappThreadAudit.getInstance().dumpThreadStats();
	}

	@Override
	public String getCorePoolSize() {

		return Integer.toString(DmsBootStrapper.getInstance().getDappExecutor().getCorePoolSize());
	}

	@Override
	public String getMaxCorePoolSize() {

		return Integer.toString(DmsBootStrapper.getInstance().getDappExecutor().getMaximumPoolSize());
	}

	@Override
	public void setCorePoolSize(int corePoolSize) {

		DmsBootStrapper.getInstance().getDappExecutor().setCorePoolSize(corePoolSize);

	}

	@Override
	public void setMaxCorePoolSize(int maxCorePoolSize) {

		DmsBootStrapper.getInstance().getDappExecutor().setMaximumPoolSize(maxCorePoolSize);

	}

	@Override
	public void setKeepAliveTimeinMillis(long timeInMillis) {

		DmsBootStrapper.getInstance().getDappExecutor().setKeepAliveTime(timeInMillis, MILLISECONDS);
	}

	@Override
	public long getKeepAliveTimeinMillis() {

		return DmsBootStrapper.getInstance().getDappExecutor().getKeepAliveTime(MILLISECONDS);
	}

	@Override
	public void gracefulSHutdown() {
		DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Received request for graceful shutdown").writeLog();

		Timer time = new Timer();
		time.schedule(new WaitAndshutdownApplication(), 2500);
	}

	class WaitAndshutdownApplication extends TimerTask {
		@Override
		public void run() {
			try {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Gracefully shutting down application").writeLog();

				System.exit(0);
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
				System.exit(1);
			}
		}

	}

}
