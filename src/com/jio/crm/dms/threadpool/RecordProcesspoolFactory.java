package com.jio.crm.dms.threadpool;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.threads.RecordProcess;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.PooledObjectFactory;
import com.jio.telco.framework.pool.impl.DefaultPooledObject;

/*
 * Dummy pool factory to start the thread pool with User defined thread passed
 */
public class RecordProcesspoolFactory implements PooledObjectFactory<RecordProcess> {

	@Override
	public void activateObject(PooledObject<RecordProcess> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				Constants.EXECUTING + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
	}

	@Override
	public void destroyObject(PooledObject<RecordProcess> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				Constants.EXECUTING + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
	}

	@Override
	public PooledObject<RecordProcess> makeObject() throws Exception {
		return new DefaultPooledObject<>(new RecordProcess());
	}

	@Override
	public void passivateObject(PooledObject<RecordProcess> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				Constants.EXECUTING + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
	}

	@Override
	public boolean validateObject(PooledObject<RecordProcess> arg0) {
		return false;
	}

}
