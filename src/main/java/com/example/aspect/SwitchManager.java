package com.example.aspect;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liupeng on 04/05/2017.
 */
public class SwitchManager {
    private static final ConcurrentHashMap<String, Switch> switchMaps = new ConcurrentHashMap<>();
    public static Switch register(Switch s) {
        if (!switchMaps.contains(s.name())) {
            switchMaps.put(s.name(), s);
        } else {
            throw new RuntimeException("SwitchManager#register " + s + " has existed, please use another unique name." );
        }
        return s;
    }

    public static Switch getSwitch(String name) {
        if (switchMaps.contains(name)) {
            return switchMaps.get(name);
        } else {
            return null;
        }
    }
}
