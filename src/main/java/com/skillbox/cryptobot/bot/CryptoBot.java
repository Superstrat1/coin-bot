package com.skillbox.cryptobot.bot;

import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.scheduler.PriceCallAndNotificationExecutor;
import com.skillbox.cryptobot.service.CrudService;
import com.skillbox.cryptobot.substatemap.SubscribersStateMap;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;

    @Autowired
    private SubscribersStateMap map;
    @Autowired
    private PriceCallAndNotificationExecutor priceCallAndNotificationExecutor;
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
        SendMessage subscriptionNotificationMessage = new SendMessage();
        long userId = update.getMessage().getFrom().getId();
        sm.setChatId(userId);
        subscriptionNotificationMessage.setChatId(userId);
        subscriptionNotificationMessage.setText("");

        if (map.getMap().containsKey(userId)) {
            switch (map.getMap().get(userId)) {
                case WAITING_FOR_SUBSCRIPTION_PRICE -> {
                    Subscriber subscriber = subscriberCrudService.getByTelegramId(userId);
                    if (subscriber != null) {
                        Double price;
                        try {
                            price = Double.valueOf(update.getMessage().getText().replace(",", "."));
                            if (price < 0 || price > Double.MAX_VALUE || Double.isNaN(price)) {
                                throw new NumberFormatException();
                            }
                        } catch (Exception e) {
                            sm.setText("""
                                    Введите интересующую стоимость цифрами!
                                    Цена может быть выражена десятичной дробью и должна быть больше нуля.
                                    Либо используйте команду /stop_subscription для остановки процедуры
                                    """);
                            try {
                                execute(sm);
                                return;
                            } catch (TelegramApiException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                            subscriber.setPrice(price);
                            subscriberCrudService.change(subscriber);
                            map.getMap().remove(userId);
                            sm.setText("Новая подписка создана на стоимость " + TextUtil.toString(price) + " USD"
                                    + "\nКогда цена биткоина будет ниже или равна вашей, вам придет уведомление!");
                            subscriptionNotificationMessage.setText(subscriptionNotificationText(price, userId));
                    } else {
                        sm.setText("Что то пошло не так!\nИспользуйте команду /start и повторите попытку");
                    }
                }
            }

        } else {
            String message = """
                    Этот бот взаимодействует только с командами!
                    Используйте команду - /help для лучшего понимания взаимодействия
                    """;
            sm.setText(message);
        }

        try {
            execute(sm);
            if (!subscriptionNotificationMessage.getText().isEmpty()) {
                execute(subscriptionNotificationMessage);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedDelayString = "${telegram.bot.notify.delay.check-duration}")
    public void notification() {
        SendMessage message = new SendMessage();
        double price = priceCallAndNotificationExecutor.priceCalling();

        priceCallAndNotificationExecutor.getRequiredUsers().forEach(sub -> {
            message.setChatId(sub.getTelegramId());
            sub.setLastNotification(LocalDateTime.now());
            String text = "Пора покупать, цена биткоина " + TextUtil.toString(price) + "\nВаша цена "
                    + TextUtil.toString(sub.getPrice()) + ". Разница " + (sub.getPrice() - price) + ".";
            message.setText(text);
            subscriberCrudService.change(sub);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String subscriptionNotificationText(Double price, Long id) {

        String text;
        Double currentPrice = priceCallAndNotificationExecutor.priceCalling();

        if (price > currentPrice) {
            text = "Стоимость биткоина ниже вашей подписки! Сейчас биткоин стоит " + TextUtil.toString(currentPrice) + " USD"
                    + "\nВаша цена " + TextUtil.toString(price) + ". Разница " + (price - currentPrice) + ".";
            Subscriber subscriber = subscriberCrudService.getByTelegramId(id);
            subscriber.setLastNotification(LocalDateTime.now());
            subscriberCrudService.change(subscriber);
        } else {
            text = "Текущая стоимость биткоина " + TextUtil.toString(currentPrice) + " USD";
        }
        return text;
    }
}
