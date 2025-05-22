package com.jio.crm.dms.service;

import java.util.List;
import java.util.Map;

import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.Cursor;
import com.jio.crm.dms.exceptions.BaseException;

/**
 * Interface with basic CRUD operations for Jio Devops  
 * @author Barun.rai
 * 
 */
public interface BaseService<E, M, T> {
	 BaseResponse< M> save(M data);
	 BaseResponse< List<M>> saveAll(List<M> data);
	 BaseResponse< List<M>> get();
	 BaseResponse< M> getById(T id) throws BaseException;
	 BaseResponse< List<M>> getByParams(Map<?, ?> filters)throws BaseException;
	 BaseResponse< M> update(T id, M data);
	 BaseResponse< M> delete(T id);
	 BaseResponse< List<M>> getByCursor(Map<?, ?> filters, Cursor cursor)throws BaseException;
}
