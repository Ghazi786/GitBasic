package com.jio.crm.dms.utils;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class ObjectMapperHelper {

	private static ObjectMapperHelper mapper = new ObjectMapperHelper();

	private final ObjectMapper entityObjectMapper;

	private final Gson gson;

	private ObjectMapperHelper() {
		entityObjectMapper = new ObjectMapper();

		gson = new GsonBuilder().registerTypeAdapter(Date.class, new TypeAdapter<Date>() {

			@Override
			public void write(JsonWriter out, Date value) throws IOException {
				if (value != null)
					out.value(value.getTime());
				else
					out.nullValue();
			}

			@Override
			public Date read(JsonReader in) throws IOException {
				return new Date(in.nextLong());
			}
		}).create();
	}

	public static ObjectMapperHelper getInstance() {
		return mapper;
	}

	public ObjectMapper getEntityObjectMapper() {
		return entityObjectMapper;
	}

	public Gson gson() {
		return gson;
	}

	public <T> T getBeanFromString(final String json, final Class<T> clazz) throws IOException {

		return entityObjectMapper.readValue(json, clazz);
	}

	public <T> T getBeanFromObject(final Object json, final Class<T> clazz) {

		return entityObjectMapper.convertValue(json, clazz);
	}

}