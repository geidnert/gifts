package com.solidparts.gifts;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.FriendListActivity;


public class MainActivity extends ActionBarActivity {
    public final static String EXTRA_USERDTO = "userDTO";

    private UserDTO userDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");

        ((TextView) findViewById(R.id.msg)).setText("Welcome " + userDTO.getFirstname() + " to your gift organizer.");
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
        try {
            Intent intent = new Intent(MainActivity.this, GiftsActivity.class);
            intent.putExtra(EXTRA_USERDTO, userDTO);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void onOthersGifts(View v) {
        try {
            Intent intent = new Intent(MainActivity.this, FriendListActivity.class);
            intent.putExtra(EXTRA_USERDTO, userDTO);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }
}
