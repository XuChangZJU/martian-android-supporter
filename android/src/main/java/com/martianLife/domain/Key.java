package com.martianLife.domain;

import com.facebook.react.bridge.ReadableMap;

import java.util.Date;

/**
 * Created by Administrator on 2016/12/19.
 */
public class Key {
    private Long id;

    private Long expiredTime;

    private Lock lock;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public Key() {

    }
}
