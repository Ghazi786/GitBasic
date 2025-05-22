package com.jio.crm.dms.alarmmanager;


import com.jio.crm.dms.logger.DappLoggerService;
import com.rjil.management.alarm.ApplicationMemoryUsageListener;



public class MemoryOverflowListener implements ApplicationMemoryUsageListener {
	

	@Override
	public void memoryUsage(String alarmName, long used, long max, long percentage) {

		DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder("Memory [used=" + used + " bytes, max=" + max + " bytes, percentage=" + percentage + " %]", this.getClass().getName(), "memoryUsage");

	}
}
