package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.substatemap.SubscribersStateMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class StopSubscriptionCommand implements IBotCommand {

    @Autowired
    private SubscribersStateMap map;
    @Override
    public String getCommandIdentifier() {
        return "stop_subscription";
    }

    @Override
    public String getDescription() {
        return "остановить процесс подписки";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        if (map.getMap().containsKey(userId)) {
            map.getMap().remove(userId);
            sendMessage.setText("""
                Процесс подписки остановлен!
                Обратите внимание - эта команда не удаляет подписку, если она у вас была до этого
                Команда /help поможет решить что делать дальше
                """);
        } else {
            sendMessage.setText("Процесс подписки не запущен. Он запускается командой /subscribe");
        }

        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
