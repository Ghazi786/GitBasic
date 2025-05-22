package com.jio.crm.dms.serviceImplementation;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESOperationImpl;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.serviceInterface.RecordInterface;
import com.jio.crm.dms.threads.RecordProcess;
import com.jio.crm.dms.utils.Ranked;
import com.jio.telco.framework.pool.PoolingManager;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.bean.ColumnPositionMappingStrategy;
import au.com.bytecode.opencsv.bean.CsvToBean;

public class RecordService implements RecordInterface {
	private static RecordService service;

	public static RecordService getInstance() {
		if (service == null) {
			synchronized (RecordService.class) {

				service = new RecordService();

			}
		}
		return service;
	}

	@SuppressWarnings("unused")
	@Override
	public void processRecord(CSVReader reader) {
		int x = 0;

		try {

			CsvToBean<Ranked> csv = new CsvToBean<>();
			List<Ranked> list = csv.parse(setColumMapping(), reader);
			for (Ranked object : list) {

				ESOperationImpl.getInstance().addRecord(object);

				x++;
			}
			reader.close();
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

	}

	private static ColumnPositionMappingStrategy<Ranked> setColumMapping() {
		ColumnPositionMappingStrategy<Ranked> strategy = new ColumnPositionMappingStrategy<>();
		strategy.setType(Ranked.class);
		String[] columns = new String[] { "rank", "name", "prm_id", "parent_name", "parent_prm_id", "circle", "city",
				"jio_point", "jio_centre", "product_type", "no_of_sale", "total_amount" };
		strategy.setColumnMapping(columns);
		return strategy;
	}

	@Override
	public void calculateToken(JSONObject recordJson, String id) throws Exception {
		try {
			RecordProcess recordProcess;

			recordProcess = (RecordProcess) PoolingManager.getPoolingManager().borrowObject(RecordProcess.class);
			recordProcess.setRankData(recordJson);
			recordProcess.setId(id);
			DmsBootStrapper.getInstance().getDappExecutor().execute(recordProcess);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
	}

}
