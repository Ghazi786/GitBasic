package com.jio.crm.dms.alarmmanager;

import org.apache.logging.log4j.Logger;

import com.jio.crm.dms.logger.DappLogManager;
import com.rjil.management.alarm.AlarmNotificationFailureIntf;

public class AlarmNotificationFailure implements AlarmNotificationFailureIntf{

	private Logger logger =DappLogManager.getInstance().getLogger();
	/**
	 * callback method to notify an alarm notification failure
	 * 
	 * @param alarmId
	 *            alarm identifier
	 * @param alarmName
	 *            alarm name
	 */
	@Override
	public void alarmNotifyFailure(String alarmId, String alarmName) {
	logger.error("Alarm Notify Failure");

	}
}
