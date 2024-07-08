package com.skillbox.cryptobot.substatemap;

import jakarta.persistence.Column;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
public class SubStateMap {
    private HashMap<Long, SubStates> map = new HashMap<>();
}
