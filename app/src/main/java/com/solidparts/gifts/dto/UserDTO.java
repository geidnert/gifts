package com.solidparts.gifts.dto;

import java.util.Arrays;

/**
 * Created by geidnert on 25/11/15.
 */
public class UserDTO {
    private long cacheID;
    int userId;
    String email;
    String firstname;
    String lastname;

    public long getCacheID() {
        return cacheID;
    }

    public void setCacheID(long cacheID) {
        this.cacheID = cacheID;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDTO userDTO = (UserDTO) o;

        return userId == userDTO.userId;

    }

    @Override
    public int hashCode() {
        return userId;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "cacheID=" + cacheID +
                ", userId=" + userId +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                '}';
    }
}
