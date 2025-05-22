/**
 * 
 */
package com.jio.crm.dms.modules.upload.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.eclipse.jetty.http.HttpStatus;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class FileToPdfConverter {
	private static FileToPdfConverter fileToPdfConverter = null;

	private FileToPdfConverter() {

	}

	public static FileToPdfConverter getIndtance() {
		if (fileToPdfConverter == null) {
			fileToPdfConverter = new FileToPdfConverter();
		}
		return fileToPdfConverter;
	}

	public boolean convertImageToPdf(byte[] byteArray, String destPath, FileSystem fs)
			throws BaseException, IOException {

		FSDataOutputStream out = null;
		try {

			Path path = new Path(destPath);

			out = fs.create(path);
			
			Image image = Image.getInstance(byteArray);
			

			float w = image.getScaledWidth();
			float h = image.getScaledHeight();
			Rectangle page = new Rectangle(w + 20, h + 20);
			Document document = new Document(page, 10, 10, 10, 10);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			
			writer.setFullCompression();

			document.open();
			document.add(image);
			document.close();
			writer.close();
			
		} catch (Exception e) {

			

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}

		}

		return true;
	}
	
	
	public byte[] convertImageToPdfForMinio(byte[] byteArray)
			throws BaseException, IOException, DocumentException {


		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		
		com.itextpdf.text.Document minioDocument = new com.itextpdf.text.Document();

		PdfWriter.getInstance(minioDocument, byteArrayOutputStream);
		minioDocument.open();

		Image image = Image.getInstance(byteArray);
		minioDocument.add(image);
		minioDocument.close();

	
		return byteArrayOutputStream.toByteArray();
			
		
	}

	public void compressPdfFile(byte[] byteArray, String destPath, FileSystem fs) throws BaseException, IOException {
		FSDataOutputStream out = null;
		try {
			Path path = new Path(destPath);

			out = fs.create(path);
			PdfReader reader = new PdfReader(byteArray);

			PdfStamper pdfStamper = new PdfStamper(reader, out);
			pdfStamper.setFullCompression();
			pdfStamper.close();
			reader.close();

			
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

			
			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());

		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}

		}
	}
}
