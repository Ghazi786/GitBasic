package com.jio.crm.dms.kafka.producer;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.PooledObjectFactory;
import com.jio.telco.framework.pool.impl.DefaultPooledObject;

public class UOLProducerTaskFactory implements PooledObjectFactory<UOLProducerTask> {

	@Override
	public PooledObject<UOLProducerTask> makeObject() throws Exception {
		return new DefaultPooledObject<>(new UOLProducerTask());
	}

	@Override
	public void destroyObject(PooledObject<UOLProducerTask> p) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}

	@Override
	public boolean validateObject(PooledObject<UOLProducerTask> p) {

		return false;
	}

	@Override
	public void activateObject(PooledObject<UOLProducerTask> p) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}

	@Override
	public void passivateObject(PooledObject<UOLProducerTask> p) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}
}
