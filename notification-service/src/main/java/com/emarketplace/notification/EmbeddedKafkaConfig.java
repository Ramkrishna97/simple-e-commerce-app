package com.emarketplace.notification;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

@Configuration
public class EmbeddedKafkaConfig {
    @Bean(destroyMethod = "destroy")
    EmbeddedKafkaBroker embeddedKafkaBroker(@Value("${app.notification-topic}") String topic) {
        return new EmbeddedKafkaKraftBroker(1, 1, topic)
            .brokerListProperty("spring.kafka.bootstrap-servers");
    }

    @Bean KafkaAdmin kafkaAdmin(EmbeddedKafkaBroker broker){return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,broker.getBrokersAsString()));}
    @Bean ProducerFactory<String,String> producerFactory(EmbeddedKafkaBroker broker){return new DefaultKafkaProducerFactory<>(Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,broker.getBrokersAsString(),ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class,ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class));}
    @Bean KafkaTemplate<String,String> kafkaTemplate(ProducerFactory<String,String> producerFactory){return new KafkaTemplate<>(producerFactory);}
    @Bean ConsumerFactory<String,String> consumerFactory(EmbeddedKafkaBroker broker){Map<String,Object> config=new HashMap<>();config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,broker.getBrokersAsString());config.put(ConsumerConfig.GROUP_ID_CONFIG,"notification-service");config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);return new DefaultKafkaConsumerFactory<>(config);}
    @Bean ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory(ConsumerFactory<String,String> consumers){ConcurrentKafkaListenerContainerFactory<String,String> factory=new ConcurrentKafkaListenerContainerFactory<>();factory.setConsumerFactory(consumers);return factory;}
}
