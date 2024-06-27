package org.example.backend.chat;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


public interface MessageRepository extends JpaRepository<Message, Long> {
    // 메시지 내용을 검색하는 쿼리 메서드
    Optional<Message> findTopByContentContainingIgnoreCaseAndTimestampAfterOrderByTimestampDesc(String content, Timestamp lastLoginTime);

    @Query("SELECT m FROM Message m " + "WHERE LOWER(m.content) LIKE CONCAT('%', LOWER(:content), '%') " + "AND m.timestamp > :lastLoginTime " + "AND m.id < :messageId " + "ORDER BY m.timestamp DESC " + "LIMIT 1")
    Optional<Message> findTopByContentContainingIgnoreCaseAndTimestampAfterAndIdLessThanOrderByTimestampDesc(@Param("content") String content, @Param("lastLoginTime") Timestamp lastLoginTime, @Param("messageId") Long messageId);

    @Query("SELECT m FROM Message m " + "WHERE m.id < :id " + "AND m.timestamp > :timestamp " + "ORDER BY m.timestamp ASC " + "FETCH FIRST 10 ROWS ONLY")
    List<Message> findTop10ByIdLessThanAndTimestampAfterOrderByTimestampAsc(@Param("id") Long id, @Param("timestamp") Timestamp timestamp);

    @Query("SELECT m FROM Message m " + "WHERE m.id > :id " + "AND m.timestamp > :timestamp " + "ORDER BY m.timestamp ASC " + "FETCH FIRST 10 ROWS ONLY")
    List<Message> findTop10ByIdGreaterThanAndTimestampAfterOrderByTimestampAsc(@Param("id") Long id, @Param("timestamp") Timestamp timestamp);

}