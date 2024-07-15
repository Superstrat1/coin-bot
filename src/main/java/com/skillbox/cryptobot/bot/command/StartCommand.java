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
 * Обработка команды начала работы с ботом
 */
@Service
@AllArgsConstructor
@Slf4j
public class StartCommand implements IBotCommand {

    @Autowired
    private CrudService<Subscriber> service;
    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {

        long userId = message.getFrom().getId();
        SendMessage answer = new SendMessage();
        answer.setChatId(message.getChatId());

        String name = message.getFrom().getFirstName(); // delete after

        if(!service.existsByTelegramId(userId)) {
            Subscriber subscriber = new Subscriber();
            subscriber.setTelegramId(userId);
            Subscriber savedSubscriber = service.create(subscriber);
            log.info("{} was created", savedSubscriber);
        }

        String hello = "Привет, " + name + "! Данный бот помогает отслеживать стоимость биткоина.\n";
        String commands = """
                Поддерживаемые команды:
                 /get_price - получить стоимость биткоина
                 /subscribe - подписаться на определенную стоимость биткоина
                 /get_subscription - посмотреть свою подписку
                 /unsubscribe - отменить подписку
                 /stop_subscription - остановить процедуру подписку
                 /help - небольшая помощь
                """;
        answer.setText(hello + commands);
        try {
            absSender.execute(answer);
            log.debug("User {} requested /start command", userId);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command from user {}", userId,e);
        }
    }
}