package com.jio.crm.dms.node.es;

import java.util.List;

public interface HAOperation {
	/**
	 * Method to Dump Event in ES
	 * 
	 * @param identifier
	 * @param eventData
	 */

	void dumpEventData(String identifier, String eventData);

	/**
	 * Method to get Event Data from ES
	 * 
	 * @param identifier
	 * @return
	 */

	String getEventData(String identifier);

	/**
	 * It will return all Events Details From ES
	 * 
	 * @return
	 */

	List<String> getAllEventData();

	/**
	 * 
	 * It will delete event data from ES
	 * 
	 * @param identifier
	 */

	void removeEventData(String identifier);
}
