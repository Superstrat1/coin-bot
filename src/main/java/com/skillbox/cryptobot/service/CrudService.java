package com.skillbox.cryptobot.service;

public interface CrudService<T> {

//    T getByPrice(Integer price);

    T getByTelegramId(long id);
    T create(T entity);
    void change(T entity);
    void delete(T entity);
    boolean existsByTelegramId(long userId);
}
