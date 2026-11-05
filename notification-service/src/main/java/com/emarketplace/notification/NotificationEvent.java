package com.emarketplace.notification;
import java.time.Instant;
public record NotificationEvent(String senderUsername,String recipientUsername,String recipientRole,String message,Instant createdAt){}
