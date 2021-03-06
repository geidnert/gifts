package com.solidparts.gifts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;

import java.util.Iterator;
import java.util.List;

/**
 * Created by geidnert on 29/11/15.
 */
public class GiftAdapter extends ArrayAdapter<GiftDTO> {

    private Boolean ownGift;
    private GiftService giftService;
    private UserDTO userDTO;
    private GiftsActivity giftActivity;
    private Context context;
    private List<GiftDTO> gifts;
    private GiftDTO actionGift;
    private MessageManager messageManager;

    public GiftAdapter(Context context, List<GiftDTO> gifts, Boolean ownGift, UserDTO userDTO, GiftService giftService, GiftsActivity giftsActivity) {
        super(context, 0, gifts);

        this.gifts = gifts;
        this.context = context;
        this.giftActivity = giftsActivity;
        this.ownGift = ownGift;
        this.userDTO = userDTO;
        this.giftService = giftService;
        messageManager = new MessageManager();
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

        final Bitmap bitmap = BitmapFactory.decodeByteArray(giftDTO.getImage(), 0,
                giftDTO.getImage().length);

        giftImage.setImageBitmap(bitmap);
        giftDescription.setText(giftDTO.getDescription());

        if(!ownGift && (giftDTO.getUrl() != null && !giftDTO.getUrl().equals(""))){
            giftDescription.setTextColor(0xFF5EA9A1);
        }


        ImageButton imageButton2 = (ImageButton) convertView.findViewById(R.id.imageButton2);
        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.imageButton);

        TextView giftBought = (TextView) convertView.findViewById(R.id.giftBought);


        // Ser en gift som redan är bokad
        if(!ownGift && giftDTO.isBought() && giftDTO.getBoughtById() != userDTO.getId()){
            imageButton2.setVisibility((View.GONE));
            imageButton.setVisibility((View.GONE));
            giftImage.setVisibility((View.GONE));
            giftDescription.setVisibility(View.GONE);
            giftBought.setVisibility(View.VISIBLE);
        // Man har bokat en gift och ser på den själv
        } else if(!ownGift && giftDTO.isBought() && giftDTO.getBoughtById() == userDTO.getId()){
            imageButton2.setBackgroundColor(0xFFFF8B8D);// Red
            imageButton2.setVisibility((View.VISIBLE));
            imageButton.setVisibility((View.GONE));
            giftImage.setVisibility((View.VISIBLE));
            giftDescription.setVisibility(View.VISIBLE);
            giftBought.setVisibility(View.GONE);
        // Man ser en gift som går att boka
        } else if(!ownGift && !giftDTO.isBought()) {
            imageButton2.setBackgroundColor(0xFF99FF8B);// Green
            imageButton2.setVisibility((View.VISIBLE));
            imageButton.setVisibility((View.GONE));
            giftImage.setVisibility((View.VISIBLE));
            giftDescription.setVisibility(View.VISIBLE);
            giftBought.setVisibility(View.GONE);
        // Det är ens egen gift som man kan ta bort
        } else {
            imageButton.setVisibility((View.VISIBLE));
            imageButton2.setVisibility((View.GONE));
            giftImage.setVisibility((View.VISIBLE));
            giftDescription.setVisibility(View.VISIBLE);
            giftBought.setVisibility(View.GONE);
        }

        giftImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giftActivity.onShowImage(bitmap);
            }
        });

        giftDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!giftService.isNetworkAvaliable(giftActivity)) {
                    messageManager.show(giftActivity, "No network connection available!", true);
                    Intent intent = new Intent(giftActivity, LoginActivity.class);
                    context.startActivity(intent);
                }

                if (ownGift) {
                    giftActivity.update(giftDTO);
                } else {
                    if(giftDTO.getUrl() != null && !giftDTO.getUrl().equals("")) {
                        Uri uri = Uri.parse(giftDTO.getUrl()); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }
                }
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!giftService.isNetworkAvaliable(giftActivity)) {
                    messageManager.show(giftActivity, "No network connection available!", true);
                    Intent intent = new Intent(giftActivity, LoginActivity.class);
                    context.startActivity(intent);
                }

                if(ownGift){
                    giftActivity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                    // TODO - Remove gift
                    RemoveGiftTask removeGiftTask = new RemoveGiftTask();
                    GiftDTO[] gifts = new GiftDTO[1];
                    gifts[0] = giftDTO;
                    removeGiftTask.execute(gifts);
                }
            }
        });

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!giftService.isNetworkAvaliable(giftActivity)) {
                    messageManager.show(giftActivity, "No network connection available!", true);
                    Intent intent = new Intent(giftActivity, LoginActivity.class);
                    context.startActivity(intent);
                }

                if (!giftDTO.isBought() || (giftDTO.isBought() && giftDTO.getBoughtById() == userDTO.getId())) {
                    giftActivity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                    // TODO - Toggle bought

                    if (giftDTO.isBought()) {
                        giftDTO.setBought(false);
                        giftDTO.setBoughtById(0);
                    } else {
                        giftDTO.setBought(true);
                        giftDTO.setBoughtById(userDTO.getId());
                    }

                    try {
                        UpdateGiftTask updateGiftTask = new UpdateGiftTask();
                        GiftDTO[] gifts = new GiftDTO[1];
                        gifts[0] = giftDTO;
                        updateGiftTask.execute(gifts);
                        //giftService.updateGift(giftDTO);
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

    private class UpdateGiftTask extends AsyncTask<GiftDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(GiftDTO... giftDTO) {
            try {
                actionGift = giftDTO[0];
                giftService.updateGift(actionGift);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Iterator it =  gifts.iterator();

                while(it.hasNext()){
                    GiftDTO gift = (GiftDTO)it.next();

                    if(actionGift != null && gift.equals(actionGift)) {
                        notifyDataSetChanged();
                        actionGift = null;
                    }
                }
            }

            giftActivity.findViewById(R.id.progressBar).setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            //findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            //disableButtons();
        }
    }

    private class RemoveGiftTask extends AsyncTask<GiftDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(GiftDTO... giftDTO) {
            try {
                actionGift = giftDTO[0];
                giftService.removeGift(actionGift);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Iterator it =  gifts.iterator();

                while(it.hasNext()){
                    GiftDTO gift = (GiftDTO)it.next();

                    if(actionGift != null && gift.equals(actionGift)) {
                        it.remove();
                        notifyDataSetChanged();
                        actionGift = null;
                    }
                }
            }

            giftActivity.findViewById(R.id.progressBar).setVisibility(View.GONE);
            giftActivity.setTitle("My gifts (" + gifts.size() + ")");
        }

        @Override
        protected void onPreExecute() {
            //findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            //disableButtons();
        }
    }

}