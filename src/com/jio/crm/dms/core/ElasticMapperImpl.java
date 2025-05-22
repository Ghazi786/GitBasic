package com.jio.crm.dms.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ElasticMapperImpl<M> implements Mapper<Map<String, Object>, M> {

	private ObjectMapper mapper = new ObjectMapper();
	private Class<M> type;

	public ElasticMapperImpl(Class<M> type) {
		this.type = type;
	}

	public Class<M> getType() {
		return type;
	}

	public Map<String, Object> mapModelToEntity(M model) {

		return null;
	}

	public M mapEntityToModel(Map<String, Object> entity) {
		M model = null;
		model =  mapper.convertValue(entity, this.type);

		return model;
	}

	public List<Map<String, Object>> mapModelsToEntities() {

		return new ArrayList<Map<String, Object>>();
	}

	@Override
	public List<M> mapEntitiesToModels(List<Map<String, Object>> entities) {
		List<M> models = new ArrayList<>();
		for (Map<String, Object> entity : entities) {
			models.add(mapEntityToModel(entity));
		}
		return models;
	}

}
