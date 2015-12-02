package com.solidparts.gifts.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.solidparts.gifts.dto.DataDTO;
import com.solidparts.gifts.dto.UserDTO;

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
public class OnlineUserDAO implements IUserDAO {
    private final NetworkDAO networkDAO;
    private final OfflineUserDAO offlineUserDAO;
    private final Context context;

    public OnlineUserDAO(Context context) {
        networkDAO = new NetworkDAO();
        offlineUserDAO = new OfflineUserDAO(context);
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
    public List<UserDTO> getUsers(String g) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("groupname", ""+g));

        String request = networkDAO.request(NetworkDAO.GET_ALL_USERS, nameValuePairs);

        List<UserDTO> allUsers = new ArrayList<UserDTO>();
        JSONObject root = new JSONObject(request);
        JSONArray items = root.getJSONArray("users");

        for (int i = 0; i < items.length(); i++) {
            JSONObject jsonUser = items.getJSONObject(i).getJSONObject("user");

            int id = jsonUser.getInt("id");
            String firstname = jsonUser.getString("firstname");
            String lastname = jsonUser.getString("lastname");
            String email = jsonUser.getString("email");
            String groupname = jsonUser.getString("groupname");


            //byte[] image = Base64.decode(jsonUser.get("image").toString(), Base64.DEFAULT);


            UserDTO userDTO = new UserDTO();
            userDTO.setId(id);
            userDTO.setEmail(email);
            userDTO.setFirstname(firstname);
            userDTO.setLastname(lastname);
            userDTO.setGroupName(groupname);

            allUsers.add(userDTO);
        }

        return allUsers;
    }

    @Override
    public UserDTO getUser(String userEmail, String userPassword) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("email", userEmail));
        nameValuePairs.add(new BasicNameValuePair("password", userPassword));

        String request = networkDAO.request(NetworkDAO.SEARCH, nameValuePairs);

        List<UserDTO> allUsers = new ArrayList<UserDTO>();
        JSONObject root = new JSONObject(request);
        JSONArray items = root.getJSONArray("users");

        for (int i = 0; i < items.length(); i++) {
            JSONObject jsonUser = items.getJSONObject(i).getJSONObject("user");

            int id = jsonUser.getInt("id");
            String firstname = jsonUser.getString("firstname");
            String lastname = jsonUser.getString("lastname");
            String email = jsonUser.getString("email");
            String groupname = jsonUser.getString("groupname");
            String password = jsonUser.getString("password");


            //byte[] image = Base64.decode(jsonUser.get("image").toString(), Base64.DEFAULT);


            UserDTO userDTO = new UserDTO();
            userDTO.setId(id);
            userDTO.setEmail(email);
            userDTO.setFirstname(firstname);
            userDTO.setLastname(lastname);
            userDTO.setGroupName(groupname);
            userDTO.setPassword(password);

            allUsers.add(userDTO);
        }

        return allUsers.get(0);
    }

    @Override
    public void addUser(UserDTO userDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("email", userDTO.getEmail()));
        nameValuePairs.add(new BasicNameValuePair("password", userDTO.getPassword()));
        nameValuePairs.add(new BasicNameValuePair("firstname", userDTO.getFirstname()));
        nameValuePairs.add(new BasicNameValuePair("lastname", userDTO.getLastname() + ""));
        nameValuePairs.add(new BasicNameValuePair("groupname", userDTO.getGroupName()));
        //nameValuePairs.add(new BasicNameValuePair("image", Base64.encodeToString(userDTO.getImage(), Base64.DEFAULT)));

        String request = networkDAO.request(NetworkDAO.ADD_USER, nameValuePairs);

        userDTO.setId(Integer.parseInt(request.trim()));
        offlineUserDAO.updateUser(userDTO, sync);

        // Also save to local database if its not a sync operation
        if (sync == 0) {
            offlineUserDAO.addUser(userDTO, 1);
        }

    }

    @Override
    public void updateUser(UserDTO userDTO, int sync) throws IOException, JSONException {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("email", userDTO.getEmail()));
        nameValuePairs.add(new BasicNameValuePair("firstname", userDTO.getFirstname()));
        nameValuePairs.add(new BasicNameValuePair("lastname", userDTO.getLastname() + ""));
        nameValuePairs.add(new BasicNameValuePair("groupname", userDTO.getGroupName()));
        //nameValuePairs.add(new BasicNameValuePair("image", Base64.encodeToString(userDTO.getImage(), Base64.DEFAULT)));

        networkDAO.request(NetworkDAO.UPDATE, nameValuePairs);

        // Also save to local database if its not a sync operation
        //if (sync == 0) {
        offlineUserDAO.updateUser(userDTO, 1);
        //}
    }


    @Override
    public void removeUserById(int id) throws Exception {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("onlineid", id + ""));
        networkDAO.request(NetworkDAO.REMOVE, nameValuePairs);

        // Also save to local database if its not a sync operation
        offlineUserDAO.removeUserById(id);
    }

    @Override
    public void removeUserByCacheId(long cacheId) throws Exception {

    }

    @Override
    public List<UserDTO> getNotSyncedAddedUsers() throws Exception {
        return null;
    }

    @Override
    public List<UserDTO> getNotSyncedRemovedUsers() throws Exception {
        return null;
    }
}
