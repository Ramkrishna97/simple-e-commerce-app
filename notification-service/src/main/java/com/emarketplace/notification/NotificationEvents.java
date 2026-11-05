package com.emarketplace.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationEvents {
    private final KafkaTemplate<String,String> kafka; private final ObjectMapper json; private final NotificationRepository notifications; private final String topic;
    public NotificationEvents(KafkaTemplate<String,String> kafka,ObjectMapper json,NotificationRepository notifications,@Value("${app.notification-topic}")String topic){this.kafka=kafka;this.json=json;this.notifications=notifications;this.topic=topic;}
    public void publish(NotificationEvent event){try{kafka.send(topic,event.recipientUsername(),json.writeValueAsString(event));}catch(JsonProcessingException e){throw new IllegalArgumentException("Message could not be serialized",e);}}
    @KafkaListener(topics="${app.notification-topic}") public void receive(String payload){try{NotificationEvent event=json.readValue(payload,NotificationEvent.class);notifications.save(new NotificationMessage(event.senderUsername(),event.recipientUsername(),event.recipientRole(),event.message(),event.createdAt()));}catch(JsonProcessingException e){throw new IllegalArgumentException("Invalid notification event",e);}}
}
