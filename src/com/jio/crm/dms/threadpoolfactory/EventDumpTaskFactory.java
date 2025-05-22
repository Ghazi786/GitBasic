package com.jio.crm.dms.threadpoolfactory;

import com.jio.crm.dms.ha.EventDumpTask;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.PooledObjectFactory;
import com.jio.telco.framework.pool.impl.DefaultPooledObject;

public class EventDumpTaskFactory implements PooledObjectFactory<EventDumpTask> {

	

	  @Override
	  public void activateObject(PooledObject<EventDumpTask> arg0) throws Exception {
		  DappLoggerService.GENERAL_INFO_LOG
			.getLogBuilder(
					Constants.EXECUTING + this.getClass().getName() + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeLog();
	  }

	  @Override
	  public void destroyObject(PooledObject<EventDumpTask> arg0) throws Exception {
		  DappLoggerService.GENERAL_INFO_LOG
			.getLogBuilder(
					Constants.EXECUTING + this.getClass().getName() + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeLog();
	  }

	  @Override
	  public PooledObject<EventDumpTask> makeObject() throws Exception {
	    return new DefaultPooledObject<>(new EventDumpTask());
	  }

	  @Override
	  public void passivateObject(PooledObject<EventDumpTask> arg0) throws Exception {
		  DappLoggerService.GENERAL_INFO_LOG
			.getLogBuilder(
					Constants.EXECUTING + this.getClass().getName() + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
			.writeLog();
	  }

	  @Override
	  public boolean validateObject(PooledObject<EventDumpTask> arg0) {
	    return false;
	  }
	
}
