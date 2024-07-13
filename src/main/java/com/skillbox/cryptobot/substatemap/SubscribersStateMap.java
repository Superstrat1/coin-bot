package com.skillbox.cryptobot.substatemap;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
public class SubscribersStateMap {
    private HashMap<Long, SubStates> map = new HashMap<>();
}
