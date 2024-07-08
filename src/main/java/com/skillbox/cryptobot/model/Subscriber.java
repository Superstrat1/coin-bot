package com.skillbox.cryptobot.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscribers")
@Data
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    @Column(name = "telegram_id", unique = true)
    private Long telegramId;
    private Integer price;
    @Column(name = "last_notification")
    private LocalDateTime lastNotification;
}
