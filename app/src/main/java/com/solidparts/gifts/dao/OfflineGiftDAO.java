package com.solidparts.gifts.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solidparts.gifts.dto.GiftDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 25/11/15.
 */
public class OfflineGiftDAO extends SQLiteOpenHelper implements IGiftDAO {
    public static final String GIFT = "gift";
    public static final String CACHE_ID = "cache_id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String LOCATION = "location";
    public static final String ONLINEID = "onlineid";
    public static final String IMAGE = "image";
    public static final String COUNT = "count";
    public static final String QRCODE = "qrcode";
    public static final String SYNCED = "synced";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";

    public OfflineGiftDAO(Context context) {
        super(context, "gifts.db", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createItems = "CREATE TABLE " + GIFT + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ONLINEID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + COUNT + " INTEGER, " + IMAGE +
                " BLOB, " + QRCODE + " TEXT, " + LOCATION + " TEXT, " + SYNCED + " INTEGER, " + LONGITUDE + " DOUBLE, " +
                LATITUDE + " DOUBLE );";

        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GIFT);

        String createItems = "CREATE TABLE " + GIFT + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ONLINEID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + COUNT + " INTEGER, " + IMAGE +
                " BLOB, " + QRCODE + " TEXT, " + LOCATION + " TEXT, " + SYNCED + " INTEGER, " + LONGITUDE + " DOUBLE, " +
                LATITUDE + " DOUBLE );";

        db.execSQL(createItems);
    }

    @Override
    public List<GiftDTO> getGifts(int userId) throws IOException, JSONException {

        String query = "Select * FROM " + GIFT + " WHERE " + NAME + " LIKE  \"%" + userId + "%\"" + " OR " + LOCATION + " LIKE \"%" + userId + "%\" AND " + SYNCED + " < 2";

        // Search all in a location
        //if (searchType == ALL) {
        //    query = "Select * FROM " + GIFT;
        //}
        List<GiftDTO> searchResultList = getGiftDTOs(query);
        return searchResultList;
    }

    public List<GiftDTO> getNotSyncedAddedGifts() throws Exception {
        String query = "Select * FROM " + GIFT + " WHERE " + SYNCED + " = 0";
        List<GiftDTO> searchResultList = getGiftDTOs(query);
        return searchResultList;
    }

    public List<GiftDTO> getNotSyncedRemovedGifts() throws Exception {
        String query = "Select * FROM " + GIFT + " WHERE " + SYNCED + " = 2";
        List<GiftDTO> searchResultList = getGiftDTOs(query);
        return searchResultList;
    }

    private List<GiftDTO> getGiftDTOs(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<GiftDTO> searchResultList = new ArrayList<>();
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            GiftDTO giftDTO = new GiftDTO();

            giftDTO.setCacheID(cursor.getInt(0));
            giftDTO.setId(cursor.getInt(1));
            giftDTO.setName(cursor.getString(2));
            giftDTO.setDescription(cursor.getString(3));

            searchResultList.add(giftDTO);
            cursor.moveToNext();
        }

        db.close();
        return searchResultList;
    }

    @Override
    public void addGift(GiftDTO itemDTO, int sync) throws IOException, JSONException {
        ContentValues cv = new ContentValues();

        cv.put(ONLINEID, itemDTO.getId());
        cv.put(NAME, itemDTO.getName());
        cv.put(DESCRIPTION, itemDTO.getDescription());

        long cachceId = getWritableDatabase().insert(GIFT, null, cv);
        itemDTO.setCacheID(cachceId);
    }

    @Override
    public void updateGift(GiftDTO giftDTODTO, int sync) {
        System.out.println("values: " + giftDTODTO.toString());
        ContentValues cv = new ContentValues();

        cv.put(ONLINEID, giftDTODTO.getId());
        cv.put(NAME, giftDTODTO.getName());
        cv.put(DESCRIPTION, giftDTODTO.getDescription());

        String where = "onlineid=?";

        String[] whereArgs = {Long.toString(giftDTODTO.getId())};
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(GIFT, cv, where, whereArgs);

    }

    @Override
    public void removeGiftById(int onlineId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(GIFT, ONLINEID + "=" + onlineId, null);
        db.close();
    }

    @Override
    public void removeGiftByCacheId(long cacheId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(GIFT, CACHE_ID + "=" + cacheId, null);
        db.close();
    }
}
