package com.solidparts.gifts;

import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.AsyncTask;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import android.widget.EditText;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;

import java.io.ByteArrayOutputStream;

import java.util.Iterator;
import java.util.List;


public class GiftsActivity extends ActionBarActivity {
    private static final int PICK_IMAGE_ID = 3;

    private UserDTO userDTO;
    private static UserDTO viewUserDTO;
    private GiftService giftService;
    private MessageManager messageManager;
    private ImageView giftImage;
    private static SearchGiftTask searchGiftTask;
    private boolean update = false;
    GiftDTO updateGiftDTO = null;
    List<GiftDTO> allUserGifts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts);

        userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");
        viewUserDTO = (UserDTO) getIntent().getSerializableExtra("viewUserDTO");
        giftService = new GiftService(this);
        messageManager = new MessageManager();
        giftImage = ((ImageView) findViewById(R.id.image));

        searchGiftTask = new SearchGiftTask();
        search();


        if(viewUserDTO.getId() == userDTO.getId()){
            setTitle("My gifts");
        } else {
            setTitle(viewUserDTO.getFirstname() + " " + viewUserDTO.getLastname() + "'s gifts");
        }

        if(!viewUserDTO.equals(userDTO)){
            (findViewById(R.id.image)).setVisibility(View.GONE);
            (findViewById(R.id.description)).setVisibility(View.GONE);
            (findViewById(R.id.url)).setVisibility(View.GONE);
            (findViewById(R.id.addGift)).setVisibility(View.GONE);
            (findViewById(R.id.clearGift)).setVisibility(View.GONE);
            (findViewById(R.id.ruler)).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gifts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddGift(View v) {
        if (!giftService.isNetworkAvaliable(GiftsActivity.this)) {
            messageManager.show(GiftsActivity.this, "No network connection available!", true);
            Intent intent = new Intent(GiftsActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        GiftDTO giftDTO = getGiftDTO();

        if (giftDTO == null) {
            return;
        }

        if (update) {
            update = false;
            UpdateGiftTask updateGiftTask = new UpdateGiftTask();

            giftDTO.setBoughtById(updateGiftDTO.getBoughtById());
            giftDTO.setBought(updateGiftDTO.isBought());
            giftDTO.setId(updateGiftDTO.getId());

            GiftDTO[] giftDTOs = new GiftDTO[1];
            giftDTOs[0] = giftDTO;
            updateGiftTask.execute(giftDTOs);
        } else {
            AddGiftTask addGiftTask = new AddGiftTask();
            GiftDTO[] gifts = new GiftDTO[1];
            gifts[0] = giftDTO;
            addGiftTask.execute(gifts);
        }

        clearInputFields();

        ImageView img = (ImageView) findViewById(R.id.image);
        img.setImageResource(R.mipmap.camera);

    }

    public void onPickImage(View view) {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }



    public void update(GiftDTO giftDTO){
        updateGiftDTO = giftDTO;
        // Lookup view for data population
        update = true;
        ImageView giftImage = (ImageView) findViewById(R.id.image);
        TextView giftDescription = (TextView) findViewById(R.id.description);
        TextView giftUrl = (TextView) findViewById(R.id.url);
        // Populate the data into the template view using the data object

        Bitmap bitmap = BitmapFactory.decodeByteArray(giftDTO.getImage(), 0,
                giftDTO.getImage().length);


        giftImage.setImageBitmap(bitmap);
        CommonResources.cameraBmp = bitmap;
        giftDescription.setText(giftDTO.getDescription());
        giftUrl.setText(giftDTO.getUrl());

    }

    private void clearInputFields(){
        ImageView giftImage = (ImageView) findViewById(R.id.image);
        TextView giftDescription = (TextView) findViewById(R.id.description);
        TextView giftUrl = (TextView) findViewById(R.id.url);

        giftImage.setImageBitmap(null);
        CommonResources.cameraBmp = null;
        giftDescription.setText("");
        giftUrl.setText("");

        ImageView img= (ImageView) findViewById(R.id.image);
        img.setImageResource(R.mipmap.camera);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch(requestCode) {
                case PICK_IMAGE_ID:
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                    // TODO use bitmap
                    CommonResources.cameraBmp = bitmap;
                    showImage(bitmap);
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
            }
        }
    }

    public static void search(){
            searchGiftTask.execute(new String[]{"" + viewUserDTO.getId()});
    }

    public void onShowImage(Bitmap image) {
        (findViewById(R.id.addGift)).setVisibility(View.GONE);
        (findViewById(R.id.clearGift)).setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.fullImage)).setImageBitmap(image);
        (findViewById(R.id.fullImage)).setVisibility(View.VISIBLE);
    }

    public void onHideImage(View view) {
        if(viewUserDTO.getId() == userDTO.getId()) {
            (findViewById(R.id.clearGift)).setVisibility(View.VISIBLE);
            (findViewById(R.id.addGift)).setVisibility(View.VISIBLE);
        }

        (findViewById(R.id.fullImage)).setVisibility(View.GONE);
    }

    public void onClearGift(View v){
        clearInputFields();
        ImageView img = (ImageView) findViewById(R.id.image);
        img.setImageResource(R.mipmap.camera);
    }

    //---------------------------------------------------------------------------------------------
    // -------------------------- PRIVATE -----------------------------------------------------------

    private void showImage(Bitmap image) {
        giftImage.setImageBitmap(image);
    }

    private GiftDTO getGiftDTO() {
        String giftDescription = ((EditText) findViewById(R.id.description)).getText().toString();
        String giftUrl = ((EditText) findViewById(R.id.url)).getText().toString();

        if (giftDescription.equals("")) {
            messageManager.show(getApplicationContext(), "ERROR: You need to type description!", false);
            return null;
        }

        GiftDTO giftDTO = new GiftDTO();
        giftDTO.setDescription(giftDescription);
        giftDTO.setUrl(giftUrl);
        giftDTO.setUserId(userDTO.getId());

        if(CommonResources.cameraBmp != null) {
            ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
            CommonResources.cameraBmp.compress(Bitmap.CompressFormat.PNG, 100, bos1);
            byte[] itemImg = bos1.toByteArray();
            giftDTO.setImage(itemImg);
        } else {
            ImageView img= (ImageView) findViewById(R.id.image);
            img.setImageResource(R.mipmap.camera);

            img.buildDrawingCache();
            Bitmap bmap = img.getDrawingCache();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            giftDTO.setImage(byteArray);
        }


        return giftDTO;
    }

    //---------------------------------------------------------------------------------------------
    // -------------------------- ASYNC -----------------------------------------------------------

    private class AddGiftTask extends AsyncTask<GiftDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(GiftDTO... giftDTO) {
            try {
                giftService.addGift(giftDTO[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            //enableButtons();
            if (success) {
                messageManager.show(getApplicationContext(), "Gift Saved!", false);
                SearchGiftTask searchGiftTask = new SearchGiftTask();

                searchGiftTask.execute(new String[]{""+userDTO.getId()});
            }
            else
                messageManager.show(getApplicationContext(), "Gift not saved!", false);

            hideSoftKeyboard();
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            //disableButtons();
        }
    }

    private class UpdateGiftTask extends AsyncTask<GiftDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(GiftDTO... giftDTO) {
            try {
                giftService.updateGift(giftDTO[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            //enableButtons();
            if (success) {
                messageManager.show(getApplicationContext(), "Gift Saved!", false);
                SearchGiftTask searchGiftTask = new SearchGiftTask();

                searchGiftTask.execute(new String[]{""+userDTO.getId()});
                clearInputFields();
            }
            else
                messageManager.show(getApplicationContext(), "Gift not saved!", false);

            hideSoftKeyboard();
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            //disableButtons();
        }
    }

    class SearchGiftTask extends AsyncTask<String, Integer, List<GiftDTO>> {

        @Override
        protected List<GiftDTO> doInBackground(String... searchTerms) {
            try {
                return giftService.getGifts(Integer.parseInt(searchTerms[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */

        @Override
        protected void onPostExecute(final List<GiftDTO> allGifts) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);

            allUserGifts = allGifts;

            // adapt the search results returned from doInBackground so that they can be presented on the UI.
            if (allGifts != null && allGifts.size() > 0) {

                if(viewUserDTO.getId() == userDTO.getId()) {
                    setTitle("My gifts (" + allUserGifts.size() + ")");
                }

                Iterator it = allGifts.iterator();

                while(it.hasNext()){
                    GiftDTO giftDTO = (GiftDTO) it.next();

                    if(giftDTO.getUserId() != userDTO.getId() && giftDTO.isBought() && giftDTO.getBoughtById() != userDTO.getId()){
                        it.remove();
                    }
                }

                final ListView giftlistView = (ListView) findViewById(android.R.id.list);
                GiftAdapter giftAdapter = new GiftAdapter(GiftsActivity.this, allGifts, viewUserDTO.equals(userDTO), userDTO, giftService, GiftsActivity.this);

                giftlistView.setAdapter(giftAdapter);
            } else {
                messageManager.show(getApplicationContext(), "Did not find any gifts for " +
                        viewUserDTO.getFirstname() + " " + viewUserDTO.getLastname() + "!", false);
            }
            hideSoftKeyboard();
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
        }
    }

    private void hideSoftKeyboard(){
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(findViewById(R.id.description).getWindowToken(), 0);
        }
    }

}
