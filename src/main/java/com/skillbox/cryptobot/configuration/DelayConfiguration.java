package com.skillbox.cryptobot.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram.bot.notify.delay")
@Data
public class DelayConfiguration {

    private int value;
    private String unit;

}
