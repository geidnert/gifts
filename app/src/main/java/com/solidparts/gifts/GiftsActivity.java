package com.solidparts.gifts;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ListActivity;
import android.widget.Toast;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class GiftsActivity extends ListActivity {
    public static final int CAMERA_REQUEST = 1;
    public static final int IMAGE_GALLERY_REQUEST = 2;

    private UserDTO userDTO;
    private GiftService giftService;
    private MessageManager messageManager;
    private ImageView giftImage;

    String[] itemname ={
            "Safari",
            "Camera",
            "Global",
            "FireFox",
            "UC Browser",
            "Android Folder",
            "VLC Player",
            "Cold War"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts);

        userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");
        giftService = new GiftService(this);
        messageManager = new MessageManager();
        giftImage = ((ImageView) findViewById(R.id.image));

        ((TextView) findViewById(R.id.userName)).setText(userDTO.getFirstname() + " " + userDTO.getLastname());

        this.setListAdapter(new ArrayAdapter<String>(
                this, R.layout.item_gift,
                R.id.giftDescription, itemname));

        SearchGiftTask searchGiftTask = new SearchGiftTask();

        searchGiftTask.execute(new String[]{"" + userDTO.getId()});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        GiftDTO giftDTO = getGiftDTO();

        if (giftDTO == null) {
            return;
        }

        /*if (update) {
            ItemUpdateTask itemUpdateTask = new ItemUpdateTask();
            ItemDTO[] items = new ItemDTO[1];
            items[0] = itemDTO;
            itemUpdateTask.execute(items);
        } else {*/
            AddGiftTask addGiftTask = new AddGiftTask();
            GiftDTO[] gifts = new GiftDTO[1];
            gifts[0] = giftDTO;
            addGiftTask.execute(gifts);
        //}
    }

    public void onTakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                CommonResources.cameraBmp = image;
                showImage(CommonResources.cameraBmp);
            }

            if (requestCode == IMAGE_GALLERY_REQUEST) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    showImage(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    //---------------------------------------------------------------------------------------------
    // -------------------------- PRIVATE -----------------------------------------------------------

    private void showImage(Bitmap image) {
        CommonResources.cameraBmp = image;
        giftImage.setImageBitmap(CommonResources.cameraBmp);
    }

    private GiftDTO getGiftDTO() {
        //String giftName = ((EditText) findViewById(R.id.giftName)).getText().toString();
        String giftDescription = ((EditText) findViewById(R.id.description)).getText().toString();
        String giftUrl = ((EditText) findViewById(R.id.url)).getText().toString();

        if (giftDescription.equals("")) {
            messageManager.show(getApplicationContext(), "ERROR: You need to fill in the complete form, generate a qr code and add a image!", false);
            return null;
        }

        GiftDTO giftDTO = new GiftDTO();
        //giftDTO.setName(name);
        giftDTO.setDescription(giftDescription);
        giftDTO.setUrl(giftUrl);
        giftDTO.setUserId(userDTO.getId());
        //itemDTO.setCacheID(cacheId);
        //itemDTO.setOnlineid(intentItemDTO != null ? intentItemDTO.getOnlineid() : -1);

        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        //ByteArrayOutputStream bos2 = new ByteArrayOutputStream();

        CommonResources.cameraBmp.compress(Bitmap.CompressFormat.JPEG, 100, bos1);

        byte[] itemImg = bos1.toByteArray();


        giftDTO.setImage(itemImg);

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

            //startActivity(new Intent(AddItemActivity.this, MainActivity.class));
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
            // adapt the search results returned from doInBackground so that they can be presented on the UI.
            if (allGifts != null && allGifts.size() > 0) {

                List<String> allItemNames = new ArrayList<>(allGifts.size());

                for (GiftDTO giftDTO : allGifts) {
                    allItemNames.add(giftDTO.getDescription());
                }

                //ArrayAdapter<String> itemAdaptor = new ArrayAdapter<String>(GiftsActivity.this, R.layout.item_gift, allItemNames);

                GiftAdapter giftAdapter = new GiftAdapter(GiftsActivity.this, allGifts);


                // show the search resuts in the list.
                //setListAdapter(plantAdapter);

                //setProgressBarIndeterminateVisibility(false);
                //Intent intent = new Intent(SearchActivity.this, AddItemActivity.class);
                //intent.putExtra(EXTRA_ITEMDTO, allItems);
                //startActivity(intent);
                final ListView giftlistView = (ListView) findViewById(android.R.id.list);
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id., allItems);
// Assign adapter to ListView
                giftlistView.setAdapter(giftAdapter);

                // ListView Item Click Listener
                giftlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition = position;

                        //Intent intent = new Intent(GiftsActivity.this, AddItemActivity.class);
                        //intent.putExtra(EXTRA_ITEMDTO, allItems.get(position));
                        //intent.putExtra(EXTRA_SEARCHWORD, ((EditText) findViewById(R.id.searchWord)).getText().toString());
                        //startActivity(intent);

                        // ListView Clicked item value
                        //String itemValue = (String) itemlistView.getItemAtPosition(position);

                        // Show Alert
                        //Toast.makeText(getApplicationContext(),
                        //        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        //        .show();

                    }

                });
            } else {
                messageManager.show(getApplicationContext(), "Did not find any matches!", false);
            }
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
        }
    }

}
