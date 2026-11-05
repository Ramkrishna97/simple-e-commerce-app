package com.emarketplace.notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NotificationReceiptRepository extends JpaRepository<NotificationReceipt,String>{boolean existsByNotificationIdAndUsername(String notificationId,String username);}
