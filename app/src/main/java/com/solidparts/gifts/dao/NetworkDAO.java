package com.solidparts.gifts.dao;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by geidnert on 25/11/15.
 */
public class NetworkDAO implements INetworkDAO{
    public static final String ADD = "add.php";
    public static final String ADD_USER = "addUser.php";
    public static final String UPDATE = "update.php";
    public static final String UPDATE_USER = "updateUser.php";
    public static final String REMOVE = "remove.php";
    public static final String SEARCH = "get.php";
    public static final String GET_ALL_USERS = "getAllUsers.php";
    public static final String SEARCH_GIFT = "searchGift.php";
    public static final String APP_DATA = "getAppData.php";
    public static final String DATABASE_DEV = "gifts_dev";
    public static final String DATABASE_LIVE = "gifts";

    private static final String URL = "http://solidparts.se/gifts/";

    @Override
    public String request(String action, ArrayList<NameValuePair> nameValuePairs) throws IOException {
        String returnString;
        nameValuePairs.add(new BasicNameValuePair("database", DATABASE_LIVE));
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(URL + action);
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        returnString = httpClient.execute(httpPost, responseHandler);

        return returnString;
    }
}
