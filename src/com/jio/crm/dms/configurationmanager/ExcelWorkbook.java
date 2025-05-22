package com.jio.crm.dms.configurationmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.crm.dms.fms.FMSIntegrationConfigEnum;
import com.jio.crm.dms.ha.DumpEventProcessTask;
import com.jio.crm.dms.ha.EventDumpTask;
import com.jio.crm.dms.kafka.KafkaConfigEnum;
import com.jio.crm.dms.kafka.producer.UOLProducerTask;
import com.jio.crm.dms.kafka.producer.UOLProducerTaskFactory;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.notification.NotificationServiceConfigEnum;
import com.jio.crm.dms.notification.tibco.TibcoConfigEnum;
import com.jio.crm.dms.threadpool.RecordProcesspoolFactory;
import com.jio.crm.dms.threadpoolfactory.ClearCodePoolFactory;
import com.jio.crm.dms.threadpoolfactory.DumpEventProcessTaskFactory;
import com.jio.crm.dms.threadpoolfactory.EventAckDispatcherFactory;
import com.jio.crm.dms.threadpoolfactory.EventAckRequestDispatcher;
import com.jio.crm.dms.threadpoolfactory.EventDumpTaskFactory;
import com.jio.crm.dms.threadpoolfactory.ResttalkBuilderPoolFactory;
import com.jio.crm.dms.threads.RecordProcess;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.ExcelBookConstants;
import com.jio.telco.framework.clearcode.ClearCodeAsnPojo;
import com.jio.telco.framework.pool.PoolBuilder;
import com.jio.telco.framework.resource.ResourceBuilder;

/**
 * This class contains the Transaction Resource Manager Configuration Excel
 * sheet. This class is Singleton, thread safe and will load lazily. It will
 * have Configuration Parameter Sheet, Mongo DataBase Parameter sheet OAM_Client
 * Config sheet and Alarm sheet which will loaded during Boot time of CMN.
 *
 */

public class ExcelWorkbook {

	private static ExcelWorkbook workBookInstance = null;
	private final String className = this.getClass().getName();

	private XSSFSheet configSettingsSheet;
	private XSSFSheet databaseSheet;
	private XSSFSheet oamClientSheet;
	private XSSFSheet alarmSheet;
	private XSSFWorkbook workbook;
	private XSSFSheet poolingSheet;
	private XSSFSheet kafkaConfigSheet;
	private XSSFSheet notificationConfigSheet;
	private XSSFSheet tibcoIntegrationConfigSheet;
	private XSSFSheet fmsIntegrationConfigSheet;

	private ExcelWorkbook() {

		if (workBookInstance != null) {
			throw new UnsupportedOperationException(
					"Use getInsatnce() method to create instance of DappExcelWorkbook class");
		}
	}

	public static synchronized ExcelWorkbook getInstance() {
		if (workBookInstance == null) {
			workBookInstance = new ExcelWorkbook();
		}
		return workBookInstance;
	}

	/**
	 * This method will load the excel sheet data from ConfigParamSheet
	 * 
	 * @return
	 */
	public void initialize() {
		FileInputStream fileInputStream2 = null;

		try {

			String profile = System.getProperty("profile");
			if (profile != null && profile.equals("dev")) {
				fileInputStream2 = new FileInputStream(new File("./configuration/ConfigParamSheet.xlsx"));
			} else {
				fileInputStream2 = new FileInputStream(new File("../configuration/ConfigParamSheet.xlsx"));

			}

			XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream2);

			this.workbook = workbook;

			this.configSettingsSheet = this.workbook.getSheetAt(ExcelBookConstants.ParamSheet.CONFIG_SETTINGS_SHEET);
			this.configSettingsSheet.removeRow(this.configSettingsSheet.getRow(0));

			this.databaseSheet = this.workbook.getSheetAt(ExcelBookConstants.ParamSheet.DATABASE_SHEET);
			this.databaseSheet.removeRow(this.databaseSheet.getRow(0));

			this.oamClientSheet = this.workbook.getSheetAt(ExcelBookConstants.ParamSheet.OAM_CLIENT_SHEET);
			this.oamClientSheet.removeRow(this.oamClientSheet.getRow(0));

			this.alarmSheet = this.workbook.getSheetAt(ExcelBookConstants.ParamSheet.ALARM_SHEET);
			this.alarmSheet.removeRow(this.alarmSheet.getRow(0));

			this.poolingSheet = this.workbook.getSheetAt(ExcelBookConstants.ParamSheet.INDEX_POOLING_SHEET);
			this.poolingSheet.removeRow(this.poolingSheet.getRow(0));

			this.kafkaConfigSheet = this.workbook.getSheetAt(ExcelBookConstants.ParamSheet.KAFKA_CONFIG_SHEET);

			this.notificationConfigSheet = this.workbook
					.getSheetAt(ExcelBookConstants.ParamSheet.NOTIFICATION_CONFIG_SHEET);

			this.tibcoIntegrationConfigSheet = this.workbook
					.getSheetAt(ExcelBookConstants.ParamSheet.TIBCO_INTEGRATION_CONFIG_SHEET);

			this.fmsIntegrationConfigSheet = this.workbook
					.getSheetAt(ExcelBookConstants.ParamSheet.FMS_INTEGRATION_CONFIG_SHEET);

			fileInputStream2.close();
			loadandconfigurepoolingsheet();
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Excel workbook and sheets have been initialized." + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

		} catch (FileNotFoundException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, "Configuration ExcelSheet Not Found", className, "initialize")
					.writeExceptionLog();

		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"IOException occured while loading Configuration Excel Sheet", className, "initialize")
					.writeExceptionLog();
		}

		finally {

			try {
				if (fileInputStream2 != null) {
					fileInputStream2.close();
				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();

			}

		}

	}

	private void loadandconfigurepoolingsheet() {
		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder("Reading configuration pooling sheet", className, "loadAndConfigurePoolingSheet")
					.writeLog();

			String poolName;
			Cell poolCell;
			Row poolRow;
			PoolBuilder builder;
			for (int i = 1; i <= this.poolingSheet.getPhysicalNumberOfRows(); i++) {
				builder = ResourceBuilder.objectPool();
				poolRow = this.poolingSheet.getRow(i);
				poolName = poolRow.getCell(0).getStringCellValue().trim();
				poolCell = poolRow.getCell(1);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setMaxActive(Integer.parseInt(poolCell.getStringCellValue().trim()));

				poolCell = poolRow.getCell(2);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setMaxIdle(Integer.parseInt(poolCell.getStringCellValue().trim()));

				poolCell = poolRow.getCell(3);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setMaxWait(Long.parseLong(poolCell.getStringCellValue().trim()));

				poolCell = poolRow.getCell(4);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setMinEvictableIdleTimeMillis(Long.parseLong(poolCell.getStringCellValue().trim()));

				poolCell = poolRow.getCell(5);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setMinIdle(Integer.parseInt(poolCell.getStringCellValue().trim()));

				poolCell = poolRow.getCell(6);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setNumTestsPerEviction(Integer.parseInt(poolCell.getStringCellValue().trim()));

				poolCell = poolRow.getCell(7);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setTestOnBorrow(Boolean.parseBoolean(poolCell.getStringCellValue().trim().toLowerCase()));

				poolCell = poolRow.getCell(8);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setTestOnReturn(Boolean.parseBoolean(poolCell.getStringCellValue().trim().toLowerCase()));

				poolCell = poolRow.getCell(9);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setTestWhileIdle(Boolean.parseBoolean(poolCell.getStringCellValue().trim().toLowerCase()));

				poolCell = poolRow.getCell(10);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setTimeBetweenEvictionRunsMillis(Long.parseLong(poolCell.getStringCellValue().trim()));

				poolCell = poolRow.getCell(11);
				poolCell.setCellType(Cell.CELL_TYPE_STRING);
				builder.setWhenExhaustAction(Boolean.parseBoolean(poolCell.getStringCellValue().trim().toLowerCase()));
				switch (poolName) {

				case Constants.DEVOPS_CLEAR_CODE_POOL:
					builder.createObjectPool(new ClearCodePoolFactory(), ClearCodeAsnPojo.class);
					break;

				case Constants.DEVOPS_EVENT_ACK_POOL:
					builder.createObjectPool(new EventAckDispatcherFactory(), EventAckRequestDispatcher.class);
					break;

				case Constants.DUMP_EVENT_TASK_POOL:
					builder.createObjectPool(new EventDumpTaskFactory(), EventDumpTask.class);
					break;

				case Constants.DUMP_EVENT_PROCESS_TASK_POOL:
					builder.createObjectPool(new DumpEventProcessTaskFactory(), DumpEventProcessTask.class);
					break;

				case Constants.RECORDABLE:
					builder.createObjectPool(new RecordProcesspoolFactory(), RecordProcess.class);
					break;
				case Constants.UOL_PRODUCER_TASK_POOL:
					builder.createObjectPool(new UOLProducerTaskFactory(), UOLProducerTask.class);
					break;

				default:
					break;
				}
				builder.createObjectPool(new ResttalkBuilderPoolFactory(), RestTalkBuilder.class);

			}
			InterfaceStatus.status.setObjectPoolingManager(true);
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder("Reading configuration pooling sheet", className, "loadAndConfigurePoolingSheet")
					.writeLog();
		}

	}

	public boolean loadkafkaconfigsheet() {

		DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Loading Kafka Configuration ... ").writeLog();

		boolean isLoaded = false;

		try {

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
			com.jio.crm.dms.kafka.KafkaConfigEnum paramEnum = null;
			XSSFSheet sheet = getKafkaConfigSheet();
			for (int rowNum = 1; rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_TYPE);
				if (cell == null) {
					emptyCellException(Constants.PARAMETER_TYPES, rowNum);
				} else {

					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.PARAMETER_TYPES, rowNum);

					String[] str = cell.getStringCellValue().trim().split("/");
					if (str.length == 1)
						emptyCellException(Constants.PARAMETER_VALIDATOR, rowNum);
					paramCategory = str[0].trim();

					if (paramCategory.equals(ConfigParamValidators.LOGGER))
						continue;
					paramValidator = str[1].trim().toLowerCase();
				}
				/* Read the Parameter_Name column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_NAME);

				if (cell == null) {
					emptyCellException(Constants.PARAMETER_NAME, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					paramName = cell.getStringCellValue().trim();

					if (paramName.isEmpty())
						emptyCellException(Constants.PARAMETER_NAME, rowNum);

					try {
						paramEnum = KafkaConfigEnum.valueOf(paramName);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(Constants.PARAMETER + paramName + Constants.ATROW + rowNum
								+ Constants.NOTDECLAREDANDDEFINED, e);
					}
				}
				/*
				 * Read the Startup_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_STARTUP_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.CONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.CONFIGURABLE, rowNum);
					startupConfig = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? true : false;
				}

				/*
				 * Read the Runtime_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_RUNTIME_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
					isReadOnly = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? false : true;
				}

				/* Read the Parameter_Value column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_VALUE);
				if (cell == null) {
					emptyCellException(Constants.PARAMTER_VALUE, rowNum);
				} else {
					if (paramEnum != null) {
						if (paramEnum.isValueRequired() && startupConfig) {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue().trim();

							if (paramValue.isEmpty())
								emptyCellException(Constants.PARAMTER_VALUE, rowNum);
						} else {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue();
						}
					}
				}

				/* Read the Description column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_DESCRIPTION);
				if (cell == null) {
					emptyCellException(Constants.DESCRIPTION, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					description = cell.getStringCellValue().trim();
					if (description.isEmpty())
						emptyCellException(Constants.DESCRIPTION, rowNum);
				}

				/* Read the Possible_Values column value from excel sheet */
				if (paramValidator != null) {
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_POSSIBLE_VALUES);
					if (cell != null) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						possibleValues = cell.getStringCellValue().trim();
						if (possibleValues.isEmpty() && paramValidator.equals(ConfigParamValidators.COMBOBOX))
							throw new IllegalArgumentException(
									Constants.POSSIBLEVALUESNOTSUPPLIED + paramName + Constants.ATROW + rowNum + "]");
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
							invalidCellValueException(Constants.COMBOBOX, rowNum);
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
					DappLoggerService.GENERAL_INFO_LOG
							.getLogBuilder("Loading Kafka Service Parameter Config | " + paramName + " = " + paramValue,
									className, "loadkafkaConfigSheet")
							.writeLog();
				}

				paramRange = null;
				description = null;
				paramCategory = null;
				paramValidator = null;
				paramValue = null;
			}

			String message = "Kafka Configuration parameters have been successfully loaded from the excel sheets.";
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(message, className, "loadkafkaConfigSheet").writeLog();
			InterfaceStatus.status.setKafkaConfigurationModule(true);
			isLoaded = true;

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			isLoaded = false;
		}

		return isLoaded;
	}

	public boolean loadnotificationconfigsheet() {

		DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Loading Notification Configuration ... ").writeLog();

		boolean isLoaded = false;

		try {

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
			NotificationServiceConfigEnum paramEnum = null;
			XSSFSheet sheet = getNotificationConfigSheet();
			for (int rowNum = 1; rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_TYPE);
				if (cell == null) {
					emptyCellException(Constants.PARAMETER_TYPES, rowNum);
				} else {

					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.PARAMETER_TYPES, rowNum);

					String[] str = cell.getStringCellValue().trim().split("/");
					if (str.length == 1)
						emptyCellException(Constants.PARAMETER_VALIDATOR, rowNum);
					paramCategory = str[0].trim();

					if (paramCategory.equals(ConfigParamValidators.LOGGER))
						continue;
					paramValidator = str[1].trim().toLowerCase();
				}
				/* Read the Parameter_Name column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_NAME);

				if (cell == null) {
					emptyCellException(Constants.PARAMETER_NAME, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					paramName = cell.getStringCellValue().trim();

					if (paramName.isEmpty())
						emptyCellException(Constants.PARAMETER_NAME, rowNum);

					try {
						paramEnum = NotificationServiceConfigEnum.valueOf(paramName);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(Constants.PARAMETER + paramName + Constants.ATROW + rowNum
								+ Constants.NOTDECLAREDANDDEFINED, e);
					}
				}
				/*
				 * Read the Startup_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_STARTUP_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.CONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.CONFIGURABLE, rowNum);
					startupConfig = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? true : false;
				}

				/*
				 * Read the Runtime_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_RUNTIME_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
					isReadOnly = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? false : true;
				}

				/* Read the Parameter_Value column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_VALUE);
				if (cell == null) {
					emptyCellException(Constants.PARAMTER_VALUE, rowNum);
				} else {
					if (paramEnum != null) {
						if (paramEnum.isValueRequired() && startupConfig) {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue().trim();

							if (paramValue.isEmpty())
								emptyCellException(Constants.PARAMTER_VALUE, rowNum);
						} else {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue();
						}
					}
				}

				/* Read the Description column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_DESCRIPTION);
				if (cell == null) {
					emptyCellException(Constants.DESCRIPTION, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					description = cell.getStringCellValue().trim();
					if (description.isEmpty())
						emptyCellException(Constants.DESCRIPTION, rowNum);
				}

				/* Read the Possible_Values column value from excel sheet */
				if (paramValidator != null) {
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_POSSIBLE_VALUES);
					if (cell != null) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						possibleValues = cell.getStringCellValue().trim();
						if (possibleValues.isEmpty() && paramValidator.equals(ConfigParamValidators.COMBOBOX))
							throw new IllegalArgumentException(
									Constants.POSSIBLEVALUESNOTSUPPLIED + paramName + Constants.ATROW + rowNum + "]");
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
							invalidCellValueException(Constants.COMBOBOX, rowNum);
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
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"Loading Notification Service Parameter Config | " + paramName + " = " + paramValue,
							className, "loadNotificationConfigSheet").writeLog();
				}

				paramRange = null;
				description = null;
				paramCategory = null;
				paramValidator = null;
				paramValue = null;
			}

			String message = "Notification Service Configuration parameters have been successfully loaded from the excel sheets.";
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(message, className, "loadNotificationConfigSheet")
					.writeLog();
			InterfaceStatus.status.setNotificationConfigurationModule(true);
			isLoaded = true;

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			isLoaded = false;
		}

		return isLoaded;

	}

	public boolean loadtibcointegrationconfigsheet() {

		DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Loading Tibco Configuration ... ").writeLog();

		boolean isLoaded = false;

		try {

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
			TibcoConfigEnum paramEnum = null;
			XSSFSheet sheet = getTibcoConfigSheet();
			for (int rowNum = 1; rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_TYPE);
				if (cell == null) {
					emptyCellException(Constants.PARAMETER_TYPES, rowNum);
				} else {

					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.PARAMETER_TYPES, rowNum);

					String[] str = cell.getStringCellValue().trim().split("/");
					if (str.length == 1)
						emptyCellException(Constants.PARAMETER_VALIDATOR, rowNum);
					paramCategory = str[0].trim();

					if (paramCategory.equals(ConfigParamValidators.LOGGER))
						continue;
					paramValidator = str[1].trim().toLowerCase();
				}
				/* Read the Parameter_Name column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_NAME);

				if (cell == null) {
					emptyCellException(Constants.PARAMETER_NAME, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					paramName = cell.getStringCellValue().trim();

					if (paramName.isEmpty())
						emptyCellException(Constants.PARAMETER_NAME, rowNum);

					try {
						paramEnum = TibcoConfigEnum.valueOf(paramName);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(Constants.PARAMETER + paramName + Constants.ATROW + rowNum
								+ Constants.NOTDECLAREDANDDEFINED, e);
					}
				}
				/*
				 * Read the Startup_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_STARTUP_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.CONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.CONFIGURABLE, rowNum);
					startupConfig = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? true : false;
				}

				/*
				 * Read the Runtime_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_RUNTIME_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
					isReadOnly = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? false : true;
				}

				/* Read the Parameter_Value column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_VALUE);
				if (cell == null) {
					emptyCellException(Constants.PARAMTER_VALUE, rowNum);
				} else {
					if (paramEnum != null) {
						if (paramEnum.isValueRequired() && startupConfig) {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue().trim();

							if (paramValue.isEmpty())
								emptyCellException(Constants.PARAMTER_VALUE, rowNum);
						} else {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue();
						}
					}
				}

				/* Read the Description column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_DESCRIPTION);
				if (cell == null) {
					emptyCellException(Constants.DESCRIPTION, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					description = cell.getStringCellValue().trim();
					if (description.isEmpty())
						emptyCellException(Constants.DESCRIPTION, rowNum);
				}

				/* Read the Possible_Values column value from excel sheet */
				if (paramValidator != null) {
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_POSSIBLE_VALUES);
					if (cell != null) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						possibleValues = cell.getStringCellValue().trim();
						if (possibleValues.isEmpty() && paramValidator.equals(ConfigParamValidators.COMBOBOX))
							throw new IllegalArgumentException(
									Constants.POSSIBLEVALUESNOTSUPPLIED + paramName + Constants.ATROW + rowNum + "]");
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
							invalidCellValueException(Constants.COMBOBOX, rowNum);
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
					DappLoggerService.GENERAL_INFO_LOG
							.getLogBuilder("Loading TIBCO SIT Parameter Config | " + paramName + " = " + paramValue,
									className, Constants.LOADTIBCOINTEGRATIONCONFIGSHEET)
							.writeLog();
				}

				paramRange = null;
				description = null;
				paramCategory = null;
				paramValidator = null;
				paramValue = null;
			}

			String message = "TIBCO SIT Service Configuration parameters have been successfully loaded from the excel sheets.";
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(message, className, Constants.LOADTIBCOINTEGRATIONCONFIGSHEET)
					.writeLog();
			InterfaceStatus.status.setTibcoIntegrationConfigurationModule(true);
			isLoaded = true;

		} catch (Exception e) {

			isLoaded = false;
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

		return isLoaded;

	}

	public boolean loadfmsconfigurationsheet() {

		DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Loading FMS Configuration ... ").writeLog();

		boolean isLoaded = false;

		try {

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
			FMSIntegrationConfigEnum paramEnum = null;
			XSSFSheet sheet = getFmsIntegrationConfigSheet();
			for (int rowNum = 1; rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
				XSSFRow row = sheet.getRow(rowNum);
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_TYPE);
				if (cell == null) {
					emptyCellException(Constants.PARAMETER_TYPES, rowNum);
				} else {

					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.PARAMETER_TYPES, rowNum);

					String[] str = cell.getStringCellValue().trim().split("/");
					if (str.length == 1)
						emptyCellException(Constants.PARAMETER_VALIDATOR, rowNum);
					paramCategory = str[0].trim();

					if (paramCategory.equals(ConfigParamValidators.LOGGER))
						continue;
					paramValidator = str[1].trim().toLowerCase();
				}
				/* Read the Parameter_Name column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_NAME);

				if (cell == null) {
					emptyCellException(Constants.PARAMETER_NAME, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					paramName = cell.getStringCellValue().trim();

					if (paramName.isEmpty())
						emptyCellException(Constants.PARAMETER_NAME, rowNum);

					try {
						paramEnum = FMSIntegrationConfigEnum.valueOf(paramName);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(Constants.PARAMETER + paramName + Constants.ATROW + rowNum
								+ Constants.NOTDECLAREDANDDEFINED, e);
					}
				}
				/*
				 * Read the Startup_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_STARTUP_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.CONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.CONFIGURABLE, rowNum);
					startupConfig = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? true : false;
				}

				/*
				 * Read the Runtime_Configurable column value from excel sheet
				 */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_RUNTIME_CONFIG);
				if (cell == null) {
					emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					if (cell.getStringCellValue().trim().isEmpty())
						emptyCellException(Constants.RUNTIMECONFIGURABLE, rowNum);
					isReadOnly = (cell.getStringCellValue().trim().equalsIgnoreCase("YES")) ? false : true;
				}

				/* Read the Parameter_Value column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_PARAM_VALUE);
				if (cell == null) {
					emptyCellException(Constants.PARAMTER_VALUE, rowNum);
				} else {
					if (paramEnum != null) {
						if (paramEnum.isValueRequired() && startupConfig) {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue().trim();

							if (paramValue.isEmpty())
								emptyCellException(Constants.PARAMTER_VALUE, rowNum);
						} else {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							paramValue = cell.getStringCellValue();
						}
					}
				}

				/* Read the Description column value from excel sheet */
				cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_DESCRIPTION);
				if (cell == null) {
					emptyCellException(Constants.DESCRIPTION, rowNum);
				} else {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					description = cell.getStringCellValue().trim();
					if (description.isEmpty())
						emptyCellException(Constants.DESCRIPTION, rowNum);
				}

				/* Read the Possible_Values column value from excel sheet */
				if (paramValidator != null) {
					cell = row.getCell(ExcelBookConstants.ColumnIndex.INDEX_POSSIBLE_VALUES);
					if (cell != null) {
						cell.setCellType(Cell.CELL_TYPE_STRING);
						possibleValues = cell.getStringCellValue().trim();
						if (possibleValues.isEmpty() && paramValidator.equals(ConfigParamValidators.COMBOBOX))
							throw new IllegalArgumentException(
									Constants.POSSIBLEVALUESNOTSUPPLIED + paramName + Constants.ATROW + rowNum + "]");
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
							invalidCellValueException(Constants.COMBOBOX, rowNum);
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
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"Loading FMS Integration Parameter Config | " + paramName + " = " + paramValue, className,
							Constants.LOADTIBCOINTEGRATIONCONFIGSHEET).writeLog();
				}

				paramRange = null;
				description = null;
				paramCategory = null;
				paramValidator = null;
				paramValue = null;
			}

			String message = "FMS Integration Service Configuration parameters have been successfully loaded from the excel sheets.";
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(message, className, "loadFMSIntegrationConfigSheet")
					.writeLog();
			InterfaceStatus.status.setTibcoIntegrationConfigurationModule(true);
			isLoaded = true;

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			isLoaded = false;
		}

		return isLoaded;

	}

	private XSSFSheet getTibcoConfigSheet() {
		return this.tibcoIntegrationConfigSheet;
	}

	public XSSFWorkbook getWorkbook() {
		return workbook;
	}

	public XSSFSheet getConfigSettingsSheet() {
		return configSettingsSheet;
	}

	public XSSFSheet getDatabaseSheet() {
		return databaseSheet;
	}

	public XSSFSheet getOamClientSheet() {
		return oamClientSheet;
	}

	public XSSFSheet getAlarmSheet() {
		return alarmSheet;
	}

	public XSSFSheet getKafkaConfigSheet() {
		return kafkaConfigSheet;
	}

	/**
	 * this method used to reset the Configuration Parameter sheet of TRM.
	 */
	public void reset() {
		this.configSettingsSheet = null;
		this.databaseSheet = null;
		this.workbook = null;
		this.alarmSheet = null;
		this.kafkaConfigSheet = null;
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

	public XSSFSheet getNotificationConfigSheet() {
		return notificationConfigSheet;
	}

	public XSSFSheet getTibcoIntegrationConfigSheet() {
		return tibcoIntegrationConfigSheet;
	}

	public XSSFSheet getFmsIntegrationConfigSheet() {
		return fmsIntegrationConfigSheet;
	}

}
