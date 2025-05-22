package com.jio.crm.dms.registry;

public interface Observer {

	public void update(String query,String httpBody,String method,String serverState);
}
