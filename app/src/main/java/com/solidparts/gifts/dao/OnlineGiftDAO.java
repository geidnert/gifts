package com.solidparts.gifts.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.solidparts.gifts.dto.DataDTO;
import com.solidparts.gifts.dto.GiftDTO;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 25/11/15.
 */
public class OnlineGiftDAO implements IGiftDAO {
    private final NetworkDAO networkDAO;
    private final OfflineGiftDAO offlineGiftDAO;
    private final Context context;

    public OnlineGiftDAO(Context context) {
        networkDAO = new NetworkDAO();
        offlineGiftDAO = new OfflineGiftDAO(context);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /*@Override
    public DataDTO getAppData() throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String request = networkDAO.request(NetworkDAO.APP_DATA, nameValuePairs);
        JSONObject root = new JSONObject(request);

        int appVersion = root.getJSONObject("appdata").getInt("version");
        DataDTO dataDTO = new DataDTO();
        dataDTO.setLatestAppVersion(appVersion);

        return dataDTO;
    }*/

    @Override
    public List<GiftDTO> getGifts(int userId) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("userId", "" + userId));

        String request = networkDAO.request(NetworkDAO.SEARCH_GIFT, nameValuePairs);

        List<GiftDTO> allItems = new ArrayList<GiftDTO>();
        JSONObject root = new JSONObject(request);
        JSONArray items = root.getJSONArray("gifts");

        for (int i = 0; i < items.length(); i++) {
            JSONObject jsonItem = items.getJSONObject(i).getJSONObject("gift");

            int id = jsonItem.getInt("id");
            int uuserId = jsonItem.getInt("userId");
            String name = jsonItem.getString("name");
            String description = jsonItem.getString("description");
            String url = jsonItem.getString("url");
            boolean bought = jsonItem.getInt("bought") == 1;

            byte[] image = Base64.decode(jsonItem.get("image").toString(), Base64.DEFAULT);

            GiftDTO giftDTO = new GiftDTO();
            giftDTO.setId(id);
            giftDTO.setUserId(uuserId);
            giftDTO.setName(name);
            giftDTO.setDescription(description);
            giftDTO.setBought(bought);
            giftDTO.setImage(image);
            giftDTO.setUrl(url);

            allItems.add(giftDTO);
        }

        return allItems;
    }

    @Override
    public void addGift(GiftDTO giftDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        //nameValuePairs.add(new BasicNameValuePair("name", giftDTO.getName()));
        nameValuePairs.add(new BasicNameValuePair("description", giftDTO.getDescription()));
        nameValuePairs.add(new BasicNameValuePair("userId", ""+giftDTO.getUserId()));
        nameValuePairs.add(new BasicNameValuePair("url", giftDTO.getUrl()));
        nameValuePairs.add(new BasicNameValuePair("image", Base64.encodeToString(giftDTO.getImage(), Base64.DEFAULT)));


        String request = networkDAO.request(NetworkDAO.ADD, nameValuePairs);

        giftDTO.setId(Integer.parseInt(request.trim()));
        offlineGiftDAO.updateGift(giftDTO, sync);

        // Also save to local database if its not a sync operation
        if (sync == 0) {
            offlineGiftDAO.addGift(giftDTO, 1);
        }

    }

    @Override
    public void updateGift(GiftDTO giftDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("onlineid", giftDTO.getId() + ""));
        nameValuePairs.add(new BasicNameValuePair("name", giftDTO.getName()));
        nameValuePairs.add(new BasicNameValuePair("description", giftDTO.getDescription()));


        networkDAO.request(NetworkDAO.UPDATE, nameValuePairs);

        // Also save to local database if its not a sync operation
        //if (sync == 0) {
        offlineGiftDAO.updateGift(giftDTO, 1);
        //}
    }


    @Override
    public void removeGiftById(int id) throws Exception {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("onlineid", id + ""));
        networkDAO.request(NetworkDAO.REMOVE, nameValuePairs);

        // Also save to local database if its not a sync operation
        offlineGiftDAO.removeGiftById(id);
    }

    @Override
    public void removeGiftByCacheId(long cacheId) throws Exception {

    }

    @Override
    public List<GiftDTO> getNotSyncedAddedGifts() throws Exception {
        return null;
    }

    @Override
    public List<GiftDTO> getNotSyncedRemovedGifts() throws Exception {
        return null;
    }
}
