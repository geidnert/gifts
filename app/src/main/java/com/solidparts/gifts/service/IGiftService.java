package com.solidparts.gifts.service;

import com.solidparts.gifts.dto.GiftDTO;

import java.util.List;

/**
 * Created by geidnert on 26/11/15.
 */
public interface IGiftService {
    public List<GiftDTO> getGifts(int userId) throws Exception;

    public void addGift(GiftDTO giftDTO) throws Exception;

    public void updateGift(GiftDTO giftDTO) throws Exception;

    public void removeGift(GiftDTO giftDTO) throws Exception;

    public int syncToOnlineDB(int userId);

    public int syncFromOnlineDB(int userId);
}
