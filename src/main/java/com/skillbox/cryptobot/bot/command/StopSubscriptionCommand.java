package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.substatemap.SubscribersStateMap;
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
            log.info("User {} quested /stop_subscription command and lost him state", userId);
            sendMessage.setText("""
                Процесс подписки остановлен!
                Обратите внимание - эта команда не удаляет подписку, если она у вас была до этого
                Команда /help поможет решить что делать дальше
                """);
        } else {
            sendMessage.setText("Процесс подписки не запущен. Он запускается командой /subscribe");
            log.debug("Stateless user {} requested /stop_subscription command", userId);
        }

        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error in/stop_subscription command from user {}", userId);
        }
    }
}
