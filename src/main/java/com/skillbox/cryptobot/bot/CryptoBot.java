package com.skillbox.cryptobot.bot;

import com.skillbox.cryptobot.datetimecalculator.DateTimeNotificationCalculator;
import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.service.CrudService;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.substatemap.SubStates;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class CryptoBot extends TelegramLongPollingCommandBot {

    private final String botUsername;

    @Autowired
    private SubscribersStateMap map;
    @Autowired
    private CryptoCurrencyService cryptoCurrencyService;
    @Autowired
    private CrudService<Subscriber> subscriberCrudService;
    @Autowired
    private DateTimeNotificationCalculator dateTimeNotificationCalculator;


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
        SendMessage mainMessage = new SendMessage();
        SendMessage subscriptionNotificationMessage = new SendMessage();
        long userId = update.getMessage().getFrom().getId();
        String inputMessage = update.getMessage().getText();
        mainMessage.setChatId(userId);
        subscriptionNotificationMessage.setChatId(userId);
        subscriptionNotificationMessage.setText("");

        if (map.getMap().containsKey(userId)) {
            SubStates state = map.getMap().get(userId);
            switch (state) {
                case WAITING_FOR_SUBSCRIPTION_PRICE -> {
                    Subscriber subscriber = subscriberCrudService.getByTelegramId(userId);
                    if (subscriber != null) {
                        Double price;
                        try {
                            price = Double.valueOf(inputMessage.replace(",", "."));
                            if (price < 0 || price > Double.MAX_VALUE || Double.isNaN(price)) {
                                throw new NumberFormatException();
                            }
                        } catch (Exception e) {
                            log.error("User {} input wrong price: {}", userId,inputMessage, e);
                            mainMessage.setText("""
                                    Введите интересующую стоимость цифрами!
                                    Цена может быть выражена десятичной дробью и должна быть больше нуля.
                                    Либо используйте команду /stop_subscription для остановки процедуры
                                    """);
                            try {
                                execute(mainMessage);
                                return;
                            } catch (TelegramApiException ex) {
                                log.warn("User {} not get message {}", userId, mainMessage, ex);
                                throw new RuntimeException(ex);
                            }
                        }
                            subscriber.setPrice(price);
                            subscriberCrudService.change(subscriber);
                            map.getMap().remove(userId);
                            log.info("Subscription must be created for user {} with price {}", userId, price);
                            mainMessage.setText("Новая подписка создана на стоимость " + TextUtil.toString(price) + " USD"
                                    + "\nКогда цена биткоина будет ниже или равна вашей, вам придет уведомление!");
                            subscriptionNotificationMessage.setText(subscriptionNotificationText(price, userId));
                    } else {
                        log.warn("Unregistered user {} with state: {}, input: {}", userId, state, inputMessage);
                        mainMessage.setText("Что то пошло не так!\nИспользуйте команду /start и повторите попытку");
                    }
                }
            }

        } else {
            log.debug("Stateless user {} input: {}", userId, inputMessage);
            String message = """
                    Этот бот взаимодействует только с командами!
                    Используйте команду - /help для лучшего понимания взаимодействия
                    """;
            mainMessage.setText(message);
        }

        try {
            execute(mainMessage);
            log.debug("User {} get message: {}", userId, mainMessage);
            if (!subscriptionNotificationMessage.getText().isEmpty()) {
                execute(subscriptionNotificationMessage);
            }
        } catch (TelegramApiException e) {
            log.warn("User {} not get mainMessage {}, or subNotificationMessage {}",
                    userId, mainMessage, subscriptionNotificationMessage, e);
        }
    }

    @Scheduled(fixedDelayString = "${telegram.bot.notify.delay.check-duration}")
    public void notification() {
        log.trace("Bot notification method was execute. Time: {}", LocalDateTime.now());
        SendMessage message = new SendMessage();
        Double price;
        try {
            price = cryptoCurrencyService.getBitcoinPrice();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.trace("Bot notification method priceCall: {}", price);

        subscriberCrudService.getByPriceAndDateTime(price, dateTimeNotificationCalculator.dateTimeCalculation()).forEach(sub -> {
            message.setChatId(sub.getTelegramId());
            LocalDateTime oldDate = sub.getLastNotification();
            sub.setLastNotification(LocalDateTime.now());

            String text = "Пора покупать, цена биткоина " + TextUtil.toString(price) + "\nВаша цена "
                    + TextUtil.toString(sub.getPrice()) + ". Разница " + (sub.getPrice() - price) + ".";
            message.setText(text);
            subscriberCrudService.change(sub);
            try {
                execute(message);
                log.trace("User {} with subPrice {} was notificated. His old lastNotification date {} was changed to {}",
                        sub.getTelegramId(), sub.getPrice(), oldDate, sub.getLastNotification());
            } catch (TelegramApiException e) {
                log.warn("User {} not get message {}", sub.getTelegramId(), message, e);
            }
        });
    }

    private String subscriptionNotificationText(Double price, Long id) {

        String text;
        Double currentPrice;
        try {
            currentPrice = cryptoCurrencyService.getBitcoinPrice();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (price > currentPrice) {
            text = "Стоимость биткоина ниже вашей подписки! Сейчас биткоин стоит " + TextUtil.toString(currentPrice) + " USD"
                    + "\nВаша цена " + TextUtil.toString(price) + ". Разница " + (price - currentPrice) + ".";
            Subscriber subscriber = subscriberCrudService.getByTelegramId(id);
            subscriber.setLastNotification(LocalDateTime.now());
            subscriberCrudService.change(subscriber);
            log.info("User {} input price greater than current price of BitCoin: {}, lastNotification time was changed {}",
                    id, price, subscriber.getLastNotification());
        } else {
            text = "Текущая стоимость биткоина " + TextUtil.toString(currentPrice) + " USD";
        }
        return text;
    }
}
