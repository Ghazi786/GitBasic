package com.jio.crm.dms.utils;

/**
 * Define common functionaly for ClockResolution Implementations.
 *
 * @author pechague
 *
 */
public abstract class AbstractClockResolution {


    protected long getSystemMilliseconds() {
      return System.currentTimeMillis();
    }

}
