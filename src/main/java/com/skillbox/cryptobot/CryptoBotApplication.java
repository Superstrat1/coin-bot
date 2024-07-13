package com.skillbox.cryptobot;

import com.skillbox.cryptobot.configuration.DelayConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoBotApplication.class, args);
    }

}
