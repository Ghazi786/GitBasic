package com.jio.crm.dms.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerUtil {

	public static Producer<String, Object> createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfigEnum.KAFKA_BROKERS.getStringValue());
		props.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaConfigEnum.CLIENT_ID.getStringValue());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ObjectSerialize.class.getName());

		// Use Snappy compression for batch compression.
		props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");

		// Linger up to 100 ms before sending batch if size not met
		props.put(ProducerConfig.LINGER_MS_CONFIG, 100);

		// Batch up to 64K buffer sizes.

		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 100);

		props.put(ProducerConfig.ACKS_CONFIG, "all");

		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);

		return new KafkaProducer<>(props);

	}

	private KafkaProducerUtil() {
		super();
	}

}
