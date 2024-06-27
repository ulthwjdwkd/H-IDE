package org.example.backend.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backend.user.entity.User;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private Timestamp timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User user; // 유저 객체를 참조

    public Message() {
        this.timestamp = Timestamp.from(Instant.now());
    }
}