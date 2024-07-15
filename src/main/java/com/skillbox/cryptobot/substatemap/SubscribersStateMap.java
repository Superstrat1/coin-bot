package com.skillbox.cryptobot.substatemap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Data
@Slf4j
public class SubscribersStateMap {
    private HashMap<Long, SubStates> map = new HashMap<>(){
        @Override
        public SubStates put(Long key, SubStates value) {
            log.debug("User {} was put in StateMap with state {}", key, value);
            return super.put(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            log.debug("User {} was remove from StateMap with state {}", key, value);
            return super.remove(key, value);
        }
    };
}
