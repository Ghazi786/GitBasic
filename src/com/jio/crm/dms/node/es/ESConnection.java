package com.jio.crm.dms.node.es;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.client.transport.TransportClient.HostFailureListener;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.plugins.Plugin;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.alarmmanager.AlarmNameIntf;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;

/*
 * Class for Elastic Search DB
 * It includes creating connections creating index etc.
 */
public class ESConnection extends Plugin implements HostFailureListener {

	private RestHighLevelClient client;
	private static ESConnection esConnection;
	private BulkProcessor processor;
	private HAOperation haOperation = new HAOperationImpl();
	Collection<Class<? extends Plugin>> PLUGIN = Collections.unmodifiableList(Arrays.asList(ESPlugin.class));

	private ESConnection() {
	}

	public static synchronized ESConnection getInstance() {
		if (esConnection == null) {
			esConnection = new ESConnection();
		}
		return esConnection;
	}

	private boolean createConnectionWithCluster() {

		boolean isSuccess = false;
		try {

			DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder(
					"###################### Going to create connection with ES cluster ######################"
							+ Constants.CREATECONNECTIONWITHCLUSTER);

			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(ConfigParamsEnum.ES_XPACKUNAME.getStringValue(),
							ConfigParamsEnum.ES_XPACKPASSWORD.getStringValue()));
			Map<String, String> coordinatorAddress = this
					.nodeIpPorts(ConfigParamsEnum.ES_CORDINATORNODEIPANDPORT.getStringValue());

			HttpHost[] host = new HttpHost[coordinatorAddress.size()];
			int i = 0;
			for (Map.Entry<String, String> entry : coordinatorAddress.entrySet()) {
				host[i] = new HttpHost(entry.getKey(), Integer.parseInt(entry.getValue()));
				++i;
			}

			RestClientBuilder builder = RestClient.builder(host)
					.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
						@Override
						public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
							return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
						}
					});

			client = new RestHighLevelClient(builder);

			isSuccess = client.ping(RequestOptions.DEFAULT);

			if (isSuccess) {
				InterfaceStatus.status.setEsConnected(true);
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
						"###################### Connection has been created with ES cluster ######################",
						this.getClass().getName(), Constants.CREATECONNECTIONWITHCLUSTER).writeLog();

			} else {
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder("XXXXXXXXXXXXXXXXXXXXXX ES CONNECTION NOT CREATEDXXXXXXXXXXXXXXXXXXXXXX",
								this.getClass().getName(), Constants.CREATECONNECTIONWITHCLUSTER)
						.writeLog();

			}

		} catch (ElasticsearchSecurityException | IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeLog();

		}
		return isSuccess;
	}

	private Map<String, String> nodeIpPorts(String address) {

		Map<String, String> ipPorts = new HashMap<>();
		String[] addresses = address.split(",");

		for (String ipPort : addresses) {
			ipPorts.putAll(this.splitIpPort(ipPort));
		}

		return ipPorts;
	}

	private Map<String, String> splitIpPort(String ip6) {

		Map<String, String> ipPortMap = new HashMap<>();
		String port = ip6.substring(ip6.lastIndexOf(':') + 1);
		String ipAddress = ip6.substring(0, ip6.lastIndexOf(':'));

		ipPortMap.put(ipAddress, port);

		return ipPortMap;
	}

	@Override
	public void onNodeDisconnected(DiscoveryNode node, Exception ex) {

		DappLoggerService.GENERAL_INFO_LOG
				.getInfoLogBuilder("Disconnected node :" + node.getHostAddress() + "onNodeDisconnected");

	}

	private boolean isIndexExist(String indexName) {

		boolean mappingFlag = false;
		try {

			GetIndexRequest request = new GetIndexRequest(indexName);
			mappingFlag = client.indices().exists(request, RequestOptions.DEFAULT);

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage() + "index exception");
		}
		return mappingFlag;
	}

	public boolean createIndex(String indexName) {

		boolean flag = false;
		try {
			boolean indexingFlag = this.isIndexExist(indexName);

			if (!indexingFlag) {

				CreateIndexRequest request = new CreateIndexRequest(indexName);
				request.settings(
						Settings.builder().put(ESConstants.INDEX_DOC_LIMIT, ESConstants.INDEX_DOC_LIMIT_VALUE));
				CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
				flag = createIndexResponse.isAcknowledged();

				DappLoggerService.GENERAL_INFO_LOG
						.getInfoLogBuilder("Index Created Successfully : " + indexName + "createIndex");

			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage() + "index exception");
			String profile = System.getProperty("profile");
			if (profile == null || !profile.equals("dev")) {
				OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(
						AlarmNameIntf.DMS_ES_EVENT_INDEX_CREATION_FAILED,
						AlarmNameIntf.DMS_ES_EVENT_INDEX_CREATION_FAILED);
			}
		}
		return flag;
	}

	private boolean isMappingExist(String indexName) {
		boolean isMappingExist = false;
		try {

			GetMappingsRequest request = new GetMappingsRequest();
			request.indices(indexName.toLowerCase());

			GetMappingsResponse getMappingResponse = client.indices().getMapping(request, RequestOptions.DEFAULT);

			Map<String, MappingMetadata> allMappings = getMappingResponse.mappings();
			MappingMetadata mappingMetaData = allMappings.get(indexName);

			if (mappingMetaData != null) {
				isMappingExist = true;

			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

		}
		return isMappingExist;
	}

	public RestHighLevelClient getClient() {
		return client;
	}

	public void setClient(RestHighLevelClient client) {
		this.client = client;
	}

	private void createMappingEvent(String indexName, String typeName) {

		boolean mappingFlag = this.isMappingExist(indexName);
		if (!mappingFlag) {

			XContentBuilder ruleBuilder;
			try {
				ruleBuilder = jsonBuilder().startObject().field(ESConstants.DYNAMIC, false)
						.field(ESConstants.PROPERTIES).startObject();

				ruleBuilder.field(ESConstants.RANK).startObject().field(ESConstants.TYPE, ESConstants.LONG).endObject();

				ruleBuilder.field(ESConstants.NAME).startObject().field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE)
						.endObject();
				ruleBuilder.field(ESConstants.PRM_ID).startObject().field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE)
						.endObject();
				ruleBuilder.field(ESConstants.PARENT_NAME).startObject()
						.field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE).endObject();
				ruleBuilder.field(ESConstants.PARENT_PRM_ID).startObject()
						.field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE).endObject();
				ruleBuilder.field(ESConstants.CIRCLE).startObject().field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE)
						.endObject();

				ruleBuilder.field(ESConstants.CITY).startObject().field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE)
						.endObject();

				ruleBuilder.field(ESConstants.JIO_POINT).startObject().field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE)
						.endObject();

				ruleBuilder.field(ESConstants.JIOCENTRE).startObject().field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE)
						.endObject();

				ruleBuilder.field(ESConstants.PRODUCTTYPE).startObject()
						.field(ESConstants.TYPE, ESConstants.KEYWORD_TYPE).endObject();

				ruleBuilder.field(ESConstants.NO_OF_SALE).startObject().field(ESConstants.TYPE, ESConstants.LONG)
						.endObject();

				ruleBuilder.field(ESConstants.TOLTAL_AMOUNT).startObject().field(ESConstants.TYPE, ESConstants.LONG)
						.endObject();

				ruleBuilder.field(ESConstants.DATE).startObject().field(ESConstants.TYPE, ESConstants.DATE).endObject();

				ruleBuilder.field(ESConstants.TOKENASSIGNED).startObject().field(ESConstants.TYPE, ESConstants.BOOLEAN)
						.endObject();

				ruleBuilder.endObject().endObject();

				PutMappingRequest putRequest = new PutMappingRequest(indexName + "_" + typeName);
				putRequest.source(ruleBuilder);

				AcknowledgedResponse putMappingResponse = client.indices().putMapping(putRequest,
						RequestOptions.DEFAULT);

				putMappingResponse.isAcknowledged();

			} catch (IOException e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
						e.getMessage() + "createMappingEvent");
				OamClientManager.getInstance();
				OamClientManager.getOamClientForAlarm()
						.raiseAlarmToOamServer(AlarmNameIntf.DMS_EVENT_MAPPING_CREATION_FAIL);
			}
		}
	}

	public void initializeES() {

		try {

			boolean esConnected = this.createConnectionWithCluster();
			System.out.println("esconnected " + esConnected);
			if (esConnected) {
				this.createIndex(ESConstants.RECORD_INDEX_NAME);
				this.createMappingEvent(ESConstants.RECORD_INDEX_NAME, ESConstants.RECORD_INDEX_TYPE);

				this.startLoad();
			} else {

				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getInfoLogBuilder("Es Connection Failed initializeES");
			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage() + "initializeES");
		}
	}

	public void startLoad() {

		try {

			BulkProcessor.Listener listener = new BulkProcessor.Listener() {
				@Override
				public void beforeBulk(long executionId, BulkRequest request) {
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"Executing [ " + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
					System.out.println("completed one batch " + response.hasFailures());
					if (response.hasFailures()) {
						System.out.println("executionId : " + executionId + ", BulkResponse Fail Message : "
								+ response.buildFailureMessage());
					}
				}

				@Override
				public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"Executing [ " + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				}
			};

			processor = BulkProcessor
					.builder((request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
							listener)
					.setBulkActions(ConfigParamsEnum.ES_BULKACTION.getIntValue())
					.setFlushInterval(TimeValue.timeValueSeconds(ConfigParamsEnum.ES_FLUSHINTERVAL.getIntValue()))
					.setConcurrentRequests(2)
					.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3)).build();

		} catch (Exception e) {
			try {
				client.close();
			} catch (IOException e1) {

				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();

			}
		}

	}

	/**
	 * @return the haOperation
	 */
	public HAOperation getHaOperation() {

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("HA Operation", this.getClass().getName(), "getHaOperation")
				.writeLog();

		return haOperation;
	}

	public BulkProcessor getProcessor() {
		return processor;
	}
}
