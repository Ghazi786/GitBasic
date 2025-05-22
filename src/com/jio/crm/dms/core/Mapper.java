package com.jio.crm.dms.core;

import java.util.List;

public interface Mapper<E, M> {
	Class<M> getType();
	E mapModelToEntity(M model); 
	M mapEntityToModel(E entity);
	List<E> mapModelsToEntities(List<M> models);
	List<M> mapEntitiesToModels(List<E> entities);
}
