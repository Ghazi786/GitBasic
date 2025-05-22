package com.jio.crm.dms.ha;

import com.jio.crm.dms.core.BaseEventBean;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;

public class EventDumpTask implements Runnable {

	private BaseEventBean eventData;

	/**
	 * 
	 * @param eventData
	 */

	public EventDumpTask(BaseEventBean eventData) {
		this.eventData = eventData;
	}

	public EventDumpTask() {
		/**
		 * Default Constructor
		 */
	}

	@Override
	public void run() {

		String identifier = eventData.getFlowId() + "_" + eventData.getBranchId();
		String eventDataStr = eventData.toString();
		
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();

		ESConnection.getInstance().getHaOperation().dumpEventData(identifier, eventDataStr);

	}
	
}
