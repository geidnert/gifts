package com.solidparts.gifts.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.solidparts.gifts.dto.UserDTO;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 25/11/15.
 */
public class OfflineUserDAO extends SQLiteOpenHelper implements IUserDAO {
    public static final String USER = "user";
    public static final String ID = "id";
    public static final String CACHE_ID = "cache_id";
    public static final String FAMILY = "family";
    public static final String EMAIL = "email";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String SYNCED = "synced";

    public OfflineUserDAO(Context context) {
        super(context, "gifts.db", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createItems = "CREATE TABLE " + USER + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ID + " INTEGER, " + EMAIL + " TEXT, " + FIRSTNAME + " TEXT, " + LASTNAME + " TEXT );";

        db.execSQL(createItems);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER);

        String createItems = "CREATE TABLE " + USER + " ( " + CACHE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ID + " INTEGER, " + EMAIL + " TEXT, " + FIRSTNAME + " TEXT, " + LASTNAME + " TEXT );";

        db.execSQL(createItems);
    }

    @Override
    public List<UserDTO> getUsers(String searchTerm, int searchType) throws IOException, JSONException {

        String query = "Select * FROM " + USER + " WHERE " + FAMILY + " LIKE  \"%" + searchTerm + "%\" AND " + SYNCED + " < 2";

        // Search all in a location
        if (searchType == ALL) {
            query = "Select * FROM " + USER;
        }
        List<UserDTO> searchResultList = getUserDTOs(query);
        return searchResultList;
    }

    public List<UserDTO> getNotSyncedAddedUsers() throws Exception {
        String query = "Select * FROM " + USER + " WHERE " + SYNCED + " = 0";
        List<UserDTO> searchResultList = getUserDTOs(query);
        return searchResultList;
    }

    public List<UserDTO> getNotSyncedRemovedUsers() throws Exception {
        String query = "Select * FROM " + USER + " WHERE " + SYNCED + " = 2";
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

            searchResultList.add(userDTO);
            cursor.moveToNext();
        }

        db.close();
        return searchResultList;
    }

    @Override
    public void addUser(UserDTO userDTO, int sync) throws IOException, JSONException {
        ContentValues cv = new ContentValues();

        cv.put(ID, userDTO.getId());
        cv.put(EMAIL, userDTO.getEmail());
        cv.put(FIRSTNAME, userDTO.getFirstname());
        cv.put(LASTNAME, userDTO.getLastname());


        cv.put(IMAGE, userDTO.getImage());
        cv.put(QRCODE, userDTO.getQrCode());
        cv.put(SYNCED, sync);
        cv.put(LONGITUDE, userDTO.getLongitude());
        cv.put(LATITUDE, userDTO.getLatitude());

        long cachceId = getWritableDatabase().insert(USER, null, cv);
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
        db.update(USER, cv, where, whereArgs);

    }

    @Override
    public void removeUserByOnlineId(int onlineId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USER, ONLINEID + "=" + onlineId, null);
        db.close();
    }

    @Override
    public void removeUserByCacheId(long cacheId) throws Exception {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USER, CACHE_ID + "=" + cacheId, null);
        db.close();
    }
}
