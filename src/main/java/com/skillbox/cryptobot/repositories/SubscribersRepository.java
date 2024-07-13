package com.skillbox.cryptobot.repositories;

import com.skillbox.cryptobot.entities.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubscribersRepository extends JpaRepository<Subscriber, UUID> {

    boolean existsByTelegramId(long userId);
    Subscriber findByTelegramId(long userId);

    @Query(value = "select * from subscribers where price >= ?1 and (last_notification <= ?2 or last_notification is null)", nativeQuery = true)
    List<Subscriber> findByPriceAndLastNotification(Double price, LocalDateTime dateTime);
}
