package com.jio.crm.dms.countermanager;

import java.util.HashMap;

import com.jio.crm.dms.logger.DappLoggerService;

/**
 * 
 * This Enum defines Counter Category and Counter name
 * 
 * @author Ashish14.Gupta
 *
 */
public enum CounterCategoryEnum {

	ES_CLIENT("es_operation", CounterNameEnum.CNTR_ES_INDEX_CREATION_REQUESTS,
			CounterNameEnum.CNTR_ES_INDEX_CREATION_SUCCESS, CounterNameEnum.CNTR_ES_INDEX_CREATION_FAILURE,
			CounterNameEnum.CNTR_ES_DOCUMENTS_CREATION_SUCCESS, CounterNameEnum.CNTR_ES_DOCUMENTS_CREATION_FAILURE,
			CounterNameEnum.CNTR_ES_SEARCH_REQUESTS, CounterNameEnum.CNTR_ES_SEARCH_SUCCESS,
			CounterNameEnum.CNTR_ES_SEARCH_FAILURE),

	REST_CALL("rest_call_request", CounterNameEnum.CNTR_SEND_REST_REQUEST, CounterNameEnum.CNTR_SEND_REST_ACCEPTED,
			CounterNameEnum.CNTR_SEND_REST_SUCCESS, CounterNameEnum.CNTR_SEND_REST_FAILURE);

	private CounterNameEnum[] counterNames;
	private String cliName;
	private static HashMap<String, CounterCategoryEnum> cliNameVsEnumMap = new HashMap<>();
	private static final int FORMAT_BORDER = 55;

	static {
		CounterCategoryEnum[] counterCategories = CounterCategoryEnum.values();
		for (CounterCategoryEnum counterCategory : counterCategories)
			cliNameVsEnumMap.put(counterCategory.getCliName(), counterCategory);
	}

	private CounterCategoryEnum(String cliName, CounterNameEnum... counterNames) {
		this.cliName = cliName;
		this.counterNames = counterNames;
	}

	/**
	 * @return all the counters in this category in a map with key as counter name
	 *         and value as its current value
	 */
	HashMap<String, Long> getCounterMap() {
		HashMap<String, Long> cmap = new HashMap<>();
		for (CounterNameEnum counterNameEnum : this.counterNames)
			cmap.put(counterNameEnum.name(), counterNameEnum.getValue());
		return cmap;
	}

	/**
	 * reset all the performance counters that belongs to this category
	 */
	void reset() {
		for (CounterNameEnum counterName : this.counterNames)
			counterName.reset();
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder("All performance counters of category '" + this.name() + "' have been reset",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	/**
	 * @return the name to be used as a CLI argument to fetch/reset counters by
	 *         category
	 */
	String getCliName() {
		return cliName;
	}

	/**
	 * returns the enumeration instance from the name of the CLI argument
	 * 
	 * @param arg
	 *            name of the CLI argument
	 * @return the enumeration instance from the name of the CLI argument
	 */
	static CounterCategoryEnum getEnumFromCliArg(String arg) {
		return cliNameVsEnumMap.get(arg);
	}

	/**
	 * @return return the counters in a readable format to be displayed in CLI
	 */
	StringBuilder formatCounters() {
		StringBuilder counterDump = new StringBuilder();
		String s;
		String format;
		String key;
		for (CounterNameEnum counterNameEnum : this.counterNames) {
			key = counterNameEnum.name();
			format = key + "%" + (FORMAT_BORDER - key.length()) + "s";
			s = String.format(format, "=   ");
			counterDump.append(s + counterNameEnum.getValue() + "\n");
		}
		counterDump.insert(0, "\n    List of Counters of type = " + this.cliName + "\n")
				.append("######################################\n");
		return counterDump;
	}

}
