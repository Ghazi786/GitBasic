package com.jio.crm.dms.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jio.crm.dms.logger.DappLoggerService;

/**
 * @author Barun.Rai
 *
 * @param <E>
 * @param <M>
 */
public class Mapper<E, M> {

	/**
	 * Method which maps a list of entities to respective models
	 * 
	 * @param entities
	 * @param clazz
	 * @param entityClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public List<M> mapObjects(final List<E> entities, final Class<M> clazz, final Class<E> entityClass)
			throws InstantiationException, IllegalAccessException {
		final List<M> models = new ArrayList<>();
		final List<String> planFields = Arrays.asList(entityClass.getDeclaredFields()).stream().map(Field::getName)
				.collect(Collectors.toList());
		for (final E entity : entities) {
			if (entity != null) {
				M model = null;
				try {
					model = clazz.getDeclaredConstructor().newInstance();
					for (final Field f : model.getClass().getDeclaredFields()) {
						if (planFields.contains(f.getName())) {
							new PropertyDescriptor(f.getName(), clazz).getWriteMethod().invoke(model,
									new PropertyDescriptor(f.getName(), entityClass).getReadMethod().invoke(entity));
						}
					}
				} catch (final Exception e) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
							.writeExceptionLog();

				} finally {
					models.add(model);
				}

			}
		}
		return models;
	}
}
