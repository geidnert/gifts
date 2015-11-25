package com.solidparts.gifts.dto;

/**
 * Created by geidnert on 25/11/15.
 */
public class DataDTO {
    private int appVersion;

    public int getLatestAppVersion() {
        return appVersion;
    }

    public void setLatestAppVersion(int appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String toString() {
        return "DataDto{" +
                "appVersion=" + appVersion +
                '}';
    }
}
