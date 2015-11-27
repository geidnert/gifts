package com.solidparts.gifts.dto;

import java.io.Serializable;

/**
 * Created by geidnert on 25/11/15.
 */
public class UserDTO implements Serializable {
    private long cacheID;
    int id;
    String email;
    String firstname;
    String lastname;
    String group;

    public long getCacheID() {
        return cacheID;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDTO userDTO = (UserDTO) o;

        return id == userDTO.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "cacheID=" + cacheID +
                ", id=" + id +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
