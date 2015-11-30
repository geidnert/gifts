package com.solidparts.gifts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geidnert on 29/11/15.
 */
public class GiftAdapter extends ArrayAdapter<GiftDTO> {

    private Boolean ownGift;
    private GiftService giftService;
    private UserDTO userDTO;

    public GiftAdapter(Context context, List<GiftDTO> gifts, Boolean ownGift, UserDTO userDTO, GiftService giftService) {
        super(context, 0, gifts);

        this.ownGift = ownGift;
        this.userDTO = userDTO;
        this.giftService = giftService;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final GiftDTO giftDTO = getItem(position);
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

        Button button = (Button) convertView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ownGift){
                    // TODO - Remove gift
                    RemoveGiftTask removeGiftTask = new RemoveGiftTask();
                    GiftDTO[] gifts = new GiftDTO[1];
                    gifts[0] = giftDTO;
                    removeGiftTask.execute(gifts);

                } else {
                    // TODO - Toggle bought

                    if(giftDTO.isBought()){
                        giftDTO.setBought(false);
                        giftDTO.setBoughtById(0);
                    } else {
                        giftDTO.setBought(true);
                        giftDTO.setBoughtById(userDTO.getId());
                    }

                    try {
                        giftService.updateGift(giftDTO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }


    //---------------------------------------------------------------------------------------------
    // -------------------------- ASYNC -----------------------------------------------------------

    private class RemoveGiftTask extends AsyncTask<GiftDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(GiftDTO... giftDTO) {
            try {
                giftService.removeGift(giftDTO[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
           // findViewById(R.id.progressBar).setVisibility(View.GONE);
            //enableButtons();
            if (success) {
                notifyDataSetChanged();
            }
            //else
                //messageManager.show(getApplicationContext(), "Gift not saved!", false);*/

            //startActivity(new Intent(AddItemActivity.this, MainActivity.class));
        }

        @Override
        protected void onPreExecute() {
            //findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            //disableButtons();
        }
    }

}