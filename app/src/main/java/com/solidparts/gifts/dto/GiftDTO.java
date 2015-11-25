package com.solidparts.gifts.dto;

import java.util.Arrays;

/**
 * Created by geidnert on 25/11/15.
 */
public class GiftDTO {
    private long cacheID;
    int onlineid;
    String name;
    String description;
    boolean bought;

    public long getCacheID() {
        return cacheID;
    }

    public void setCacheID(long cacheID) {
        this.cacheID = cacheID;
    }

    public int getOnlineid() {
        return onlineid;
    }

    public void setOnlineid(int onlineid) {
        this.onlineid = onlineid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GiftDTO giftDTO = (GiftDTO) o;

        return onlineid == giftDTO.onlineid;

    }

    @Override
    public int hashCode() {
        return onlineid;
    }

    @Override
    public String toString() {
        return "GiftDTO{" +
                "cacheID=" + cacheID +
                ", onlineid=" + onlineid +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", bought=" + bought +
                '}';
    }
}
