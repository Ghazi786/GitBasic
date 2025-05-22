package com.jio.crm.dms.threadpoolfactory;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.PooledObjectFactory;

public class ObjectPoolFactory<T> implements PooledObjectFactory<T> {


	private String objectname = null;
	private String classname = null;
	
	public ObjectPoolFactory(String objName, String classsName) {
		this.objectname = objName;
		this.classname = classsName;
	}

	@Override
	public void activateObject(PooledObject<T> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				Constants.EXECUTING + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
	}

	@Override
	public void destroyObject(PooledObject<T> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				Constants.EXECUTING + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
	}

	@Override
	public PooledObject<T> makeObject() throws Exception {
		
		return null;
	}

	@Override
	public void passivateObject(PooledObject<T> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				Constants.EXECUTING + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
	}

	@Override
	public boolean validateObject(PooledObject<T> arg0) {
		
		return false;
	}

}
