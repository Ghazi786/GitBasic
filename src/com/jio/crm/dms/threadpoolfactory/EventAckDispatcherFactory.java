package com.jio.crm.dms.threadpoolfactory;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.PooledObjectFactory;

public class EventAckDispatcherFactory implements PooledObjectFactory<EventAckRequestDispatcher> {

	@Override
	public void activateObject(PooledObject<EventAckRequestDispatcher> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	@Override
	public void destroyObject(PooledObject<EventAckRequestDispatcher> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}

	@Override
	public PooledObject<EventAckRequestDispatcher> makeObject() throws Exception {

		return null;
	}

	@Override
	public void passivateObject(PooledObject<EventAckRequestDispatcher> arg0) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}

	@Override
	public boolean validateObject(PooledObject<EventAckRequestDispatcher> arg0) {

		return false;
	}
}
