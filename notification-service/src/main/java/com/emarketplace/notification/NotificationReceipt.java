package com.emarketplace.notification;
import jakarta.persistence.*;
@Entity @Table(uniqueConstraints=@UniqueConstraint(columnNames={"notificationId","username"}))
public class NotificationReceipt {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @Column(nullable=false) private String notificationId;
    @Column(nullable=false) private String username;
    protected NotificationReceipt(){}
    public NotificationReceipt(String notificationId,String username){this.notificationId=notificationId;this.username=username;}
}
