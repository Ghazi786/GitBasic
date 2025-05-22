package com.jio.crm.dms.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.crm.dms.core.BaseModal;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.Cursor;
import com.jio.crm.dms.core.Mapper;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;
import com.jio.crm.dms.utils.Constants;

/**
 * Abstract elastic service implementation class to do basic CRUD operations for
 * Jio Devops
 * 
 * @author Barun.rai
 * 
 */
public abstract class BaseServiceElasticImpl<E, M, T> implements BaseService<E, M, T> {

	private Mapper<Map<String, Object>, M> mapper;
	private Cursor cursor;

	public BaseServiceElasticImpl(Mapper<Map<String, Object>, M> mapper) {
		this.mapper = mapper;
	}

	/**
	 * Used to save data to elastic search database
	 * 
	 * @param obj
	 *            : M model object to be saved
	 *
	 */
	public BaseResponse<M> save(M obj) {

		BaseResponse<M> response = new BaseResponse<>();
		ObjectMapper objectMapper = new ObjectMapper();
		IndexResponse indexResponse = null;
		try {
			IndexRequest indexRequest = new IndexRequest(Constants.SUBSCRIPTIONENGINE + obj.getClass().getSimpleName());
			indexRequest.id(((BaseModal) obj).getId()).source(objectMapper.writeValueAsBytes(obj));
			indexResponse = ESConnection.getInstance().getClient().index(indexRequest, RequestOptions.DEFAULT);

			response.setData(obj);
			response.setSuccess(indexResponse.getResult().getLowercase().equals("created"));
			response.setMessage("Some message");
			response.setStatus(200);
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
		return response;
	}

	/**
	 * Used to save list of data to elastic search database
	 * 
	 * @param objects
	 *            : a list with T type objects
	 */
	public BaseResponse<List<M>> saveAll(List<M> objects) {
		ObjectMapper objectMapper = new ObjectMapper();
		BulkResponse response1 = null;
		BaseResponse<List<M>> response = new BaseResponse<>();
		try {
			BulkRequest bulkRequest = new BulkRequest();

			for (M obj : objects) {
				bulkRequest.add(new IndexRequest(Constants.SUBSCRIPTIONENGINE + obj.getClass().getSimpleName())
						.id(((BaseModal) obj).getId()).source(objectMapper.writeValueAsBytes(obj)));

			}

			response1 = ESConnection.getInstance().getClient().bulk(bulkRequest, RequestOptions.DEFAULT);
			response.setData(objects);
			response.setSuccess(!response1.hasFailures());
			response.setStatus(200);
			response.setMessage(response1.buildFailureMessage());
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
		return response;
	}

	/**
	 * Used to retrieve all T type data from database
	 * 
	 * @param c
	 *            : a class of type T
	 * @return list of data with type as T
	 * 
	 */
	public List<Map<String, Object>> getAll(Class<M> c) {

		SearchResponse response = null;
		try {
			response = ESConnection.getInstance().getClient().search(
					new SearchRequest(Constants.SUBSCRIPTIONENGINE + c.getSimpleName()), RequestOptions.DEFAULT);
		} catch (IOException e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

		List<Map<String, Object>> list = new ArrayList<>();
		response.getHits().forEach(x -> {
			x.getSourceAsMap().put("id", x.getId());
			list.add(x.getSourceAsMap());
		});

		return list;
	}

	/**
	 * Used to retrieve all T type data from database based on filters provided
	 * 
	 * @param c
	 *            : Class c of Type T
	 * @param map
	 *            : Map with key value as filter value
	 * @return list of map with filtered value based on filters provided
	 * 
	 */

	public BaseResponse<List<M>> get() {
		SearchResponse response = null;
		try {
			if (this.cursor == null) {

				SearchRequest searchRequest = new SearchRequest(
						Constants.SUBSCRIPTIONENGINE + mapper.getType().getSimpleName());
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder.size(100);
				searchRequest.source(searchSourceBuilder);
				searchRequest.scroll(new TimeValue(60000));

				response = ESConnection.getInstance().getClient().search(searchRequest, RequestOptions.DEFAULT);

				cursor = new Cursor();
				this.cursor.setAfter(response.getScrollId());

			} else {

				response = ESConnection.getInstance().getClient().scroll(
						new SearchScrollRequest(this.cursor.getAfter()).scroll(new TimeValue(60000)),
						RequestOptions.DEFAULT);

				this.cursor.setAfter(response.getScrollId());

			}
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
		List<Map<String, Object>> list = new ArrayList<>();
		response.getHits().forEach(x ->

		list.add(x.getSourceAsMap()));

		List<M> mdata = mapper.mapEntitiesToModels(list);
		BaseResponse<List<M>> responseJson = new BaseResponse<>();
		responseJson.setData(mdata);
		responseJson.setSuccess(true);
		responseJson.setStatus(200);
		if (mdata.isEmpty()) {
			this.cursor = null;
			responseJson.setCursor(null);
		} else {

			responseJson.setCursor(this.cursor);
		}

		responseJson.setMessage(Constants.SUCCESS);
		return responseJson;

	}

	public BaseResponse<M> getById(T id) {
		GetResponse response = null;
		try {
			response = ESConnection.getInstance().getClient().get(
					new GetRequest(Constants.SUBSCRIPTIONENGINE + mapper.getType().getSimpleName()).id(id.toString()),
					RequestOptions.DEFAULT);

		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

		Map<String, Object> dataMap = response.getSourceAsMap();
		dataMap.put("id", response.getId());
		M data = mapper.mapEntityToModel(dataMap);
		BaseResponse<M> baseResponse = new BaseResponse<>();
		baseResponse.setData(data);
		baseResponse.setMessage(Constants.SUCCESS);
		baseResponse.setStatus(200);
		return baseResponse;
	}

	public BaseResponse<List<M>> getByParams(Map<?, ?> filters) {

		List<Map<String, Object>> list = new ArrayList<>();
		BoolQueryBuilder query = new BoolQueryBuilder();
		filters.forEach((key, value) -> query.filter(QueryBuilders.termsQuery((String) key, ((String[]) value)[0])));

		SearchRequest searchRequest = new SearchRequest(
				Constants.SUBSCRIPTIONENGINE + mapper.getType().getSimpleName());
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(query);
		searchRequest.source(searchSourceBuilder);

		SearchResponse response = null;

		try {
			response = ESConnection.getInstance().getClient().search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

		response.getHits().forEach(x ->

		list.add(x.getSourceAsMap()));
		List<M> mdata = mapper.mapEntitiesToModels(list);
		BaseResponse<List<M>> responseJson = new BaseResponse<>();
		responseJson.setData(mdata);
		responseJson.setSuccess(true);
		responseJson.setMessage(Constants.SUCCESS);
		responseJson.setStatus(200);
		return responseJson;
	}

	public BaseResponse<M> update(T id, M data) {
		ObjectMapper objectMapper = new ObjectMapper();
		UpdateRequest updateRequest = new UpdateRequest();
		updateRequest.index(Constants.SUBSCRIPTIONENGINE + this.mapper.getType().getSimpleName());
		updateRequest.id(id.toString());

		try {
			updateRequest.doc(objectMapper.writeValueAsBytes(data));
		} catch (JsonProcessingException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
		try {
			UpdateResponse response = ESConnection.getInstance().getClient().update(updateRequest,
					RequestOptions.DEFAULT);
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + response
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		BaseResponse<M> responseJson = new BaseResponse<>();
		responseJson.setSuccess(true);
		responseJson.setMessage(Constants.SUCCESS);
		responseJson.setStatus(200);
		return responseJson;
	}

	public BaseResponse<M> delete(T id) {

		DeleteRequest deleteRequest = new DeleteRequest("subscriptionengine_User", id.toString());

		try {
			DeleteResponse response = ESConnection.getInstance().getClient().delete(deleteRequest,
					RequestOptions.DEFAULT);
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + response
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

		BaseResponse<M> responseJson = new BaseResponse<>();
		responseJson.setSuccess(true);
		responseJson.setMessage(Constants.SUCCESS);
		responseJson.setStatus(200);
		return responseJson;
	}

	public BaseResponse<List<M>> getByCursor(Cursor cursor) throws BaseException {

		return null;
	}

}
