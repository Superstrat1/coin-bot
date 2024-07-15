package com.skillbox.cryptobot.datetimecalculator;

import com.skillbox.cryptobot.configuration.DelayConfiguration;
import com.skillbox.cryptobot.entities.Subscriber;
import com.skillbox.cryptobot.service.CrudService;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class DateTimeNotificationCalculator {

    @Autowired
    private DelayConfiguration delayConfiguration;

    private static ChronoUnit unit;



    public LocalDateTime dateTimeCalculation() {
        if (unit == null) {
            switch (delayConfiguration.getUnit()) {
                case ("seconds") -> unit = ChronoUnit.SECONDS;
                case ("minutes") -> unit = ChronoUnit.MINUTES;
                case ("hours") -> unit = ChronoUnit.HOURS;
                default -> unit = ChronoUnit.MINUTES;
            }
            log.debug("ChronoUnit get {} value", unit);
            log.debug("Delay for notification = {}", delayConfiguration.getValue());
        }

        //etc.
        return LocalDateTime.now().minus(delayConfiguration.getValue(), unit);
    }
}
