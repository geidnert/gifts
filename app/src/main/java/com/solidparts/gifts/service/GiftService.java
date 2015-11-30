package com.solidparts.gifts.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.Toast;

import com.solidparts.gifts.GiftAdapter;
import com.solidparts.gifts.dao.IGiftDAO;
import com.solidparts.gifts.dao.OfflineGiftDAO;
import com.solidparts.gifts.dao.OnlineGiftDAO;
import com.solidparts.gifts.dto.GiftDTO;

import java.util.List;

/**
 * Created by geidnert on 26/11/15.
 */
public class GiftService implements IGiftService {
    IGiftDAO onlineGiftDAO;
    IGiftDAO offlineGiftDAO;
    Context context;

    public GiftService(Context context) {
        onlineGiftDAO = new OnlineGiftDAO(context);
        offlineGiftDAO = new OfflineGiftDAO(context);
        this.context = context;
    }


    @Override
    public List<GiftDTO> getGifts(int userId) throws Exception {
        List<GiftDTO> gifts = null;

        try {
            gifts = onlineGiftDAO.getGifts(userId);
            //items = offlineGiftDAO.getItems(searchTerm, searchType);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                gifts = offlineGiftDAO.getGifts(userId);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return gifts;
    }

    @Override
    public void addGift(GiftDTO giftDTO) {
        try {
            onlineGiftDAO.addGift(giftDTO, 0);
            //offlineGiftDAO.addItem(giftDTO, 0);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                offlineGiftDAO.addGift(giftDTO, 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void updateGift(GiftDTO giftDTO) throws Exception {
        try {
            onlineGiftDAO.updateGift(giftDTO, 1);
        } catch (Exception e) {
            // No network, use offline mode
            try {
                offlineGiftDAO.updateGift(giftDTO, 0);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void removeGift(GiftDTO giftDTO) {
        boolean success = false;
        boolean foundNotSyncedIem = false;

        try {
            onlineGiftDAO.removeGiftById(giftDTO.getId());
        } catch (Exception e) {
            // No network, use offline mode
            try {
                //offlineGiftDAO.removeItemByOnlineId(onlineId);
                List<GiftDTO> notSyncedAddedItems = offlineGiftDAO.getNotSyncedAddedGifts();

                for (GiftDTO notSynceditem : notSyncedAddedItems) {
                    if (notSynceditem.getCacheID() == giftDTO.getCacheID()) {
                        offlineGiftDAO.removeGiftByCacheId(giftDTO.getCacheID());
                        foundNotSyncedIem = true;
                    }
                }

                // mark for deletion
                if (!foundNotSyncedIem) {
                    offlineGiftDAO.updateGift(giftDTO, 2);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    @Override
    public int syncToOnlineDB(int userId) {
        // get all items that are not synced from local db
        List<GiftDTO> notSyncedAddedGifts = null;
        List<GiftDTO> notSyncedRemovedGifts = null;
        try {
            notSyncedAddedGifts = offlineGiftDAO.getNotSyncedAddedGifts();
            notSyncedRemovedGifts = offlineGiftDAO.getNotSyncedRemovedGifts();
        } catch (Exception e1) {
            e1.printStackTrace();
            return -1;
        }

        // Sync added items to online db if available
        if (notSyncedAddedGifts != null && notSyncedAddedGifts.size() > 0) {

            for (GiftDTO giftDTO : notSyncedAddedGifts) {
                try {
                    onlineGiftDAO.addGift(giftDTO, 1); // add not synced item to online db
                    offlineGiftDAO.updateGift(giftDTO, 1); // mark item as synced in local db
                } catch (Exception e) {
                    // No network, can not sync
                    e.printStackTrace();
                    return -1;
                }
            }
        }

        // Sync removed items to online db if available
        if (notSyncedRemovedGifts != null && notSyncedRemovedGifts.size() > 0) {

            for (GiftDTO giftDTO : notSyncedRemovedGifts) {
                try {
                    onlineGiftDAO.removeGiftById(giftDTO.getId()); // add not synced item to online db
                    offlineGiftDAO.removeGiftById(giftDTO.getId()); // mark item as synced in local db
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
    public int syncFromOnlineDB(int userId) {
        if (!isNetworkAvaliable(context)) {
            return -1;
        }

        // get all items that are not synced from local db
        List<GiftDTO> onlineGifts = null;
        List<GiftDTO> localGifts = null;
        try {
            onlineGifts = onlineGiftDAO.getGifts(userId);
            localGifts = offlineGiftDAO.getGifts(userId);
        } catch (Exception e1) {
            e1.printStackTrace();
            return -1;
        }

        // Sync to offline db
        if (onlineGifts != null && onlineGifts.size() > 0) {
            for (GiftDTO onlineItemDto : onlineGifts) {
                boolean foundItem = false;
                try {
                    // check if online db item allready is defined in local db
                    for (GiftDTO localItemDTO : localGifts) {
                        if (localItemDTO.equals(onlineItemDto)) {
                            foundItem = true;
                        }
                    }

                    // Did not find the item in the local storage, add it
                    if (!foundItem) {
                        offlineGiftDAO.addGift(onlineItemDto, 1); // mark item as synced in local db
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
