package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.service.CrudService;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.slf4j.SLF4JLogBuilder;
import org.apache.logging.slf4j.SLF4JLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Обработка команды отмены подписки на курс валюты
 */
@Service
@Slf4j
@AllArgsConstructor
public class UnsubscribeCommand implements IBotCommand {

    @Autowired
    private CrudService<Subscriber> service;

    @Override
    public String getCommandIdentifier() {
        return "unsubscribe";
    }

    @Override
    public String getDescription() {
        return "Отменяет подписку пользователя";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        long userId = message.getFrom().getId();
        Subscriber subscriber = service.getByTelegramId(userId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        if (subscriber != null) {
            if (subscriber.getPrice() == null) {
                log.info("User {} requested /unsubscribe command and have not subscription", userId);
                sendMessage.setText("Активные подписки отсутствуют");
            } else {
                log.info("User {} requested /unsubscribe command with subscription {}", userId, subscriber.getPrice());
                subscriber.setPrice(null);
                service.change(subscriber);
                sendMessage.setText("Ваша подписка удалена!");
            }

        } else {
            log.warn("Unregistered user {} requested subscription", userId);
            sendMessage.setText("Что то пошло не так! Попробуйте использовать команду /start и повторить действие");
        }
        try {
            log.debug("User {} requested /unsubscribe and get message {}", userId, sendMessage);
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}