package com.project.revconnect.repository;

import com.project.revconnect.model.Message;
import com.project.revconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {


    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);


    @Query("SELECT DISTINCT u FROM User u WHERE u IN (SELECT m.receiver FROM Message m WHERE m.sender = :user) OR u IN (SELECT m.sender FROM Message m WHERE m.receiver = :user)")
    List<User> findMessagePartners(@Param("user") User user);

    long countByReceiverAndReadFalse(User receiver);

    @Query("SELECT m.sender.id, COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.read = false GROUP BY m.sender.id")
    List<Object[]> countUnreadBySender(@Param("receiver") User receiver);

    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.receiver = :receiver AND m.sender = :sender AND m.read = false")
    int markConversationAsRead(@Param("receiver") User receiver, @Param("sender") User sender);
}
