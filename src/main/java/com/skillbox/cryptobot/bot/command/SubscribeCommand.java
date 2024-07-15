package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.service.CrudService;
import com.skillbox.cryptobot.substatemap.SubscribersStateMap;
import com.skillbox.cryptobot.substatemap.SubStates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
public class SubscribeCommand implements IBotCommand {

    @Autowired
    private SubscribersStateMap map;
    @Autowired
    private CrudService<Subscriber> subscriberCrudService;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        long userId = message.getFrom().getId();
        Subscriber subscriber = subscriberCrudService.getByTelegramId(userId);
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        if (subscriber != null) {
            map.getMap().put(userId, SubStates.WAITING_FOR_SUBSCRIPTION_PRICE);
            log.info("User {} requested /subscribe command and get state", userId);
            sm.setText("Введите стоимость биткоина на которую вы хотите подписаться");
        } else {
            log.warn("Unregistered user {} requested subscription", userId);
            sm.setText("Что то пошло не так! Попробуйте использовать команду /start и повторить действие");
        }
        try {
            absSender.execute(sm);
        } catch (TelegramApiException e) {
            log.error("Error in /subscribe command from user {}", userId);
        }
    }
}