package com.solidparts.gifts.dto;

import java.util.Arrays;

/**
 * Created by geidnert on 25/11/15.
 */
public class GiftDTO {
    private long cacheID;
    int id;
    int userId;
    String name;
    String description;
    String url;
    byte[] image;
    boolean bought;

    public long getCacheID() {
        return cacheID;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setCacheID(long cacheID) {
        this.cacheID = cacheID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GiftDTO giftDTO = (GiftDTO) o;

        return id == giftDTO.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "GiftDTO{" +
                "cacheID=" + cacheID +
                ", id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", image=" + Arrays.toString(image) +
                ", bought=" + bought +
                '}';
    }

}
