package com.skillbox.cryptobot.bot.command;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class HelpCommand implements IBotCommand {
    @Override
    public String getCommandIdentifier() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "описание бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();

        String text = """
                Данный бот предназначен для помощи в торговле биткоином
                Вы можете подписаться на интересующую вас стоимость, и бот пришлет вам уведомление, когда цена,
                которую бот проверяет каждые 2 минуты, будет ниже или равна той на которую вы подписались.
                Бот работает только с командами. Их список чуть ниже!
                Если бот никак не реагирует на какие то команды попробуйте ввести команду /start, и повторить процедуру
                Поддерживаемые команды:
                 /get_price - получить стоимость биткоина
                 /subscribe - подписаться на определенную стоимость биткоина
                 /get_subscription - посмотреть свою подписку
                 /unsubscribe - отменить подписку
                 /stop_subscription - остановить процедуру подписку
                 /help - небольшая помощь
                """;
        sendMessage.setChatId(userId);
        sendMessage.setText(text);
        try {
            absSender.execute(sendMessage);
            log.debug("User {} requested /help", userId);
        } catch (TelegramApiException e) {
            log.error("Error in /help command from user {}", userId);
        }

    }
}
