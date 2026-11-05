package com.emarketplace.notification;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class NotificationMessage {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @Column(nullable=false) private String senderUsername;
    @Column(nullable=false) private String recipientUsername;
    @Column(nullable=false) private String recipientRole;
    @Column(nullable=false,length=2000) private String message;
    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false) private boolean readMessage;
    protected NotificationMessage(){}
    public NotificationMessage(String sender,String recipient,String role,String message,Instant createdAt){this.senderUsername=sender;this.recipientUsername=recipient;this.recipientRole=role;this.message=message;this.createdAt=createdAt;}
    public String getId(){return id;} public String getSenderUsername(){return senderUsername;} public String getRecipientUsername(){return recipientUsername;} public String getRecipientRole(){return recipientRole;} public String getMessage(){return message;} public Instant getCreatedAt(){return createdAt;} public boolean isReadMessage(){return readMessage;} public void markRead(){readMessage=true;}
}
