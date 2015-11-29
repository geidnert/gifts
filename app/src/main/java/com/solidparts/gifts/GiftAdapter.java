package com.solidparts.gifts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.solidparts.gifts.dto.GiftDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 29/11/15.
 */
public class GiftAdapter extends ArrayAdapter<GiftDTO> {
    public GiftAdapter(Context context, List<GiftDTO> gifts) {
        super(context, 0, gifts);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        GiftDTO giftDTO = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_gift, parent, false);
        }
        // Lookup view for data population
        ImageView giftImage = (ImageView) convertView.findViewById(R.id.giftImage);
        TextView giftDescription = (TextView) convertView.findViewById(R.id.giftDescription);
        // Populate the data into the template view using the data object

        Bitmap bitmap = BitmapFactory.decodeByteArray(giftDTO.getImage(), 0,
                giftDTO.getImage().length);

        giftImage.setImageBitmap(bitmap);
        giftDescription.setText(giftDTO.getDescription());
        // Return the completed view to render on screen
        return convertView;
    }
}