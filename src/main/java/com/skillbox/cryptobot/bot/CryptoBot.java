package com.skillbox.cryptobot.bot;

import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.service.CrudService;
import com.skillbox.cryptobot.substatemap.SubStateMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;

    @Autowired
    private SubStateMap map;
    @Autowired
    private CrudService<Subscriber> subscriberCrudService;


    public CryptoBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            List<IBotCommand> commandList
    ) {
        super(botToken);
        this.botUsername = botUsername;

        commandList.forEach(this::register);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        SendMessage sm = new SendMessage();
        long userId = update.getMessage().getFrom().getId();
        sm.setChatId(userId);

        if (map.getMap().containsKey(userId)) {
            switch (map.getMap().get(userId)) {
                case WAITING_FOR_SUBSCRIPTION_PRICE -> {
                    Integer price = Integer.valueOf(update.getMessage().getText());
                    //TODO сделать метод проверки на цифры
                    Subscriber subscriber = subscriberCrudService.getByTelegramId(userId);
                    subscriber.setPrice(price);
                    subscriberCrudService.change(subscriber);
                    map.getMap().remove(userId);
                    log.info("Подписчик " + userId + " подписался на цену - " + price);
                    sm.setText("Новая подписка создана на стоимость " + update.getMessage().getText() + " USD"
                            + "\n" + "Когда цена биткоина будет ниже или равна вашей, вам придет уведомление!");
                }
            }

        } else {
            String message = """
                    Этот бот взаимодействует только с командами!
                    Пожалуйста введите команду - /start для отображения списка доступных команд
                    """;
            sm.setText(message);
        }

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
