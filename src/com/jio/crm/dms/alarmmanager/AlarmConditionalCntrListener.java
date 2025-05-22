package com.jio.crm.dms.alarmmanager;

import com.rjil.management.alarm.AlarmConditionCntrListener;


/**
 * Listener that is notified whenever a task is fired to fetch the counter value
 * of a conditional counter alarm
 * 
 */

		
public class AlarmConditionalCntrListener extends AlarmConditionCntrListener {

	
	@Override
	public long getCounterValue(String counterId) {
		return 0;
	}
}
