package com.jio.crm.dms.rest;

/**
 * @author Pramod.Jundre, Puspesh.Prakash
 *
 */
public interface StatisticsMBean {
	
	public boolean fetchJettyStats();
	
	public boolean fetchPoolingStats();
	
	public String fetchPoolStatistics();
}
