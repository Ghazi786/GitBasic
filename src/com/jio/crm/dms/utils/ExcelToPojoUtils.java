package com.jio.crm.dms.utils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelToPojoUtils {
	public static final String BOOLEAN_TRUE = "1";
	public static final String LIST_SEPARATOR = ",";
	private static final Logger LOGGER = Logger.getLogger(ExcelToPojoUtils.class.getName());

	private static String strToFieldName(String str) {
		str = str.replaceAll("[^a-zA-Z0-9]", "");
		return str.length() > 0 ? str.substring(0, 1).toLowerCase() + str.substring(1) : null;
	}

	public static <T> List<T> topojo(Class<T> type, InputStream inputStream)
			throws  Exception {
		List<T> results = new ArrayList<>();
		Workbook workbook = null;

		try {
			workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(0);

			// header column names
			List<String> colNames = new ArrayList<>();
			Row headerRow = sheet.getRow(0);
			for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
				org.apache.poi.ss.usermodel.Cell headerCell = headerRow.getCell(i,
						org.apache.poi.ss.usermodel.Row.RETURN_BLANK_AS_NULL);
				colNames.add(headerCell != null
						? strToFieldName(( headerCell).getStringCellValue())
						: null);
			}

			for (int j = 1; j < sheet.getPhysicalNumberOfRows(); j++) {
				Row row = sheet.getRow(j);

				T result = type.getDeclaredConstructor().newInstance();
				for (int k = 0; k < row.getPhysicalNumberOfCells(); k++) {
					if (colNames.get(k) != null) {
						org.apache.poi.ss.usermodel.Cell cell = row.getCell(k, Row.RETURN_BLANK_AS_NULL);
						if (cell != null) {
							DataFormatter formatter = new DataFormatter();
							String strValue = formatter.formatCellValue( cell);
							Field field = type.getDeclaredField(colNames.get(k));
							field.setAccessible(true);
							if (field != null) {
								Object value = null;
								if (field.getType().equals(Long.class)) {
									value = Long.valueOf(strValue);
								} else if (field.getType().equals(String.class)) {

									value = ( cell).getStringCellValue();
									if (value.equals("")) {
										value = null;
									}
								} else if (field.getType().equals(Integer.class)) {
									if (strValue.isEmpty()) {
										value = null;
									} else {
										value = Integer.valueOf(strValue);
									}
								} else if (field.getType().equals(LocalDate.class)) {
									value = LocalDate.parse(strValue);
								} else if (field.getType().equals(LocalDateTime.class)) {
									value = LocalDateTime.parse(strValue);
								} else if (field.getType().equals(boolean.class)) {
									value = BOOLEAN_TRUE.equals(strValue);
								} else if (field.getType().equals(BigDecimal.class)) {
									value = new BigDecimal(strValue);
								} else if (field.getType().equals(Object.class)) {
									value = strValue;
								} else if (field.getType().equals(float.class)) {
									value = Float.valueOf(strValue);
								} else if (field.getType().equals(List.class)) {
									value = Arrays.asList(strValue.trim().split("\\s*" + LIST_SEPARATOR + "\\s*"));
								}
								field.set(result, value);
							}
						}
					}
				}
				results.add(result);
			}
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}

		finally {
			if (workbook != null) {
				workbook.close();
			}
		}

		return results;
	}

	private ExcelToPojoUtils() {
		super();
	}

}
