package com.solidparts.gifts.dao;

import com.solidparts.gifts.dto.UserDTO;

/**
 * Created by geidnert on 25/11/15.
 */
public interface IDataDAO {
    public UserDTO getAppData() throws Exception;
}
