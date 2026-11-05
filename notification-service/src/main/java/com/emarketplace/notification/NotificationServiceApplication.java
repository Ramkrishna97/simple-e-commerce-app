package com.emarketplace.notification;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args){SpringApplication.run(NotificationServiceApplication.class,args);}
    @Bean NewTopic notificationTopic(@Value("${app.notification-topic}") String topic){return TopicBuilder.name(topic).partitions(1).replicas(1).build();}
}
