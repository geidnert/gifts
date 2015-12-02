package com.solidparts.gifts.service;

import com.solidparts.gifts.dto.DataDTO;
import com.solidparts.gifts.dto.UserDTO;

import java.util.List;

/**
 * Created by geidnert on 26/11/15.
 */
public interface IUserService {
    public DataDTO getAppData() throws Exception;

    public List<UserDTO> getUsers(String group) throws Exception;

    public UserDTO getUser(String email, String password) throws Exception;

    public void addUser(UserDTO userDTO) throws Exception;

    public void updateUser(UserDTO userDTO) throws Exception;

    public void removeUser(UserDTO userDTO) throws Exception;

    public int syncToOnlineDB();

    public int syncFromOnlineDB();
}
