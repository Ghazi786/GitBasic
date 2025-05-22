package com.jio.crm.dms.core;

import com.google.gson.JsonObject;

public abstract class PhabricatorMapperImpl<M> implements Mapper<JsonObject,M>{

	private Class<M> type;
	public PhabricatorMapperImpl(Class<M> type) {
		this.type = type;
	}
	public Class<M> getType() {
		return type;
	}

}
