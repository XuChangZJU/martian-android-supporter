package com.martianLife.domain;

import com.facebook.react.bridge.ReadableMap;
import com.martianLife.utils.DataUtils;

import java.util.Date;

/**
 * Created by Administrator on 2016/12/19.
 */
public class Lock {
    private byte[] constantKeyWord;

    private Long ckwExpiredTime;

    private Long id;

    private String address;

    public byte[] getConstantKeyWord() {
        return constantKeyWord;
    }

    public void setConstantKeyWord(byte[] constantKeyWord) {
        this.constantKeyWord = constantKeyWord;
    }

    public Long getCkwExpiredTime() {
        return ckwExpiredTime;
    }

    public void setCkwExpiredTime(Long ckwExpiredTime) {
        this.ckwExpiredTime = ckwExpiredTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Lock() {
    }
}
