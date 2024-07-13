package com.skillbox.cryptobot.scheduler;

import com.skillbox.cryptobot.configuration.DelayConfiguration;
import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.service.CrudService;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class PriceCallAndNotificationExecutor {

    private DelayConfiguration delayConfiguration;
    private CrudService<Subscriber> crudService;
    private CryptoCurrencyService cryptoCurrencyService;


    @Autowired
    public PriceCallAndNotificationExecutor(DelayConfiguration delayConfiguration, CrudService<Subscriber> crudService, CryptoCurrencyService cryptoCurrencyService) {
        this.delayConfiguration = delayConfiguration;
        this.crudService = crudService;
        this.cryptoCurrencyService = cryptoCurrencyService;
    }

    public Double priceCalling() {
        double currentCoinPrice;
        try {
            currentCoinPrice = cryptoCurrencyService.getBitcoinPrice();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return currentCoinPrice;
    }

    private LocalDateTime dateTimeComputer() {
        ChronoUnit unit;
        switch (delayConfiguration.getUnit()) {
            case ("seconds") -> unit = ChronoUnit.SECONDS;
            case ("minutes") -> unit = ChronoUnit.MINUTES;
            case ("hours") -> unit = ChronoUnit.HOURS;
            default -> unit = ChronoUnit.MINUTES;
        }
        //etc.
        int delay = delayConfiguration.getValue();

        return LocalDateTime.now().minus(delay, unit);
    }

    public List<Subscriber> getRequiredUsers() {
        return crudService.getByPriceAndDateTime(priceCalling(), dateTimeComputer());
    }
}
