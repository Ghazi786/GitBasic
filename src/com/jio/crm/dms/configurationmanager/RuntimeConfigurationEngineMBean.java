package com.jio.crm.dms.configurationmanager;

import java.io.IOException;
import java.util.Map;

/**
 * This class Exposed methods related to configuration via Mbeans useful for monitoring and debugging purpose
 *
 */
public interface RuntimeConfigurationEngineMBean {

	/**
	 * Dump configuration Parameter to file
	 * @return
	 */
	public boolean dumpConfigParams();
	
	/**
	 * Fetch all Configuration Parameter of DAPP in XML format
	 * @return
	 * @throws IOException
	 */
	public String fetchAllParamsAsXml();
	
	/**
	 * Update the Configuration Parameter which are run time configurable
	 * @param param
	 * @param value
	 * @param sentByCli
	 * @param isBulkset
	 * @return
	 */
	public byte updateParam(String param, String value, boolean sentByCli, boolean isBulkset);
	
	
	/**
	 * Fetch the Parameter details  or value
	 * @param param
	 * @return
	 */
	public String fetchParam(String param);
	
	
	
	/**
	 * This method return Command Line Parameter Map from  Config Param Enum
	 * @return
	 */
	public Map<String, String> getCliParamMap();
	
	
	/**
	 * this method get the product name i.e DAPP
	 * @return
	 */
	public String getProductName();
	 
	/**
	 * This method get the Current Product Version of Application
	 * @return
	 */
    public String getProductVersion();
    
    /**
     * This method delete all the log files from the log folder
     * @return
     */
    boolean deleteLogFiles();
 
    /**
     * This method get the current log level of the Application
     * @return
     */
    public String getLogLevel();
 
    /**
     * This method set the Current Log level of the Application to INFO,ERROR,DEBUG,FATAL
     * @param logLevel
     * @param changingAgent
     */
    public void setLogLevel(String logLevel);
	
    /**
     * This method dump Thread Related information in a file
     * @return
     */
    public String getAndDumpThreadStats();
	
    /**
     * This method return Core Pool Size of {@link DAPPThreadPoolExecutor}
     * @return
     */
    public String getCorePoolSize();
    
    /**
     * This method return Max Core Pool Size of {@link DAPPThreadPoolExecutor}
     * @return
     */
    public String getMaxCorePoolSize();
    
   /**
    * This method set core Pool size of {@link DAPPThreadPoolExecutor}
    * @param corePoolSize
    */
	public void setCorePoolSize(int corePoolSize);
	
	/**
	 * This method set max core Pool size of {@link DAPPThreadPoolExecutor}
	 * @param maxCorePoolSize
	 */
	public void setMaxCorePoolSize(int maxCorePoolSize);
	
	/**
	 * This method set keep alive time in millis for DAPPThreadPoolExecutor
	 * @param timeInMillis
	 */
	public void setKeepAliveTimeinMillis(long timeInMillis);
	
	/**
	 * This method return keep alive time in milliseconds of DAPPThreadPoolExecutor
	 * @return
	 */
	public long getKeepAliveTimeinMillis();
	
	public void gracefulSHutdown();
}
