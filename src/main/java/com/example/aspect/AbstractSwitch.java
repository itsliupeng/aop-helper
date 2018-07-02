package com.example.aspect;

/**
 * Created by liupeng on 04/05/2017.
 */
public class AbstractSwitch implements Switch {
    private final String name;
    private boolean enable = false;

    public AbstractSwitch(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Switch on() {
        enable = true;
        return this;
    }

    @Override
    public Switch off() {
        enable = false;
        return this;
    }

    @Override
    public boolean isOn() {
        return enable == true;
    }

    @Override
    public boolean isOff() {
        return !isOn();
    }
}
