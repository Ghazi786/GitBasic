package com.jio.crm.dms.countermanager;

import java.io.IOException;

/**
 * 
 * This interface content some of interface that are being exposed over Mbean
 * 
 * @author Ashish14.Gupta
 *
 */

public interface CounterManagerMBean {

  /**
   * start the purging of counters on a regular basis
   * 
   * @param duration
   */
  public void startCounterPurging(long duration);

  /**
   * return all the counters or by category in a format to be used by CLI
   * 
   * @param cntrType
   *            the category of counters
   * @return the counters
   */
  public String fetchCounterDescriptionForSpecificType(String cntrType);

  /**
   * stop the purging of the counters
   */
  public void stopCounterPurging();

  /**
   * @return return the current purge time duration
   */
  public long getCounterPurgeDuration();

  /**
   * dumps all the performance counters on a pre-defined path
   * 
   * @return the status (whether dumping was successful or failed)
   */
  public boolean dumpCounters();

  /**
   * return all the performance counters used by OAM
   * 
   * @return the counters
   */
  public String fetchCounterAsXml();

  /**
   * reset all counters or by category
   * 
   * @param cntrType
   *            category name (or bulk to reset all counters)
   * @param sentByCli
   *            flag to indicate if the reset request is from CLI/OAM
   * @return request status
   */
  public int resetCounterForSpecificType(String cntrType, boolean sentByCli);

  /**
   * return all the counters or by category in XML format to be used by OAM
   * 
   * @param cntrType
   *            the category of counters
   * @return the counters
   */
  public String fetchCounterForSpecificTypeAsXml(String cntrType);

  /**
   * return all the counters or by category in CSV format
   * 
   * @param cntrType
   *            the category of counters
   * @return the counters
 * @throws IOException 
   */
  public String fetchAllCountersAsCSV() throws IOException ;

}
