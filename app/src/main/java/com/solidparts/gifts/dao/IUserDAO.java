package com.solidparts.gifts.dao;

import android.database.sqlite.SQLiteDatabase;

import com.solidparts.gifts.dto.DataDTO;
import com.solidparts.gifts.dto.UserDTO;

import java.util.List;

/**
 * Created by geidnert on 25/11/15.
 */
public interface IUserDAO {
    public static final int DEFAULT = 1;
    public static final int ALL = 2;
    public static final String hostname = "solidparts.se";

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public DataDTO getAppData() throws Exception;

    public List<UserDTO> getUsers(int groupId) throws Exception;

    public UserDTO getUser(String email, String password) throws Exception;


    public void addUser(UserDTO userDTO, int sync) throws Exception;

    public void updateUser(UserDTO userDTO, int sync) throws Exception;

    public void removeUserById(int userId) throws Exception;

    public void removeUserByCacheId(long cacheId) throws Exception;

    public List<UserDTO> getNotSyncedAddedUsers() throws Exception;

    public List<UserDTO> getNotSyncedRemovedUsers() throws Exception;

}
