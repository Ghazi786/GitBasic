package com.jio.crm.dms.threadpoolfactory;

import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.telco.framework.pool.PooledObject;
import com.jio.telco.framework.pool.impl.DefaultPooledObject;

public class ResttalkBuilderPoolFactory extends ObjectPoolFactory<RestTalkBuilder> {

	private static final String OBJECT_NAME = "ClearCodeAsnPojo";
	private static final String CLASS_NAME = ResttalkBuilderPoolFactory.class.getSimpleName();
	public ResttalkBuilderPoolFactory() {
		super(OBJECT_NAME, CLASS_NAME);
	}

	


	@Override
	public PooledObject<RestTalkBuilder> makeObject() throws Exception {
		
		return new DefaultPooledObject<>(new RestTalkBuilder());
	}
}
