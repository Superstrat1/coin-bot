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

@Service
@Slf4j
@AllArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    @Autowired
    private CrudService<Subscriber> service;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        long userId = message.getFrom().getId();
        Subscriber subscriber = service.getByTelegramId(userId);
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());

        if(subscriber != null) {
            if (subscriber.getPrice() == null) {
                sm.setText("Активные подписки отсутствуют");
            } else {
                sm.setText("Вы подписаны на стоимость биткоина " + subscriber.getPrice() + " USD");
            }
        } else {
            sm.setText("Что то пошло не так! Попробуйте использовать команду /start и повторить действие");
        }
        try {
            absSender.execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}