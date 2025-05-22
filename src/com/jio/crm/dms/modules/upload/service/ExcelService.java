/**
 * 
 */
package com.jio.crm.dms.modules.upload.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.jio.crm.dms.bean.FileDocument;
import com.jio.crm.dms.logger.DappLoggerService;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class ExcelService {

	/**
	 * 
	 */
	public ExcelService() {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

	public HashMap<String, List<FileDocument>> readSearchParameter(XSSFSheet sheet, XSSFWorkbook workbook) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		HashMap<String, List<FileDocument>> searchParameters = new HashMap<>();
		try {

			List<FileDocument> searchParam1 = new ArrayList<>();
			FileDocument data = null;
			Row row;

			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				row = sheet.getRow(i);
				if (i == 0) {
					int lastColumn = row.getPhysicalNumberOfCells();
					Cell cellTemp = row.createCell(lastColumn);
					XSSFCellStyle style = workbook.createCellStyle();
					style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
					style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellTemp.setCellValue("Remarks");
					cellTemp.setCellStyle(style);
					continue;
				}
				data = new FileDocument();

				if (row.getCell(0) != null) {
					row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
					data.setMobileNumber(row.getCell(0).getStringCellValue().trim());
				}
				if (row.getCell(1) != null) {
					row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
					data.setOrderId(row.getCell(1).getStringCellValue().trim());
				}
				if (row.getCell(2) != null) {
					row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
					data.setCustomerId(row.getCell(2).getStringCellValue().trim());
				}
				if (row.getCell(3) != null) {
					row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
					data.setDocumentCategory(row.getCell(3).getStringCellValue().trim());
				}
				if (row.getCell(4) != null) {
					row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
					data.setDocumentNumber(row.getCell(4).getStringCellValue().trim());
				}

				searchParam1.add(data);
			}

			searchParameters.put("s1", searchParam1);

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

		return searchParameters;

	}
}
