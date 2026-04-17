package com.example.ecommerce_inventory_service.config;

import com.example.events.OrderAvroCreatedEvent;
import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
//import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${spring.kafka.schema-registry}")
    private String schemaRegistry;


    // ✅ TEMPLATE PARA JSON (STRING)
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    // ✅ TEMPLATE PARA AVRO
    @Bean
    public ProducerFactory<String, OrderAvroCreatedEvent> avroProducerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        //config.put("schema.registry.url", schemaRegistry); // OK
        config.put("apicurio.registry.url", schemaRegistry);
        config.put("apicurio.registry.auto-register", true);

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroKafkaSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean(name = "avroKafkaTemplate")
    public KafkaTemplate<String, OrderAvroCreatedEvent> kafkaAvroTemplate() {
        return new KafkaTemplate<>(avroProducerFactory());
    }
}
