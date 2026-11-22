package com.foodDelivery.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InfrastructureHealthRunner implements CommandLineRunner {

    private final RedisConnectionFactory redisConnectionFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public InfrastructureHealthRunner(
            RedisConnectionFactory redisConnectionFactory,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void run(String... args) throws Exception {

        // Test Redis
        String redisResponse = redisConnectionFactory
                .getConnection()
                .ping();

        System.out.println("Redis connection: " + redisResponse);

        // Test Kafka
        kafkaTemplate.send(
                "order-created",
                "infrastructure-test",
                "Spring Boot connected to Kafka!"
        ).get();

        System.out.println("Kafka message sent successfully!");
    }
}