package com.jio.crm.dms.utils;

import java.io.File;

/**
 * This class contains Constants which are used for Mbean manager and dump
 * configuration
 * 
 * @author Kiran.Jangid
 */

public class MBeanParameters {

	public static final String DUMP_PATH_PARAMS = System.getProperty("user.dir") + File.separator + ".."
			+ File.separator + "dumps" + File.separator + "configuration_parameters";
	public static final String DUMP_PATH_COUNTERS = System.getProperty("user.dir") + File.separator + ".."
			+ File.separator + "dumps" + File.separator + File.separator + "counters";

	public static final String DUMP_COUNTERS_FILE_PREFIX = "_Counter_";

	public static final String NODE_NAME = "dapp";
	public static final String DUMP_PARAMS_FILE_PREFIX = "_ConfigParam_";
	public static final String DUMP_FILE_EXTENSION_XML = ".xml";

	public static final String XML = "XML_";
	public static final String MBEAN_NAME_PARAMS = "com.rjil.dapp.configurationManager:rtSDP_dapp=RuntimeConfigurationEngineMBean";
	public static final String MBEAN_NAME_COUNTERS = "com.rjil.dapp.countermanager:rtSDP_dapp=CounterManagerMBean";
	public static final String MBEAN_NAME_STATISTICS = "com.rjil.dapp.rest:rtSDP_dapp=StatisticsMBean";

	private MBeanParameters() {

	}
}
