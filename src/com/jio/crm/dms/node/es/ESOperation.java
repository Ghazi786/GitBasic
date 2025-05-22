package com.jio.crm.dms.node.es;



import org.json.JSONObject;

import com.jio.crm.dms.utils.Ranked;

interface ESOperation {

	boolean addRecord(Ranked recordMetaData);
	boolean addRecordWithToken(JSONObject jsonObject);

}
