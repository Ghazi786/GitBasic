package com.jio.crm.dms.threadpoolfactory;


import com.jio.telco.framework.clearcode.ClearCodeAsnPojo;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.impl.DefaultPooledObject;

public class ClearCodePoolFactory extends ObjectPoolFactory<ClearCodeAsnPojo> {

	private static final String OBJECT_NAME = "ClearCodeAsnPojo";
	private static final String CLASS_NAME = ClearCodePoolFactory.class.getSimpleName();
	public ClearCodePoolFactory() {
		super(OBJECT_NAME, CLASS_NAME);
	}

	


	@Override
	public PooledObject<ClearCodeAsnPojo> makeObject() throws Exception {
		
		return new DefaultPooledObject<>(new ClearCodeAsnPojo());
	}
}
