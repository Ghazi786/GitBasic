package com.jio.crm.dms.alarmmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;
import com.jio.crm.dms.utils.Constants;
import com.rjil.management.alarm.AlarmUtils;
import com.rjil.management.alarm.db.AlarmDBOperation;
import com.rjil.management.alarm.db.AlarmInstancePojo;

public class customAlarmService implements AlarmDBOperation {
	
	static customAlarmService alarmService = new customAlarmService();
	String classmethod = "customAlarmService";
	
	@Override
	public boolean deleteAlarmInstance(String alarmId) {
		try {
			boolean status = false;
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("in delete alarm instance by alarm ID ", this.getClass().getName(), classmethod)
					.writeLog();
			


			RestHighLevelClient client = ESConnection.getInstance().getClient();
			DeleteRequest request = new DeleteRequest(Constants.ALARMMANAGERINDEXNAME.toLowerCase(),
					alarmId);

			DeleteResponse deleteResponse = client.delete(request,
					RequestOptions.DEFAULT);
			if (String.valueOf(deleteResponse.getResult()).equals(Constants.DELETED)) {
			
				status = true;
			} else {
				status = false;
			}

			if (status) {
				RefreshRequest refreshRequest = new RefreshRequest(
						Constants.ALARMMANAGERINDEXNAME.toLowerCase());
				try {
					client.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
				} catch (IOException e) {
					
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeExceptionLog();
				}
				AlarmUtils.logInfo(new StringBuilder().append("Response status of delete operation of alarm with id = ")
						.append(alarmId).append(" is = ") + " true");
			} else {
				AlarmUtils.logError("Error occurred while deleting alarm instance with id = " + alarmId);

			}

			return status;
		} catch (Exception e) {
			AlarmUtils.logException(e);
			return true;
		}

	}

	@Override
	public boolean deleteAlarmInstances(List<String> arg0) {
		return false;
	}

	@Override
	public boolean deleteAlarmInstancesByName(String alarmName) {
		
		MatchQueryBuilder query = QueryBuilders.matchQuery(Constants.ALARM_NAME_FIELD, alarmName);
		
		RestHighLevelClient client = ESConnection.getInstance().getClient();
		DeleteByQueryRequest request = new DeleteByQueryRequest(
				Constants.ALARMMANAGERINDEXNAME.toLowerCase());
		request.setQuery(query);
		try {
			BulkByScrollResponse bulkResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
			if (bulkResponse.getDeleted() > 0) {
				
				RefreshRequest refreshRequest = new RefreshRequest(
						Constants.ALARMMANAGERINDEXNAME.toLowerCase());
				client.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
			} else {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
			}
		} catch (IOException e) {
			AlarmUtils.logException(e);
		}
		return true;
	}

	@Override
	public List<AlarmInstancePojo> readAlarmInstances() {
		AlarmUtils.logInfo("Request received for getting list of all alarms from DB");
		List<AlarmInstancePojo> listOfAlarms = new ArrayList<>();
		try {
			
			RestHighLevelClient client = ESConnection.getInstance().getClient();

			QueryBuilder query = QueryBuilders.matchAllQuery();

			SearchRequest searchRequest = new SearchRequest(Constants.ALARMMANAGERINDEXNAME.toLowerCase());
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(query);

			searchSourceBuilder.size(Constants.INDEX_MAX_RESULT_WINDOW);
			searchRequest.source(searchSourceBuilder);

			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

			SearchHits hits = response.getHits();
			if (hits.getHits().length == 0) {
				AlarmUtils.logInfo("Returned list of alarms from DB is empty");
				return listOfAlarms;
			}
			SearchHit[] searchHits = hits.getHits();
			for (int i = 0; i < searchHits.length; i++) {
				SearchHit searchHit = searchHits[i];
				Map<String, Object> sourceMap = searchHit.getSourceAsMap();
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("source map" + sourceMap.toString(), this.getClass().getName(), "readAlarmInstances").writeLog();

				Object countObj = sourceMap.get(Constants.ALARM_COUNT_FIELD);
				long count;
				if (countObj instanceof Integer)
					count = (int) countObj;
				else
					count = (long) countObj;
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder("alrarm id delete >>>>>> " + String.valueOf(searchHit.getId()), this.getClass().getName(), "readAlarmInstances").writeLog();

				AlarmInstancePojo pojo = new AlarmInstancePojo(String.valueOf(searchHit.getId()),
						String.valueOf(sourceMap.get(Constants.ALARM_NAME_FIELD)), count,
						new Date((long) sourceMap.get(Constants.ALARM_TS_FIELD)));
				listOfAlarms.add(pojo);
			}
			AlarmUtils.logInfo(
					new StringBuilder().append("Successfully returned list of all active alarms from DB. List[")
							.append(listOfAlarms.size()).append("]:").append(listOfAlarms).toString());
		} catch (Exception e) {
			AlarmUtils.logError("Error occured while getting list of all active alarms from DB");
			AlarmUtils.logException(e);
		}
		return listOfAlarms;
	}

	@Override
	public boolean writeAlarmInstance(String alarmId, String alarmName, Date timestamp, long count) {
		Map<String, Object> sourceMap = new HashMap<>();
		sourceMap.put(Constants.ALARM_NAME_FIELD, alarmName);
		sourceMap.put(Constants.ALARM_TS_FIELD, timestamp.getTime());
		sourceMap.put(Constants.ALARM_COUNT_FIELD, count);

		try {
			
			RestHighLevelClient client = ESConnection.getInstance().getClient();
	
			IndexRequest indexrequest = new IndexRequest(Constants.ALARMMANAGERINDEXNAME.toLowerCase())
					.id(alarmId).source(sourceMap);

			IndexResponse indexResponse = client.index(indexrequest, RequestOptions.DEFAULT);

			if (String.valueOf(indexResponse.getResult()).equals(Constants.CREATED)) {
				RefreshRequest request = new RefreshRequest(Constants.ALARMMANAGERINDEXNAME.toLowerCase());
				client.indices().refresh(request, RequestOptions.DEFAULT);
				AlarmUtils.logInfo(new StringBuilder().append("Alarm instance <id:").append(alarmId)
						.append("> has been written in database").toString());

				return true;
			} else {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder("Alarm instance <id:" + alarmId + "> has been written in database",this.getClass().getName(), classmethod)
						.writeLog();

				return false;
			}

		} catch (Exception e) {
			AlarmUtils.logException(e);
			return false;
		}
		
	}
}
