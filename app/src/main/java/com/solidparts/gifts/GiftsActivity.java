package com.solidparts.gifts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.ListActivity;
import android.widget.Toast;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GiftsActivity extends ActionBarActivity {
    public static final int CAMERA_REQUEST = 1;
    public static final int IMAGE_GALLERY_REQUEST = 2;

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

        if(viewUserDTO.getId() == userDTO.getId()){
            setTitle("My gifts");
            //((TextView) findViewById(R.id.userName)).setText("My gifts");
        } else {
            setTitle(viewUserDTO.getFirstname() + " " + viewUserDTO.getLastname() + "'s gifts");
            //((TextView) findViewById(R.id.userName)).setText(viewUserDTO.getFirstname() + " " + viewUserDTO.getLastname() + "'s gifts");
        }
        searchGiftTask = new SearchGiftTask();
        search();

        if(!viewUserDTO.equals(userDTO)){
            (findViewById(R.id.image)).setVisibility(View.GONE);
            (findViewById(R.id.description)).setVisibility(View.GONE);
            (findViewById(R.id.url)).setVisibility(View.GONE);
            (findViewById(R.id.addGift)).setVisibility(View.GONE);
            (findViewById(R.id.clearGift)).setVisibility(View.GONE);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.FILL_PARENT
            );

        }
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

    public void onAddExistingImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        Uri data = Uri.parse(pictureDirectoryPath);
        intent.setDataAndType(data, "image/*");
        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    public void onTakePhoto(View view) {
        //UpdateDialogFragment updateDialogFragment = new UpdateDialogFragment();
        //updateDialogFragment.show(getFragmentManager(), "updateDialog");

        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent, CAMERA_REQUEST);

        onPickImage(view);
    }

    private static final int PICK_IMAGE_ID = 234; // the number doesn't matter

    public void onPickImage(View view) {
        //onTakePhoto(view);
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

            /*if (requestCode == CAMERA_REQUEST) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                CommonResources.cameraBmp = image;
                showImage(CommonResources.cameraBmp);
            }

            if (requestCode == IMAGE_GALLERY_REQUEST) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    CommonResources.cameraBmp = bitmap;
                    showImage(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }*/

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
        //CommonResources.cameraBmp = image;
        giftImage.setImageBitmap(image);
    }

    private GiftDTO getGiftDTO() {
        //String giftName = ((EditText) findViewById(R.id.giftName)).getText().toString();
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

        //Bitmap bitmap = ((Bitmap)giftImage.getDrawable()).getBitmap();
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.id.image);
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

    public static class UpdateDialogFragment extends DialogFragment {
GiftsActivity context;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("New photo or pick existing photo?")
                    .setPositiveButton("New", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            getActivity().startActivityForResult(intent, CAMERA_REQUEST);
                            //context.onTakePhoto();
                            dialog.dismiss();
                        }

                    })
                    .setNegativeButton("Existing", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            String pictureDirectoryPath = pictureDirectory.getPath();
                            Uri data = Uri.parse(pictureDirectoryPath);
                            intent.setDataAndType(data, "image/*");
                            getActivity().startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
                            //context.onAddExistingImage();
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }

        @Override
        public void onAttach(Activity activity) {
            // TODO Auto-generated method stub
            super.onAttach(activity);
            GiftsActivity context=(GiftsActivity)activity;
        }
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

            allUserGifts = allGifts;

            // adapt the search results returned from doInBackground so that they can be presented on the UI.
            if (allGifts != null && allGifts.size() > 0) {

                List<String> allItemNames = new ArrayList<>(allGifts.size());

                for (GiftDTO giftDTO : allGifts) {
                    allItemNames.add(giftDTO.getDescription());
                }

                //ArrayAdapter<String> itemAdaptor = new ArrayAdapter<String>(GiftsActivity.this, R.layout.item_gift, allItemNames);
                final ListView giftlistView = (ListView) findViewById(android.R.id.list);
                GiftAdapter giftAdapter = new GiftAdapter(GiftsActivity.this, allGifts, viewUserDTO.equals(userDTO), userDTO, giftService, GiftsActivity.this);


                // show the search resuts in the list.
                //setListAdapter(plantAdapter);

                //setProgressBarIndeterminateVisibility(false);
                //Intent intent = new Intent(SearchActivity.this, AddItemActivity.class);
                //intent.putExtra(EXTRA_ITEMDTO, allItems);
                //startActivity(intent);

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
                messageManager.show(getApplicationContext(), "Did not find any gifts for " +
                        viewUserDTO.getFirstname() + " " + viewUserDTO.getLastname() + "!", false);
            }
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
        }
    }

}
