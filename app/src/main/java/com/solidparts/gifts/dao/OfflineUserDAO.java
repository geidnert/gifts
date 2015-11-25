package com.solidparts.gifts.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solidparts.gifts.dto.UserDTO;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 25/11/15.
 */
public class OfflineUserDAO extends SQLiteOpenHelper implements IUserDAO {
    public static final String USER = "user";
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

    public OfflineUserDAO(Context context) {
        super(context, "gifts.db", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createItems = "CREATE TABLE " + ITEM + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ONLINEID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + COUNT + " INTEGER, " + IMAGE +
                " BLOB, " + QRCODE + " TEXT, " + LOCATION + " TEXT, " + SYNCED + " INTEGER, " + LONGITUDE + " DOUBLE, " +
                LATITUDE + " DOUBLE );";

        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ITEM);

        String createItems = "CREATE TABLE " + ITEM + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ONLINEID + " INTEGER, " + NAME + " TEXT, " + DESCRIPTION + " TEXT, " + COUNT + " INTEGER, " + IMAGE +
                " BLOB, " + QRCODE + " TEXT, " + LOCATION + " TEXT, " + SYNCED + " INTEGER, " + LONGITUDE + " DOUBLE, " +
                LATITUDE + " DOUBLE );";

        db.execSQL(createItems);
    }

    @Override
    public UserDTO getAppData() throws Exception {
        return null;
    }

    @Override
    public List<UserDTO> getItems(String searchTerm, int searchType) throws IOException, JSONException {

        String query = "Select * FROM " + ITEM + " WHERE " + NAME + " LIKE  \"%" + searchTerm + "%\"" + " OR " + LOCATION + " LIKE \"%" + searchTerm + "%\" AND " + SYNCED + " < 2";

        // Search all in a location
        if (searchType == ALL) {
            query = "Select * FROM " + ITEM;
        }
        List<UserDTO> searchResultList = getItemDTOs(query);
        return searchResultList;
    }

    public List<UserDTO> getNotSyncedAddedUsers() throws Exception {
        String query = "Select * FROM " + ITEM + " WHERE " + SYNCED + " = 0";
        List<UserDTO> searchResultList = getUserDTOs(query);
        return searchResultList;
    }

    public List<UserDTO> getNotSyncedRemovedUsers() throws Exception {
        String query = "Select * FROM " + ITEM + " WHERE " + SYNCED + " = 2";
        List<UserDTO> searchResultList = getUserDTOs(query);
        return searchResultList;
    }

    private List<UserDTO> getUserDTOs(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        List<UserDTO> searchResultList = new ArrayList<>();
        cursor.moveToFirst();

        while (cursor.isAfterLast() == false) {
            UserDTO userDTO = new UserDTO();

            userDTO.setCacheID(cursor.getInt(0));
            userDTO.setOnlineid(cursor.getInt(1));
            userDTO.setName(cursor.getString(2));
            userDTO.setDescription(cursor.getString(3));
            userDTO.setCount(cursor.getInt(4));
            userDTO.setImage(cursor.getBlob(5));
            userDTO.setQrCode(cursor.getBlob(6));
            userDTO.setLocation(cursor.getString(7));
            userDTO.setLongitude(cursor.getDouble(9));
            userDTO.setLatitude(cursor.getDouble(10));



            searchResultList.add(userDTO);
            cursor.moveToNext();
        }

        db.close();
        return searchResultList;
    }

    @Override
    public void addUser(UserDTO userDTO, int sync) throws IOException, JSONException {
        ContentValues cv = new ContentValues();

        cv.put(ONLINEID, userDTO.getOnlineid());
        cv.put(NAME, userDTO.getName());
        cv.put(DESCRIPTION, userDTO.getDescription());
        cv.put(COUNT, userDTO.getCount());
        cv.put(LOCATION, userDTO.getLocation());
        cv.put(IMAGE, userDTO.getImage());
        cv.put(QRCODE, userDTO.getQrCode());
        cv.put(SYNCED, sync);
        cv.put(LONGITUDE, userDTO.getLongitude());
        cv.put(LATITUDE, userDTO.getLatitude());

        long cachceId = getWritableDatabase().insert(ITEM, null, cv);
        userDTO.setCacheID(cachceId);
    }

    @Override
    public void updateUser(UserDTO userDTO, int sync) {
        System.out.println("values: " + userDTO.toString());
        ContentValues cv = new ContentValues();

        cv.put(ONLINEID, userDTO.getOnlineid());
        cv.put(NAME, userDTO.getName());
        cv.put(DESCRIPTION, userDTO.getDescription());
        cv.put(COUNT, userDTO.getCount());
        cv.put(LOCATION, userDTO.getLocation());
        cv.put(IMAGE, userDTO.getImage());
        cv.put(QRCODE, userDTO.getQrCode());
        cv.put(SYNCED, sync);
        cv.put(LONGITUDE, userDTO.getLongitude());
        cv.put(LATITUDE, userDTO.getLatitude());

        String where = "onlineid=?";

        String[] whereArgs = {Long.toString(userDTO.getOnlineid())};
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(ITEM, cv, where, whereArgs);

    }

    @Override
    public void removeUserByOnlineId(int onlineId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ITEM, ONLINEID + "=" + onlineId, null);
        db.close();
    }

    @Override
    public void removeUserByCacheId(long cacheId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ITEM, CACHE_ID + "=" + cacheId, null);
        db.close();
    }
}
