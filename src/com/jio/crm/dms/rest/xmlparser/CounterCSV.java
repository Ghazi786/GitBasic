package com.jio.crm.dms.rest.xmlparser;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ashish14.Gupta
 *
 */

public class CounterCSV {

  private String counterType;
  private String counterName;
  private long count;
  private ArrayList<CounterCSV> counterList;

  /**
   * Counter CVS Constructor
   */

  public CounterCSV() {
    counterList = new ArrayList<>();

  }

  /**
   * Arg Constructor
   * 
   * @param counterType
   * @param counterName
   * @param count
   */

  public CounterCSV(String counterType, String counterName, long count) {
    this.counterType = counterType;
    this.setCounterName(counterName);
    this.setCount(count);
  }

  /**
   * 
   * @return
   */

  public String getCounterType() {
    return counterType;
  }

  /**
   * 
   * @param counterType
   */

  public void setCounterType(String counterType) {
    this.counterType = counterType;
  }

  /**
   * 
   * @return
   */

  public String getCounterName() {
    return counterName;
  }

  /**
   * 
   * @param counterName
   */

  public void setCounterName(String counterName) {
    this.counterName = counterName;
  }

  /**
   * 
   * @return
   */

  public long getCount() {
    return count;
  }

  /**
   * 
   * @param count
   */

  public void setCount(long count) {
    this.count = count;
  }

  /**
   * 
   * @return
   */

  public List<CounterCSV> getCounterList() {
    return counterList;
  }

  /**
   * 
   * @param counterCsv
   */

  public void addToCounterList(CounterCSV counterCsv) {
    counterList.add(counterCsv);
  }
}
