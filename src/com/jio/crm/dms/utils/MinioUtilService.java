
package com.jio.crm.dms.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;

import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetBucketReplicationArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.SetBucketReplicationArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import io.minio.messages.AndOperator;
import io.minio.messages.DeleteMarkerReplication;
import io.minio.messages.Item;
import io.minio.messages.ReplicationConfiguration;
import io.minio.messages.ReplicationDestination;
import io.minio.messages.ReplicationRule;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;

/**
 * @author Ghajnafar.Shahid
 *
 */
@SuppressWarnings({ "squid:S2095" })
public class MinioUtilService {

	private static MinioUtilService minioUtilService = null;
	private static MinioClient minioClient = null;
	private static final String CONTENT_DISPOSIOTION = "Content-Disposition";
	private static final String CONTENT_ATTACHMENT = "inline; filename=\"";

	/**
	 * 
	 */
	public MinioUtilService() {
	}

	public static MinioUtilService getInstance() {
		getMinioClient();
		synchronized (MinioUtilService.class) {
			if (minioUtilService == null) {
				minioUtilService = new MinioUtilService();
			}
			return minioUtilService;
		}

	}

	public static MinioClient getMinioClient() {

		synchronized (MinioClient.class) {
			if (minioClient == null) {
				try {
					minioClient = MinioClient.builder().endpoint(ConfigParamsEnum.MINIO_URL.getStringValue())
							.credentials(ConfigParamsEnum.MINIO_USER.getStringValue(),
									ConfigParamsEnum.MINIO_PASSWORD.getStringValue())
							.build();
				
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return minioClient;
		}
	}

	public void uploadFile(String filepath, byte[] byteArray, String fileName, String contentType, long sizeInBytes)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException, BaseException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		boolean found = false;
		try {
			// Create a minioClient with the MinIO server playground, its access key and
			// secret key

			// Make bucket if not exist
			try {
				found = minioClient.bucketExists(
						BucketExistsArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).build());

			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
			}
			if (!found) {
				// Make a new bucket called 'testingload'
				minioClient.makeBucket(
						MakeBucketArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).build());

			} else {
				System.out.println("Bucket" + " " + ConfigParamsEnum.BUCKETNAME.getStringValue() + " already exists.");
			}

			minioClient.putObject(PutObjectArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue())
					.object(filepath).stream(new ByteArrayInputStream(byteArray), byteArray.length, -1)
					.contentType(contentType).build());

			CounterNameEnum.CNTR_UPLOAD_MINIO_FILE_SUCCESS.increment();
			System.out.println("Count:" + CounterNameEnum.CNTR_UPLOAD_MINIO_FILE_SUCCESS.getValue());
		} catch (MinioException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			CounterNameEnum.CNTR_UPLOAD_MINIO_FILE_FAILURE.increment();
			System.out.println("Error occurred: " + e);
			System.out.println("HTTP trace: " + e.httpTrace());
			throw new BaseException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}

	}

	public void uploadFiles(String fileName, String contentType) throws InvalidKeyException, ErrorResponseException,
			InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
			ServerException, XmlParserException, IllegalArgumentException, IOException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		// Make 'asiatrip' bucket if not exist
		boolean found = minioClient
				.bucketExists(BucketExistsArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).build());
		if (!found) {
			// Make a new bucket called 'asiatrip'
			minioClient
					.makeBucket(MakeBucketArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).build());
		} else {
			System.out.println("Bucket 'bucketreplication' already exists.");
		}
		for (int i = 0; i < 5000000; i++) {

			try {
				InputStream inputStream = null;

				String profile = System.getProperty("profile");
				if (profile != null && profile.equals("dev")) {
					inputStream = new FileInputStream(new File("./configuration/Jio Blockchain.pdf"));
				} else {
					inputStream = new FileInputStream(new File("../configuration/Jio Blockchain.pdf"));
				}
				/**
				 * byte[] bytes = IOUtils.toByteArray(inputStream); int
				 * sizeInBytes=bytes.length;
				 */
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("X-EventName", "UPLOAD");

				headers.put("X-Amz-R", "DOWNLOAD");
				Map<String, String> userMetadata = new HashMap<>();
				userMetadata.put("X-Name", "Raju");
				userMetadata.put("X-Gender", "Salma");
				userMetadata.put("X-TOWNr", "Patna");
				userMetadata.put("X-Country", "India");
				userMetadata.put("Air", "IndiaLove");
				// get file extensions
				String extension = FilenameUtils.getExtension(fileName);

				// get base file Name
				String fileNameTemp = FilenameUtils.getBaseName(fileName);

				String tempfileName = fileNameTemp + "_" + (i + 1) + "." + extension;
				minioClient.putObject(PutObjectArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue())
						.object(tempfileName).stream(inputStream, -1, 10485760).headers(headers)
						.contentType(contentType).build());
				
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
				
			}
		}

	}

	public List<String> getAllFilePath(String path) {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		Iterable<Result<Item>> results = MinioUtilService.getInstance().getMinioClient().listObjects(ListObjectsArgs
				.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).prefix(path).recursive(true).build());
		List<String> fileList = new ArrayList<>();
		for (Result<Item> result : results) {
			try {
				String fileName = result.get().objectName().toString();
				fileList.add(fileName.substring(fileName.lastIndexOf('/') + 1));
				
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
					| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
					| XmlParserException | IOException e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();

			}
		}
		return fileList;
	}

	public void createBucketReplication(String bucketName)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException, BaseException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		try {

			ReplicationConfiguration config = minioClient
					.getBucketReplication(GetBucketReplicationArgs.builder().bucket("testingreplication").build());

			Map<String, String> tags = new HashMap<>();
			tags.put("key1", "value1");
			tags.put("key2", "value2");

			ReplicationRule rule = new ReplicationRule(new DeleteMarkerReplication(Status.DISABLED),
					new ReplicationDestination(null, null, "arn:aws:s3:::*", null, null, null, null), null,
					new RuleFilter(new AndOperator("TaxDocs", tags)), "rule1", null, 1, null, Status.ENABLED);

			List<ReplicationRule> rules = new LinkedList<>();
			rules.add(rule);

			ReplicationConfiguration config1 = new ReplicationConfiguration("arn:aws:s3:::*", rules);

			minioClient.setBucketReplication(
					SetBucketReplicationArgs.builder().bucket(bucketName).config(config1).build());
		} catch (MinioException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			System.out.println("Error occurred: " + e);
			System.out.println("HTTP trace: " + e.httpTrace());
			throw new BaseException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}

	}

	public void getFileByUrl() throws BaseException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		// get object given the bucket and object name
		Map<String, String> reqParams = new HashMap<String, String>();
		reqParams.put("response-content-type", "application/json");

		try {

			String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.HEAD)
					.bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).object("NO1234567/" + "interview.txt")
					.expiry(2, TimeUnit.HOURS).extraQueryParams(reqParams).build());

			
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException
				| IllegalArgumentException | IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
	}

	public byte[] readFileFromMinio(String targetPath) throws BaseException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		byte bytes[] = null;
		try (InputStream in = MinioUtilService.getMinioClient().getObject(GetObjectArgs.builder()
				.bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).object(targetPath).build())) {
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
				throw new BaseException(HttpStatus.SC_BAD_REQUEST, "Bytes are zero");
			}

		}

		catch (IOException | InvalidKeyException | ErrorResponseException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
				| XmlParserException | IllegalArgumentException e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			throw new BaseException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		return bytes;

	}

	public InputStream getFileStream(String targetPath) throws BaseException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		InputStream in = null;
		try {
			in = MinioUtilService.getMinioClient().getObject(GetObjectArgs.builder()
					.bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).object(targetPath).build());

		}

		catch (IOException | InvalidKeyException | ErrorResponseException | InsufficientDataException
				| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
				| XmlParserException | IllegalArgumentException e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			throw new BaseException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		return in;

	}

	public boolean isObjectExist(String fileName) {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		try {
			minioUtilService.getMinioClient().statObject(StatObjectArgs.builder()
					.bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).object(fileName).build());
			return true;
		} catch (ErrorResponseException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return false;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			return false;
		}
	}

	public boolean copyObject(String srcFile, String destFile) throws IOException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		boolean copy = true;
		try {
			minioUtilService.getMinioClient()
					.copyObject(CopyObjectArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue())
							.object(destFile).source(CopySource.builder()
									.bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).object(srcFile).build())
							.build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException e) {
			copy = false;
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		return copy;
	}

	public boolean moveObject(String srcFile) throws IOException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		boolean move = true;
		try {
			minioUtilService.getMinioClient().removeObject(RemoveObjectArgs.builder()
					.bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).object(srcFile).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException e) {
			move = false;
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		return move;

	}

	public void getFile(HttpServletResponse response, String fileName, String targetPath, String fileDownloadName)
			throws IOException, InvalidKeyException, ErrorResponseException, InsufficientDataException,
			InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
			IllegalArgumentException, BaseException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		String extension = FilenameUtils.getExtension(fileName);
		String contentType = MimeTypes.getMimeType(extension);
		response.setContentType(contentType);
		if (fileDownloadName != null) {
			fileName = fileDownloadName + "." + extension;
		}
		response.setHeader(CONTENT_DISPOSIOTION, CONTENT_ATTACHMENT + fileName + "\"");

		try (InputStream stream = MinioUtilService.getMinioClient().getObject(GetObjectArgs.builder()
				.bucket(ConfigParamsEnum.BUCKETNAME.getStringValue()).object(targetPath).build())) {

			OutputStream os = response.getOutputStream();

			byte[] buffer = new byte[1024];
			// byte[] bytes = IOUtils.toByteArray(stream)
			// response.setContentLength(bytes.length)
			int numBytes = 0;
			try {

				while ((numBytes = stream.read(buffer)) > 0) {
					os.write(buffer, 0, numBytes);

				}
			} finally {
				os.flush();
				os.close();
				stream.close();
			}

		} catch (IOException e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			throw new BaseException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	public void deleteFile(String filepath) {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		// Remove object.
		try {
			minioClient.removeObject(RemoveObjectArgs.builder().bucket(ConfigParamsEnum.BUCKETNAME.getStringValue())
					.object(filepath).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

	}

	public void deleteBucket(String bucketName) {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		// Remove object.
		try {
			minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		System.out.println("Bucket delete Sucessfully");

	}
}
