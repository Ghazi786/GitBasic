package com.jio.crm.dms.node.es;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import com.jio.blockchain.sdk.util.UniqueUUIDGenerator;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;
import com.jio.crm.dms.node.es.ESConstants;
import com.jio.crm.dms.serviceImplementation.RecordService;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.Ranked;

/***
 * 
 * @author Rishabh3.Kumar
 *
 */
public class ESOperationImpl implements ESOperation {
	public static final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

	private ESOperationImpl() {
	}

	private static ESOperationImpl esOperationImpl = new ESOperationImpl();

	public static ESOperationImpl getInstance() {

		if (esOperationImpl == null) {
			esOperationImpl = new ESOperationImpl();
		}
		return esOperationImpl;

	}

	@Override

	public boolean addRecord(Ranked recordMetaData) {
		boolean isSuccess = false;
		try {

			Date date = new Date();
			JSONObject store = new JSONObject(recordMetaData);
			store.put(ESConstants.DATE, date.getTime());
			store.put(ESConstants.TOKENASSIGNED, false);

			ESConnection.getInstance().getProcessor()
					.add(new IndexRequest(ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_TYPE)
							.id(UniqueUUIDGenerator.getInstance().getUniqueUUID())
							.source(store.toString(), XContentType.JSON));

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"error while adding records to es " + e.getMessage());
		}

		return isSuccess;
	}

	public boolean addRecordWithToken(JSONObject jsonObject, String id) throws JSONException {
		boolean isSuccess = false;

		try {
			jsonObject.put(ESConstants.TOKENASSIGNED, true);

			ESConnection.getInstance().getProcessor()
					.add(new IndexRequest(ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_TYPE)
							.id(UniqueUUIDGenerator.getInstance().getUniqueUUID())
							.source(jsonObject.toString(), XContentType.JSON));

			jsonObject.remove(Constants.TOKENS);
			ESConnection.getInstance().getProcessor()
					.add(new IndexRequest(ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_TYPE)
							.id(UniqueUUIDGenerator.getInstance().getUniqueUUID())
							.source(jsonObject.toString(), XContentType.JSON));

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"error while adding records to es " + e.getMessage());
		}
		return isSuccess;
	}

	public void addTokensToRecords(String indexName, String type, int batchSize) {
		JSONObject record;
		Date date = new Date();
		long currentMillis = date.getTime();
		long from = currentMillis - org.apache.commons.lang.time.DateUtils.MILLIS_PER_DAY;

		try {

			SearchRequest searchRequest = new SearchRequest(indexName + "_" + type);

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(QueryBuilders.boolQuery()
					.filter(QueryBuilders.rangeQuery(ESConstants.DATE).from(from).to(currentMillis))
					.filter(QueryBuilders.termQuery(ESConstants.TOKENASSIGNED, false)));
			searchSourceBuilder.sort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC);
			searchSourceBuilder.size(batchSize);
			searchRequest.source(searchSourceBuilder);
			searchRequest.scroll(new TimeValue(5000));

			SearchResponse response = ESConnection.getInstance().getClient().search(searchRequest,
					RequestOptions.DEFAULT);

			do {
				for (SearchHit hit : response.getHits().getHits()) {
					record = new JSONObject(hit.getSourceAsString());

					RecordService.getInstance().calculateToken(record, hit.getId());
				}

				response = ESConnection.getInstance().getClient().scroll(
						new SearchScrollRequest(response.getScrollId()).scroll(new TimeValue(60000)),
						RequestOptions.DEFAULT);

			} while (response.getHits().getHits().length != 0);
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"error while getting records from es to calculate token " + e.getMessage());
		}
	}

	public boolean putCSVCredits(JSONObject jsonObject) {
		boolean isSuccess = false;
		try {

			IndexRequest indexRequest = new IndexRequest(ESConstants.CACHE_INDEX + "_" + ESConstants.CACHE_TYPE);
			indexRequest.source(jsonObject.toString(), XContentType.JSON);

			IndexResponse res = ESConnection.getInstance().getClient().index(indexRequest, RequestOptions.DEFAULT);

			if (String.valueOf(res.getResult()).equals(ESConstants.CREATED)
					|| String.valueOf(res.getResult()).equals(ESConstants.UPDATED)) {

				ESConnection.getInstance().getClient().indices().refresh(
						new RefreshRequest(ESConstants.RECORD_INDEX_NAME + "_" + ESConstants.RECORD_INDEX_TYPE),
						RequestOptions.DEFAULT);

				isSuccess = true;

			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"error while adding  token calculation data to es " + e.getMessage());
		}
		return isSuccess;

	}

	public JSONObject getCSVCredits() {
		Map<String, Object> result = new HashMap<>();
		try {
			GetResponse response = ESConnection.getInstance().getClient().get(
					new GetRequest(ESConstants.CACHE_INDEX + "_" + ESConstants.CACHE_TYPE), RequestOptions.DEFAULT);

			if (response.isExists()) {
				result = response.getSourceAsMap();

			} else {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("no cache data found in es");
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"error whike getting records from es to set cache values " + e.getMessage());
		}
		return (JSONObject) result;

	}

	@Override
	public boolean addRecordWithToken(JSONObject jsonObject) {

		return false;
	}

}
