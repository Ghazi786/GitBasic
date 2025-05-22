package com.jio.crm.dms.kafka.producer;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.elastic.search.service.impl.ElasticUUIDGenerator;
import com.jio.crm.dms.kafka.KafkaConfigEnum;
import com.jio.crm.dms.kafka.KafkaProducerUtil;
import com.jio.crm.dms.logger.DappLoggerService;

public class UOLProducerTask implements Runnable {

	private static Producer<String, Object> producer = KafkaProducerUtil.createProducer();

	private List<Map<String, Object>> uolDetails;

	private String referenceNumber;

	public List<Map<String, Object>> getUolDetails() {
		return uolDetails;
	}

	public void setUolDetails(List<Map<String, Object>> uolDetails) {
		this.uolDetails = uolDetails;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	@Override
	public void run() {

		try {

			for (Map<String, Object> uol : uolDetails) {
				ProducerRecord<String, Object> record = new ProducerRecord<>(
						KafkaConfigEnum.UOL_PUSH_TOPIC_NAME.getStringValue(),
						ElasticUUIDGenerator.getInstance().getUniqueUUID(), uol);
				producer.send(record);
			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}

	}

}
