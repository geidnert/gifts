package com.solidparts.gifts.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

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
public class OnlineGiftDAO {
    private final NetworkDAO networkDAO;
    private final OfflineItemDAO offlineItemDAO;
    private final Context context;

    public OnlineItemDAO(Context context) {
        networkDAO = new NetworkDAO();
        offlineItemDAO = new OfflineItemDAO(context);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public DataDTO getAppData() throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String request = networkDAO.request(NetworkDAO.APP_DATA, nameValuePairs);
        JSONObject root = new JSONObject(request);

        int appVersion = root.getJSONObject("appdata").getInt("version");
        DataDTO dataDTO = new DataDTO();
        dataDTO.setLatestAppVersion(appVersion);

        return dataDTO;
    }

    @Override
    public List<ItemDTO> getItems(String searchTerm, int searchType) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("searchterm", searchTerm));

        String request = networkDAO.request(NetworkDAO.SEARCH, nameValuePairs);

        List<ItemDTO> allItems = new ArrayList<ItemDTO>();
        JSONObject root = new JSONObject(request);
        JSONArray items = root.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject jsonItem = items.getJSONObject(i).getJSONObject("item");

            int id = jsonItem.getInt("id");
            int count = jsonItem.getInt("count");
            String name = jsonItem.getString("name");
            String description = jsonItem.getString("description");
            String location = jsonItem.getString("location");
            double longitude = jsonItem.getDouble("longitude");
            double latitude = jsonItem.getDouble("latitude");

            byte[] image = Base64.decode(jsonItem.get("image").toString(), Base64.DEFAULT);
            byte[] qrCode = Base64.decode(jsonItem.get("qrcode").toString(), Base64.DEFAULT);

            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setOnlineid(id);
            itemDTO.setCount(count);
            itemDTO.setName(name);
            itemDTO.setDescription(description);
            itemDTO.setLocation(location);
            itemDTO.setImage(image);
            itemDTO.setQrCode(qrCode);
            itemDTO.setLongitude(longitude);
            itemDTO.setLatitude(latitude);

            allItems.add(itemDTO);
        }

        return allItems;
    }

    @Override
    public void addItem(ItemDTO itemDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("name", itemDTO.getName()));
        nameValuePairs.add(new BasicNameValuePair("description", itemDTO.getDescription()));
        nameValuePairs.add(new BasicNameValuePair("count", itemDTO.getCount() + ""));
        nameValuePairs.add(new BasicNameValuePair("location", itemDTO.getLocation()));
        nameValuePairs.add(new BasicNameValuePair("image", Base64.encodeToString(itemDTO.getImage(), Base64.DEFAULT)));
        nameValuePairs.add(new BasicNameValuePair("qrcode", Base64.encodeToString(itemDTO.getQrCode(), Base64.DEFAULT)));
        nameValuePairs.add(new BasicNameValuePair("longitude", itemDTO.getLongitude() + ""));
        nameValuePairs.add(new BasicNameValuePair("latitude", itemDTO.getLatitude() + ""));

        String request = networkDAO.request(NetworkDAO.ADD, nameValuePairs);

        itemDTO.setOnlineid(Integer.parseInt(request.trim()));
        offlineItemDAO.updateItem(itemDTO, sync);

        // Also save to local database if its not a sync operation
        if (sync == 0) {
            offlineItemDAO.addItem(itemDTO, 1);
        }

    }

    @Override
    public void updateItem(ItemDTO itemDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("onlineid", itemDTO.getOnlineid() + ""));
        nameValuePairs.add(new BasicNameValuePair("name", itemDTO.getName()));
        nameValuePairs.add(new BasicNameValuePair("description", itemDTO.getDescription()));
        nameValuePairs.add(new BasicNameValuePair("count", itemDTO.getCount() + ""));
        nameValuePairs.add(new BasicNameValuePair("location", itemDTO.getLocation()));
        nameValuePairs.add(new BasicNameValuePair("image", Base64.encodeToString(itemDTO.getImage(), Base64.DEFAULT)));
        nameValuePairs.add(new BasicNameValuePair("qrcode", Base64.encodeToString(itemDTO.getQrCode(), Base64.DEFAULT)));
        nameValuePairs.add(new BasicNameValuePair("longitude", itemDTO.getLongitude() + ""));
        nameValuePairs.add(new BasicNameValuePair("latitude", itemDTO.getLatitude() + ""));

        networkDAO.request(NetworkDAO.UPDATE, nameValuePairs);

        // Also save to local database if its not a sync operation
        //if (sync == 0) {
        offlineItemDAO.updateItem(itemDTO, 1);
        //}
    }


    @Override
    public void removeItemByOnlineId(int onlineId) throws Exception {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("onlineid", onlineId + ""));
        networkDAO.request(NetworkDAO.REMOVE, nameValuePairs);

        // Also save to local database if its not a sync operation
        offlineItemDAO.removeItemByOnlineId(onlineId);
    }

    @Override
    public void removeItemByCacheId(long cacheId) throws Exception {

    }

    @Override
    public List<ItemDTO> getNotSyncedAddedItems() throws Exception {
        return null;
    }

    @Override
    public List<ItemDTO> getNotSyncedRemovedItems() throws Exception {
        return null;
    }
}
