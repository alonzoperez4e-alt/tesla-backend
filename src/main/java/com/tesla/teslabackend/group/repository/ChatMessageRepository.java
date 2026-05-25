package com.tesla.teslabackend.group.repository;

import com.tesla.teslabackend.group.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByGroupIdOrderByTimestampAsc(Long groupId);

    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.timestamp < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("DELETE FROM ChatMessage c WHERE c.groupId = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}