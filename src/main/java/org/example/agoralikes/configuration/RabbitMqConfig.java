package org.example.agoralikes.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.queue.like-created}")
    private String likeCreatedQueue;

    @Value("${rabbitmq.queue.like-deleted}")
    private String likeDeletedQueue;

    @Value("${rabbitmq.exchange.likes}")
    private String likesExchange;

    @Value("${rabbitmq.routing.key.like-created}")
    private String likeCreatedRoutingKey;

    @Value("${rabbitmq.routing.key.like-deleted}")
    private String likeDeletedRoutingKey;

    // 1. Создаем Exchange
    @Bean
    public TopicExchange likesExchange() {
        return new TopicExchange(likesExchange);
    }

    // 2. Создаем Очереди (Вот зачем нужны эти свойства!)
    @Bean
    public Queue createLikeQueue() {
        return new Queue(likeCreatedQueue, true);
    }

    @Bean
    public Queue deleteLikeQueue() {
        return new Queue(likeDeletedQueue, true);
    }

    // 3. Связываем (Binding) - Чтобы магия заработала
    @Bean
    public Binding bindingCreated() {
        return BindingBuilder.bind(createLikeQueue())
                .to(likesExchange())
                .with(likeCreatedRoutingKey);
    }

    @Bean
    public Binding bindingDeleted() {
        return BindingBuilder.bind(deleteLikeQueue())
                .to(likesExchange())
                .with(likeDeletedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
