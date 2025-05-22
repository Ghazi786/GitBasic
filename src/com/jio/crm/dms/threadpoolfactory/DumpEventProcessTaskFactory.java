package com.jio.crm.dms.threadpoolfactory;

import com.jio.crm.dms.ha.DumpEventProcessTask;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.PooledObjectFactory;
import com.jio.telco.framework.pool.impl.DefaultPooledObject;

public class DumpEventProcessTaskFactory implements PooledObjectFactory<DumpEventProcessTask> {

	@Override
	public void activateObject(PooledObject<DumpEventProcessTask> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public void destroyObject(PooledObject<DumpEventProcessTask> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public PooledObject<DumpEventProcessTask> makeObject() throws Exception {
		return new DefaultPooledObject<>(new DumpEventProcessTask());
	}

	@Override
	public void passivateObject(PooledObject<DumpEventProcessTask> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public boolean validateObject(PooledObject<DumpEventProcessTask> arg0) {
		return false;
	}

}
