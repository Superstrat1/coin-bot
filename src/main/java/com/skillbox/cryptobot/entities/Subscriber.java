package com.skillbox.cryptobot.entities;

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
    private Double price;
    @Column(name = "last_notification")
    private LocalDateTime lastNotification;
}
