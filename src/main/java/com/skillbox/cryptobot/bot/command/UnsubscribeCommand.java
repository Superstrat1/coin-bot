package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.service.CrudService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        if(subscriber.getPrice() == null) {
            sendMessage.setText("Активные подписки отсутствуют");
        } else {
            subscriber.setPrice(null);
            service.change(subscriber);
            sendMessage.setText("Ваша подписка удалена!");
        }

        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}