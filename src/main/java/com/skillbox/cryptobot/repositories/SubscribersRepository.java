package com.skillbox.cryptobot.repositories;

import com.skillbox.cryptobot.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscribersRepository extends JpaRepository<Subscriber, UUID> {

    Optional<Subscriber> findByPrice(Integer price);

    boolean existsByTelegramId(long userId);
}
