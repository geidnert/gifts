package com.solidparts.gifts;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.solidparts.gifts.dto.DataDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;
import com.solidparts.gifts.service.UserService;


public class MainActivity extends ActionBarActivity {
    public final static String EXTRA_USERDTO = "userDTO";
    public final static String EXTRA_VIEWUSERDTO = "viewUserDTO";

    private UserDTO userDTO;
    private UserService userService;
    private MessageManager messageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle("Gifts v" + this.getResources().getInteger(R.integer.app_major_version) + "." + this.getResources().getInteger(R.integer.app_minor_version));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check network status
        messageManager = new MessageManager();
        if (!userService.isNetworkAvaliable(this)) {
            messageManager.show(this, "No network connection available!", true);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");

        userService = new UserService(this);

        ((TextView) findViewById(R.id.msg)).setText("Welcome " + userDTO.getFirstname() + "!");

        // Sync data
        String[] appArgs = new String[]{};
        AppSyncTask appSyncTask = new AppSyncTask();
        appSyncTask.execute(appArgs);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void onMyGifts(View v) {
        messageManager = new MessageManager();
        if (!userService.isNetworkAvaliable(MainActivity.this)) {
            messageManager.show(MainActivity.this, "No network connection available!", true);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        try {
            Intent intent = new Intent(MainActivity.this, GiftsActivity.class);
            intent.putExtra(EXTRA_USERDTO, userDTO);
            intent.putExtra(EXTRA_VIEWUSERDTO, userDTO);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void onOthersGifts(View v) {
        messageManager = new MessageManager();
        if (!userService.isNetworkAvaliable(MainActivity.this)) {
            messageManager.show(MainActivity.this, "No network connection available!", true);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }


        try {
            Intent intent = new Intent(MainActivity.this, FriendListActivity.class);
            intent.putExtra(EXTRA_USERDTO, userDTO);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void onLogout(View v) {
        try {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }


    class AppSyncTask extends AsyncTask<String, DataDTO, DataDTO> {

        @Override
        protected DataDTO doInBackground(String... searchTerms) {
            try {
                return userService.getAppData();
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
        protected void onPostExecute(DataDTO dataDTO) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            //showButtons();
            if (dataDTO != null && getResources().getInteger(R.integer.app_minor_version) < dataDTO.getLatestAppVersion()) {
                UpdateDialogFragment updateDialogFragment = new UpdateDialogFragment();
                updateDialogFragment.show(getFragmentManager(), "updateDialog");
            }
        }


        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            //hideButtons();
        }
    }

    public static class UpdateDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("New version of Gifts is available, please download it now!")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String url = "http://solidparts.se/gifts/install/";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);

                            dialog.dismiss();
                        }

                    })
                    .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
