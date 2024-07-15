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
    private final String regex = "[0-9]{1,20}.?[0-9]{0,8}";

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
        long userId = update.getMessage().getFrom().getId();
        String inputMessage = update.getMessage().getText();
        mainMessage.setChatId(userId);

        try {
            if (!map.getMap().containsKey(userId)) {
                anyInputExceptRight(mainMessage);
                throw new IllegalArgumentException(
                        String.format("Stateless user %d input: %s", userId, inputMessage));
            }
            SubStates state = map.getMap().get(userId);
            Subscriber subscriber = subscriberCrudService.getByTelegramId(userId);
            if (subscriber == null) {
                nullUserMessage(mainMessage);
                throw new NullPointerException(
                        String.format("Unregistered user %d with state: %s, input: %s", userId, state, inputMessage));
            }
            switch (state) {
                case WAITING_FOR_SUBSCRIPTION_PRICE -> {
                    String editedInput = inputMessage.replace(",", ".");
                    if (!editedInput.matches(regex)) {
                        incorrectInput(mainMessage);
                        throw new IllegalArgumentException(
                                String.format("User %d input incorrect value %s", userId, inputMessage));
                    }
                    subscription(subscriber, editedInput, mainMessage);
                }
            }
        } catch (Exception e) {
            log.error("Error occurred", e);
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

    private String subscriptionNotification(Double price, Long id) {

        String text;
        Double currentPrice;
        try {
            currentPrice = cryptoCurrencyService.getBitcoinPrice();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (price > currentPrice) {
            text = "Стоимость биткоина ниже вашей подписки! Сейчас биткоин стоит " + TextUtil.toString(currentPrice) + " USD"
                    + "\nВаша цена " + TextUtil.toString(price) + ". Разница " + TextUtil.toString(price - currentPrice);
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

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred in sendMessage method", e);
        }
    }

    private void subscription(Subscriber subscriber, String input, SendMessage message) {
        long userId = subscriber.getTelegramId();
        Double price = Double.parseDouble(input);
        subscriber.setPrice(price);
        subscriberCrudService.change(subscriber);
        map.getMap().remove(userId);
        log.info("Subscription must be created for user {} with price {}", userId, price);
        String newSubscriptionCreated = "Новая подписка создана на стоимость " + price + " USD"
                + "\nКогда цена биткоина будет ниже или равна вашей, вам придет уведомление!";
        message.setText(newSubscriptionCreated);
        sendMessage(message);
        message.setText(subscriptionNotification(price, userId));
        sendMessage(message);
    }

    private void nullUserMessage(SendMessage message) {
        String messageText = "Что то пошло не так!\nИспользуйте команду /start и повторите попытку";
        message.setText(messageText);
        sendMessage(message);
    }

    private void incorrectInput(SendMessage message) {
        String messageText = """
                Введите интересующую стоимость цифрами!
                Цена может быть выражена десятичной дробью и должна быть больше нуля.
                Для дроби не более 8 знаков после запятой.
                Либо используйте команду /stop_subscription для остановки процедуры
                """;
        message.setText(messageText);
        sendMessage(message);
    }

    private void anyInputExceptRight(SendMessage message) throws IllegalArgumentException {

        String anyInputWithoutState = """
                Этот бот взаимодействует только с командами!
                Используйте команду - /help для лучшего понимания взаимодействия
                """;
        message.setText(anyInputWithoutState);
        sendMessage(message);
    }
}
