package com.jio.crm.dms.utils;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jio.crm.dms.logger.DappLoggerService;

public class MapperUtil<E, M> {

	private static final MapperUtil mapper = new MapperUtil();

	private MapperUtil() {
	}

	public static final MapperUtil getInstance() {
		return mapper;
	}

	public List<M> mapObjects(final List<E> entities, final Class<M> clazz)
			throws InstantiationException, IllegalAccessException {

		M model = null;

		List<M> po = new ArrayList<>();

		for (final E entity : entities) {

			final List<String> planFields = Arrays.asList(clazz.getDeclaredFields()).stream().map(Field::getName)
					.collect(Collectors.toList());

			model = clazz.newInstance();
			for (final Field f : model.getClass().getDeclaredFields()) {
				if (planFields.contains(f.getName())) {
					try {
						new PropertyDescriptor(f.getName(), clazz).getWriteMethod().invoke(model,
								new PropertyDescriptor(f.getName(), clazz).getReadMethod().invoke(entity));
					} catch (Exception e) {
						DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
								.writeExceptionLog();

					}
				}
			}

			po.add(model);

		}
		return po;

	}

	public List<M> excelToPojo(final Class<M> clazz, final String fileName) {

		InputStream is = null;

		FileInputStream fileInputStream2 = null;

		String path = Constants.CONFIGURATION;

		List<M> entites = null;

		try {

			String profile = System.getProperty("profile");
			if (profile != null && profile.equals("dev")) {
				fileInputStream2 = new FileInputStream(new File("." + path + fileName + ".xlsx"));

			} else {
				fileInputStream2 = new FileInputStream(new File(".." + path + fileName + ".xlsx"));

			}

			is = fileInputStream2;

		} catch (FileNotFoundException e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
		try {
			entites = ExcelToPojoUtils.topojo(clazz, is);

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

		return entites;

	}
}
