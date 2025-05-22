package com.jio.crm.dms.serviceInterface;

import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;

public interface RecordInterface {

	void processRecord(CSVReader file);

	void calculateToken(JSONObject recordJson,String id) throws Exception;
}
