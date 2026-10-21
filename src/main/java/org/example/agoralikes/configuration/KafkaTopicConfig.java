package org.example.agoralikes.configuration;

import org.springframework.kafka.support.serializer.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic likesTopic() {
        return TopicBuilder.name("agora.likes")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public DefaultKafkaProducerFactoryCustomizer jsonSerializerCustomizer(ObjectMapper objectMapper) {
        return producerFactory -> ((DefaultKafkaProducerFactory<Object, Object>) producerFactory)
                .setValueSerializerSupplier(() -> new JsonSerializer<>(objectMapper));
    }

}
