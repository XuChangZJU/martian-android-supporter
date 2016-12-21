package com.martianLife.domain;

import com.facebook.react.bridge.ReadableMap;

/**
 * Created by Administrator on 2016/12/20.
 */
public class Token {
    private String accessToken;

    private String refreshToken;

    private Long expiredTime;

    private Boolean expired;

    private Long userId;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(Long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
