package com.emarketplace.notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface NotificationRepository extends JpaRepository<NotificationMessage,String>{
    List<NotificationMessage> findByRecipientUsernameOrderByCreatedAtDesc(String username);
    @Query("select n from NotificationMessage n where n.recipientUsername=:username or (n.recipientUsername='*' and (n.recipientRole=:role or n.recipientRole='ALL')) order by n.createdAt desc")
    List<NotificationMessage> findInbox(@Param("username")String username,@Param("role")String role);
}
