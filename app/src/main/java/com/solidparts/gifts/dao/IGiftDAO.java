package com.solidparts.gifts.dao;

import android.database.sqlite.SQLiteDatabase;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;

import java.util.List;

/**
 * Created by geidnert on 25/11/15.
 */
public interface IGiftDAO {
    public static final int DEFAULT = 1;
    public static final int ALL = 2;
    public static final String hostname = "solidparts.se";

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public List<GiftDTO> getGifts(int userId) throws Exception;

    public void addGift(UserDTO userDTO, GiftDTO giftDTO, int sync) throws Exception;

    public void updateGift(GiftDTO giftDTO, int sync) throws Exception;

    public void removeGiftById(int onlineId) throws Exception;

    public void removeGiftByCacheId(long cacheId) throws Exception;

    public List<GiftDTO> getNotSyncedAddedGifts() throws Exception;

    public List<GiftDTO> getNotSyncedRemovedGifts() throws Exception;

}
