package com.skillbox.cryptobot.service;

import java.time.LocalDateTime;
import java.util.List;

public interface CrudService<T> {

//    T getByPrice(Integer price);

    T getByTelegramId(long id);
    T create(T entity);
    void change(T entity);
    void delete(T entity);
    boolean existsByTelegramId(long userId);
    List<T> getByPriceAndDateTime(Double price, LocalDateTime dateTime);
}
