package com.jio.crm.dms.countermanager;

import static com.jio.crm.dms.configurationmanager.InterfaceStatus.status;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.writer.CSVEntryConverter;
import com.googlecode.jcsv.writer.CSVWriter;
import com.googlecode.jcsv.writer.internal.CSVWriterBuilder;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.rest.xmlparser.Counter;
import com.jio.crm.dms.rest.xmlparser.CounterCSV;
import com.jio.crm.dms.rest.xmlparser.Counters;
import com.jio.crm.dms.rest.xmlparser.Param;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.DappParameters;

/**
 * this class is singleton class thread safe and load lazily. its implements
 * CounterManagerMBean.
 * 
 * @author Ashish14.Gupta
 *
 */
public class CounterManager implements CounterManagerMBean {

	private CSVCounterEntryConverter csConverter = new CSVCounterEntryConverter();
	private Serializer serializer = new Persister();
	private static CounterManager trmCounterInstance = null;
	private long counterPurgeDuration = ConfigParamsEnum.COUNTER_PURGE_DURATION.getLongValue();
	private TimerTask counterTimerTask = null;

	private CounterManager() {
		status.setCounterModule(true);
	}

	public static synchronized CounterManager getInstance() {
		if (trmCounterInstance == null) {
			trmCounterInstance = new CounterManager();
		}
		return trmCounterInstance;
	}

	/**
	 * Start the Mbean Service for Monitoring counter
	 */
	public void startCounterMbeanService() {

		try {
			MBeanServer counterMbeanServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName adapterName = new ObjectName(DappParameters.MBEAN_NAME_COUNTERS);
			counterMbeanServer.registerMBean(this, adapterName);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "startCounterMbeanService")
					.writeExceptionLog();
		}
	}

	/**
	 * Stop the Mbean Service for Counter Management
	 */
	public void stopCounterMbeanService() {
		try {
			ManagementFactory.getPlatformMBeanServer()
					.unregisterMBean(new ObjectName(DappParameters.MBEAN_NAME_COUNTERS));
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "stopCounterMbeanService")
					.writeExceptionLog();
		}
	}

	public String getCounterCategory() {

		StringBuilder sbr = new StringBuilder();
		sbr.append("List of Counter Category \n").append("###################################### \n\n");

		for (CounterCategoryEnum counterCategory : CounterCategoryEnum.values())
			sbr.append(counterCategory.getCliName()).append("\n");

		sbr.append("###################################### \n");

		return sbr.toString();
	}

	@Override
	public void startCounterPurging(long duration) {
		Timer timer = new Timer();
		if (counterTimerTask != null) {
			counterTimerTask.cancel();
			counterTimerTask = null;
		}
		counterTimerTask = new CounterTimerTask();
		counterPurgeDuration = duration;
		timer.schedule(counterTimerTask, duration * 1000, duration * 1000);
	}

	@Override
	public void stopCounterPurging() {
		if (counterTimerTask != null) {
			counterTimerTask.cancel();
			counterTimerTask = null;
		} else {
			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Counter already stopped").writeLog();
		}
		this.counterPurgeDuration = -1;
	}

	@Override
	public boolean dumpCounters() {
		boolean isSuccess = false;
		Format formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

		String fileFormatXML = DappParameters.NODE_NAME + DappParameters.DUMP_COUNTERS_FILE_PREFIX + DappParameters.XML
				+ formatter.format(new Date()) + DappParameters.DUMP_FILE_EXTENSION_XML;
		try (FileWriter fileWriter = new FileWriter(DappParameters.DUMP_PATH_COUNTERS + File.separator + fileFormatXML,
				false)) {

			File file = new File(DappParameters.DUMP_PATH_COUNTERS);
			if (!file.exists())
				file.mkdir();
			String countersAsXML = this.fetchCounterAsXml();

			fileWriter.write(countersAsXML);
			
			isSuccess = true;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "dumpCounters")
					.writeExceptionLog();
		}
		return isSuccess;
	}

	@Override
	public String fetchCounterAsXml() {
		Counters counterList = new Counters();
		Counter counter = new Counter();
		counter.setCounterType(DmsBootStrapper.getInstance().getComponentId());
		CounterNameEnum[] counterNames = CounterNameEnum.values();
		for (CounterNameEnum counterName : counterNames)
			counter.addToParamList(new Param(counterName.name(), Long.toString(counterName.getValue())));
		counterList.addToCounterList(counter);
		StringWriter stringWriter = new StringWriter();
		try {
			this.serializer.write(counterList, stringWriter);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "Error fetch counter ");
			return null;
		}
		return stringWriter.toString();
	}

	@Override
	public String fetchAllCountersAsCSV() throws IOException {
		Writer writer = new StringWriter();
		CSVWriter<CounterCSV> csvWriter = new CSVWriterBuilder<CounterCSV>(writer).strategy(CSVStrategy.UK_DEFAULT)
				.entryConverter(this.csConverter).build();
		CounterCategoryEnum[] counterCategories = CounterCategoryEnum.values();
		CounterCSV cntrCsv;
		for (CounterCategoryEnum counterCategory : counterCategories) {
			cntrCsv = new CounterCSV();
			Set<Entry<String, Long>> set = counterCategory.getCounterMap().entrySet();
			for (Entry<String, Long> entry : set)
				cntrCsv.addToCounterList(new CounterCSV(counterCategory.name(), entry.getKey(), entry.getValue()));
			csvWriter.writeAll(cntrCsv.getCounterList());
		}
		return CounterConstants.CSV_HEADER_COUNTERS + writer.toString();
	}

	@Override
	public String fetchCounterDescriptionForSpecificType(String cntrType) {
		if (Constants.BULK.equals(cntrType)) {
			CounterCategoryEnum[] counterCategories = CounterCategoryEnum.values();
			StringBuilder counterDump = new StringBuilder();
			for (CounterCategoryEnum counterCategory : counterCategories)
				counterDump.append(counterCategory.formatCounters());
			return counterDump.toString();
		}
		CounterCategoryEnum counterCategory = CounterCategoryEnum.getEnumFromCliArg(cntrType);
		if (counterCategory == null)
			return null;
		return counterCategory.formatCounters().toString();
	}

	/**
	 * 
	 */

	@Override
	public int resetCounterForSpecificType(String cntrType, boolean sentByCli) {

		if (CounterConstants.BULK.equals(cntrType)) {
			CounterCategoryEnum[] counterCategories = CounterCategoryEnum.values();
			for (CounterCategoryEnum counterCategory : counterCategories)
				counterCategory.reset();
			DappLoggerService.GENERAL_INFO_LOG
					.getInfoLogBuilder("All performance counters have been reset successfully");
			return 0;
		}

		CounterCategoryEnum counterCategory = null;

		if (sentByCli)
			counterCategory = CounterCategoryEnum.getEnumFromCliArg(cntrType);
		else {
			try {
				counterCategory = CounterCategoryEnum.valueOf(cntrType);
			} catch (IllegalArgumentException e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getExceptionLogBuilder(e, "Error un-registering the MBean, null pointer exception\\n")
						.writeExceptionLog();
			}
		}

		if (counterCategory == null) {
			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("Counter category '" + cntrType + "' is unknown : ")
					.writeLog();
			return -1;
		}

		counterCategory.reset();
		return 0;
	}

	@Override
	public String fetchCounterForSpecificTypeAsXml(String cntrType) {
		Counters counterList = new Counters();
		if (CounterConstants.BULK.equals(cntrType)) {
			CounterCategoryEnum[] counterCategories = CounterCategoryEnum.values();
			for (CounterCategoryEnum counterCategory : counterCategories)
				counterList.addToCounterList(getCounter(counterCategory));
		} else {
			CounterCategoryEnum counterCategory = CounterCategoryEnum.valueOf(cntrType);
			counterList.addToCounterList(getCounter(counterCategory));
		}
		StringWriter stringWriter = new StringWriter();
		try {
			this.serializer.write(counterList, stringWriter);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "Error fetch counter ");
			return null;
		}
		return stringWriter.toString();
	}

	private Counter getCounter(CounterCategoryEnum counterCategory) {
		Counter counter = new Counter();
		counter.setCounterType(counterCategory.name());
		Set<Map.Entry<String, Long>> set = counterCategory.getCounterMap().entrySet();
		for (Entry<String, Long> entry : set)
			counter.addToParamList(new Param(entry.getKey(), Long.toString(entry.getValue())));
		return counter;
	}

	@Override
	public long getCounterPurgeDuration() {
		return this.counterPurgeDuration;
	}

	public String dumpCounters(String cntrType) {
		boolean isSuccess = false;
		Format formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

		String fileFormatXML = DappParameters.NODE_NAME + DappParameters.DUMP_COUNTERS_FILE_PREFIX + DappParameters.XML
				+ formatter.format(new Date()) + DappParameters.DUMP_FILE_EXTENSION_XML;

		try (FileWriter fileWriter = new FileWriter(DappParameters.DUMP_PATH_COUNTERS + File.separator + fileFormatXML,
				false)) {

			File file = new File(DappParameters.DUMP_PATH_COUNTERS);
			if (!file.exists())
				file.mkdir();
			String countersAsXML = this.fetchCounterDescriptionForSpecificType(cntrType);
			if (countersAsXML == null)
				return null;

			fileWriter.write(countersAsXML);
			
			isSuccess = true;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "dumpCounters")
					.writeExceptionLog();
		}
		return Boolean.toString(isSuccess);
	}

	private class CSVCounterEntryConverter implements CSVEntryConverter<CounterCSV> {
		@Override
		public String[] convertEntry(CounterCSV counter) {
			String[] cntrCsv = new String[3];
			cntrCsv[0] = counter.getCounterType();
			cntrCsv[1] = counter.getCounterName();
			cntrCsv[2] = String.valueOf(counter.getCount());
			return cntrCsv;
		}
	}

}