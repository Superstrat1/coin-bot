package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.repositories.SubscribersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrudSubServiceImpl implements CrudService<Subscriber> {

    @Autowired
    private SubscribersRepository repository;


    @Override
    public Subscriber getByTelegramId(long id) {
        return repository.findByTelegramId(id);
    }

    @Override
    public Subscriber create(Subscriber entity) {
        return repository.save(entity);
    }

    @Override
    public void change(Subscriber entity) {
        repository.save(entity);
    }

    @Override
    public void delete(Subscriber entity) {
        repository.delete(entity);
    }

    @Override
    public boolean existsByTelegramId(long userId) {
        return repository.existsByTelegramId(userId);
    }


}
