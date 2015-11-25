package com.solidparts.gifts.dao;

import org.apache.http.NameValuePair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by geidnert on 25/11/15.
 */
public interface INetworkDAO {
    public String request(String action, ArrayList<NameValuePair> nameValuePairs) throws IOException;
}
