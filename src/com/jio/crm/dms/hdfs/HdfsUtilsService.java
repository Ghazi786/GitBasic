/**
 * 
 */
package com.jio.crm.dms.hdfs;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.eclipse.jetty.http.HttpStatus;

import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class HdfsUtilsService {

	private static HdfsUtilsService instance;

	public static HdfsUtilsService getInstance() {
		if (instance == null) {
			instance = new HdfsUtilsService();
		}
		return instance;
	}

	public byte[] readFile(String filePath) throws IOException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		Configuration conf = HdfsConfigurationManager.getConfiguration();

		FileSystem fileSystem = FileSystem.get(conf);
		byte[] bytes = null;
		Path path = new Path(filePath);
		if (!fileSystem.exists(path)) {
			throw new FileNotFoundException();
		}
		try (FSDataInputStream in = fileSystem.open(path)) {
			bytes = new byte[in.available()];
			int bytesRead = in.read(bytes);
			if (bytesRead >= 0) {
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			} else {
				throw new BaseException(HttpStatus.BAD_REQUEST_400, "Bytes are zero");
			}

		} catch (Exception e) {
			throw new IOException();
		} finally {
			fileSystem.close();
		}
		return bytes;
	}

	public void writeByte(byte[] bytes, String destPath, String fileName) throws BaseException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		try {
			Configuration conf = HdfsConfigurationManager.getConfiguration();

			FileSystem fs = FileSystem.get(conf);
			Path path = new Path(destPath + Constants.DELIMITER + fileName);
			try (FSDataOutputStream out = fs.create(path)) {
				out.write(bytes);

			} catch (Exception e) {
				throw new BaseException(HttpStatus.BAD_REQUEST_400, "File Writting Exception");
			}

		} catch (Exception e) {
			throw new BaseException(HttpStatus.BAD_REQUEST_400, "File Writting Exception");
		}

	}

	public void deleteFile(String file) throws IOException, BaseException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		Configuration conf = HdfsConfigurationManager.getConfiguration();

		FileSystem fileSystem = FileSystem.get(conf);

		Path path = new Path(file);
		if (!fileSystem.exists(path)) {
			throw new BaseException(HttpStatus.BAD_REQUEST_400, "File Not Found Exception");
		}

		fileSystem.delete(path, true);

		fileSystem.close();
	}

	void deleteFileOrFolder(String targetPath) {

		Configuration conf = HdfsConfigurationManager.getConfiguration();

		try {

			FileSystem fileSystem = FileSystem.get(conf);

			Path path = new Path(targetPath);
			if (!fileSystem.exists(path)) {
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}

			fileSystem.delete(path, true);

			fileSystem.close();

		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

	}

	public boolean renamePath(String src, String destPath, String fileName) throws IOException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		Configuration conf = HdfsConfigurationManager.getConfiguration();
		FileSystem fileSystem = FileSystem.get(conf);
		boolean rename = false;
		Path srcpath = new Path(src);
		Path destPathRoot = new Path(destPath);
		if (!fileSystem.exists(srcpath)) {
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Executing [ not found" + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			throw new FileNotFoundException();
		}
		if (!fileSystem.exists(destPathRoot)) {
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Executing [creating directory " + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			fileSystem.mkdirs(destPathRoot);
		}
		Path destpath = new Path(destPath + Constants.DELIMITER + fileName);
		rename = fileSystem.rename(srcpath, destpath);
		return rename;
	}

	public boolean copyFilefromOnePathToAnother(String src, String dest) throws BaseException {
		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			Configuration conf = HdfsConfigurationManager.getConfiguration();
			FileSystem fileSystem = FileSystem.get(conf);

			Path path = new Path(src);
			Path destpath = new Path(dest);
			if (!fileSystem.exists(path)) {
				throw new BaseException(HttpStatus.BAD_REQUEST_400, "File Not Found Exception");
			}
			try (FSDataInputStream in = fileSystem.open(path)) {
				BufferedInputStream bis = null;
				bis = new BufferedInputStream(in, 1024);

				FSDataOutputStream out = fileSystem.create(destpath);
				try {
					byte[] data = new byte[1024];
					int count;
					while ((count = bis.read(data, 0, 1024)) != -1) {
						out.write(data, 0, count);
					}

				} finally {
					out.close();

					fileSystem.close();
				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
				throw new BaseException(HttpStatus.BAD_REQUEST_400,
						"File Writting Exception::copyFilefromOnePathToAnother");
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			throw new BaseException(HttpStatus.BAD_REQUEST_400, "HDFS Connection issue::copyFilefromOnePathToAnother");
		}
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [File Copied Successfully to ::: " + dest + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		return true;
	}

}
