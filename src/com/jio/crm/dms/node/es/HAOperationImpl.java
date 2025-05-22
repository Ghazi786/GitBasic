package com.jio.crm.dms.node.es;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;
import com.jio.crm.dms.node.es.ESConstants;

public class HAOperationImpl implements HAOperation {
	@Override
	public void dumpEventData(String identifier, String eventData) {

		final String methodName = "dumpEventData";

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				"Writing Data in ES = ",
				this.getClass().getName(), methodName).writeLog();

		try {
			IndexResponse response = ESConnection
					.getInstance().getClient().index(
							new IndexRequest(ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_NAME
									+ ESConstants.HA_TYPE + "").id(identifier).source(eventData),
							RequestOptions.DEFAULT);

			if (String.valueOf(response.getResult()).equals(ESConstants.CREATED.toString())
					|| String.valueOf(response.getResult())
							.equals(ESConstants.UPDATED.toString())) {
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
						"Event has been dump successfully",
						this.getClass().getName(), methodName).writeLog();
			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					"Error in event dump",
					this.getClass().getName(), methodName).writeLog();
		}
	}

	@Override
	public String getEventData(String identifier) {

		final String methodName = "getEventData";

		String jsonString = null;

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				"ES id "+ identifier,
				this.getClass().getName(), methodName).writeLog();
		try {
			
			GetResponse response = ESConnection.getInstance().getClient().get(new GetRequest(
					ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_NAME + ESConstants.HA_TYPE + "",
					identifier), RequestOptions.DEFAULT);
			
			if (response.isExists()) {
				jsonString = response.getSourceAsString();
			} else {
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
						"view event data "+ identifier,
						this.getClass().getName(), methodName).writeLog();
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					"error in view event data "+ identifier,
					this.getClass().getName(), methodName).writeLog();
		}
		return jsonString;
	}

	@Override
	public void removeEventData(String identifier) {

		final String methodName = "removeEventData";

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				"Deleting event data"+ identifier,
				this.getClass().getName(), methodName).writeLog();

		try {

			DeleteResponse response = ESConnection.getInstance().getClient().delete(new DeleteRequest(
					ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_NAME + ESConstants.HA_TYPE + "",
					identifier), RequestOptions.DEFAULT);

			if (String.valueOf(response.getResult())
					.equals(ESConstants.DELETED.toString())) {
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
						"event data is successfully removed"+ identifier,
						this.getClass().getName(), methodName).writeLog();
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					"error in deleting event data"+ identifier,
					this.getClass().getName(), methodName).writeLog();
		}

	}

	@Override
	public List<String> getAllEventData() {

		final String methodName = "getAllEventData";

		ArrayList<String> identifierList = new ArrayList<>();

		try {

			SearchRequest searchRequest = new SearchRequest(
					ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_NAME + ESConstants.HA_TYPE + "");

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(QueryBuilders.matchAllQuery()).size(500);
			searchRequest.source(searchSourceBuilder);
			
			SearchResponse response = ESConnection.getInstance().getClient()
					.search(searchRequest, RequestOptions.DEFAULT);

			for (SearchHit hit : response.getHits()) {
				String eventStr = hit.getSourceAsString();
				identifierList.add(eventStr);
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
						"all event data ids",
						this.getClass().getName(), methodName).writeLog();
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					"error in list event ids",
					this.getClass().getName(), methodName).writeLog();
		}
		return identifierList;
	}
}
