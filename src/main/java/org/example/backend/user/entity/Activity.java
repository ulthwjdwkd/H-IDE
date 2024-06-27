package org.example.backend.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity")
@Getter @Setter
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private User user;

    // 인증 횟수
    @Column(name = "point", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int point;

    @Column(name = "level", nullable = false, columnDefinition = "INT DEFAULT 1")
    private int level;

    @Column(name = "last_point_update_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime lastPointUpdateAt;

    public Activity() {
    }

    public Activity(User user) {
        this.user = user;
        this.point = 0;
        this.level = 1;
        this.lastPointUpdateAt = LocalDateTime.now();
    }

}
