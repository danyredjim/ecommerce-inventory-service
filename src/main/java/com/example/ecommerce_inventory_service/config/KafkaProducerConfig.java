package com.example.ecommerce_inventory_service.config;

import com.example.events.OrderAvroCreatedEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    // =====================================
    // 🔥 PRODUCER (para enviar stock events)
    // =====================================
    @Bean
    public ProducerFactory<String, SpecificRecord> producerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // 🔥 Schema Registry
        config.put("schema.registry.url", "http://localhost:8083");

        // 🔥 Serializers
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, SpecificRecord> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // =====================================
    // 🔥 CONSUMER (lee OrderAvroCreatedEvent)
    // =====================================
    @Bean
    public ConsumerFactory<String, OrderAvroCreatedEvent> consumerFactory() {

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-group-v2");

        // 🔥 Schema Registry
        config.put("schema.registry.url", "http://localhost:8083");

        // 🔥 Deserializers
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        // 🔥 CLAVE: devuelve objeto AVRO tipado
        config.put("specific.avro.reader", true);

        // 🔥 Leer desde inicio (útil para pruebas)
        //config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    // =====================================
    // 🔥 LISTENER FACTORY (LA CLAVE ABSOLUTA)
    // =====================================
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderAvroCreatedEvent> kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, OrderAvroCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // 🔥🔥🔥 ESTO SOLUCIONA TU ERROR
        // evita que Spring intente convertir el mensaje otra vez
        factory.setRecordMessageConverter(null);

        return factory;
    }
}