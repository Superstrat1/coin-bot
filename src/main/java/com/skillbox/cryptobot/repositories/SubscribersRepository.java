package com.skillbox.cryptobot.repositories;

import com.skillbox.cryptobot.entities.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubscribersRepository extends JpaRepository<Subscriber, UUID> {

//    List<Subscriber> findByPrice(Integer price);
    boolean existsByTelegramId(long userId);
    Subscriber findByTelegramId(long userId);
}
