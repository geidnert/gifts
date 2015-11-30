package com.solidparts.gifts.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.Toast;

import com.solidparts.gifts.dao.IUserDAO;
import com.solidparts.gifts.dao.OfflineUserDAO;
import com.solidparts.gifts.dao.OnlineUserDAO;
import com.solidparts.gifts.dto.DataDTO;
import com.solidparts.gifts.dto.UserDTO;

import java.util.List;

/**
 * Created by geidnert on 26/11/15.
 */
public class UserService implements IUserService {
    IUserDAO onlineUserDAO;
    IUserDAO offlineUserDAO;
    Context context;

    public UserService(Context context) {
        onlineUserDAO = new OnlineUserDAO(context);
        offlineUserDAO = new OfflineUserDAO(context);
        this.context = context;
    }

    @Override
    public DataDTO getAppData() throws Exception {
        DataDTO appData = null;

        try {
            appData = onlineUserDAO.getAppData();
        } catch (Exception e) {

        }
        return appData;
    }

    @Override
    public UserDTO getUser(String email, String password) throws Exception {
        UserDTO user = null;

        try {
            user = onlineUserDAO.getUser(email, password);
            //users = offlineUserDAO.getItems(searchTerm, searchType);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                user = offlineUserDAO.getUser(email, password);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return user;
    }

    @Override
    public List<UserDTO> getUsers(int groupId) throws Exception {
        List<UserDTO> users = null;

        try {
            users = onlineUserDAO.getUsers(groupId);
            //users = offlineUserDAO.getItems(searchTerm, searchType);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                users = offlineUserDAO.getUsers(groupId);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return users;
    }

    @Override
    public void addUser(UserDTO userDTO) {
        try {
            onlineUserDAO.addUser(userDTO, 0);
            //offlineUserDAO.addItem(userDTO, 0);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                offlineUserDAO.addUser(userDTO, 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void updateUser(UserDTO userDTO) throws Exception {
        try {
            onlineUserDAO.updateUser(userDTO, 1);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                offlineUserDAO.updateUser(userDTO, 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void removeUser(UserDTO userDTO) {
        boolean success = false;
        boolean foundNotSyncedIem = false;

        try {
            onlineUserDAO.removeUserById(userDTO.getId());
        } catch (Exception e) {
            // No network, use offline mode
            try {
                //offlineUserDAO.removeItemByOnlineId(onlineId);
                List<UserDTO> notSyncedAddedItems = offlineUserDAO.getNotSyncedAddedUsers();

                for (UserDTO notSynceditem : notSyncedAddedItems) {
                    if (notSynceditem.getCacheID() == userDTO.getCacheID()) {
                        offlineUserDAO.removeUserByCacheId(userDTO.getCacheID());
                        foundNotSyncedIem = true;
                    }
                }

                // mark for deletion
                if (!foundNotSyncedIem) {
                    offlineUserDAO.updateUser(userDTO, 2);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    public int syncToOnlineDB() {
        // get all items that are not synced from local db
        List<UserDTO> notSyncedAddedItems = null;
        List<UserDTO> notSyncedRemovedItems = null;
        try {
            notSyncedAddedItems = offlineUserDAO.getNotSyncedAddedUsers();
            notSyncedRemovedItems = offlineUserDAO.getNotSyncedRemovedUsers();
        } catch (Exception e1) {
            e1.printStackTrace();
            return -1;
        }

        // Sync added items to online db if available
        if (notSyncedAddedItems != null && notSyncedAddedItems.size() > 0) {

            for (UserDTO userDTO : notSyncedAddedItems) {
                try {
                    onlineUserDAO.addUser(userDTO, 1); // add not synced item to online db
                    offlineUserDAO.updateUser(userDTO, 1); // mark item as synced in local db
                } catch (Exception e) {
                    // No network, can not sync
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        // Sync removed items to online db if available
        if (notSyncedRemovedItems != null && notSyncedRemovedItems.size() > 0) {

            for (UserDTO userDTO : notSyncedRemovedItems) {
                try {
                    onlineUserDAO.removeUserById(userDTO.getId()); // add not synced item to online db
                    offlineUserDAO.removeUserById(userDTO.getId()); // mark item as synced in local db
                } catch (Exception e) {
                    // No network, can not sync
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        return 2;
    }

    @Override
    public int syncFromOnlineDB() {
        if (!isNetworkAvaliable(context)) {
            return -1;
        }

        // get all items that are not synced from local db
        List<UserDTO> onlineUsers = null;
        List<UserDTO> localUsers = null;
        try {
            //onlineUsers = onlineUserDAO.getUsers("all", OfflineUserDAO.DEFAULT);
            //localUsers = offlineUserDAO.getUsers("all", OfflineUserDAO.ALL);
        } catch (Exception e1) {
            e1.printStackTrace();
            return -1;
        }

        // Sync to offline db
        if (onlineUsers != null && onlineUsers.size() > 0) {
            for (UserDTO onlineUserDto : onlineUsers) {
                boolean foundItem = false;
                try {
                    // check if online db item allready is defined in local db
                    for (UserDTO localUserDTO : localUsers) {
                        if (localUserDTO.equals(onlineUserDto)) {
                            foundItem = true;
                        }
                    }

                    // Did not find the item in the local storage, add it
                    if (!foundItem) {
                        offlineUserDAO.addUser(onlineUserDto, 1); // mark item as synced in local db
                    }
                } catch (Exception e) {
                    // No network, can not sync
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        return 1;
    }

    public static boolean isNetworkAvaliable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)
                || (connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }

    private void showMessage(String message, boolean goBack) {
        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();
    }


}
