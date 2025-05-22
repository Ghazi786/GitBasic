package com.jio.crm.dms.utils;

import com.elastic.search.exception.ElasticSearchException;
import com.elastic.search.launcher.SessionFactory;
import com.elastic.search.service.Session;
import com.jio.subscriptions.modules.bean.Vendor;

public class CommonRepository {
	
	private static CommonRepository commonRepository=new CommonRepository();
	
	private CommonRepository() {
		
	}
	
	public static CommonRepository getInstance() {
		return commonRepository;
	}
	
	public Vendor getVendor(String vendorId)
			throws ElasticSearchException {
		
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		
		return session.getObject(Vendor.class, vendorId);
	}


}
