package com.solidparts.gifts;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.UserService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FriendListActivity extends ListActivity {
    public final static String EXTRA_USERDTO = "userDTO";
    public final static String EXTRA_VIEWUSERDTO = "viewUserDTO";

    private UserDTO userDTO;
    private UserDTO viewUserDTO;

    private UserService userService;
    private MessageManager messageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");
        viewUserDTO = (UserDTO) getIntent().getSerializableExtra("viewUserDTO");

        userService = new UserService(this);
        messageManager = new MessageManager();


        SearchUserTask searchUserTask = new SearchUserTask();

        searchUserTask.execute(new String[]{"" + userDTO.getGroup()});
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

    //---------------------------------------------------------------------------------------------
    // -------------------------- ASYNC -----------------------------------------------------------

    class SearchUserTask extends AsyncTask<String, Integer, List<UserDTO>> {

        @Override
        protected List<UserDTO> doInBackground(String... searchTerms) {
            try {
                return userService.getUsers(Integer.parseInt(searchTerms[0]));
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
        protected void onPostExecute(final List<UserDTO> allUsers) {
            findViewById(R.id.progressBar).setVisibility(View.GONE);
            // adapt the search results returned from doInBackground so that they can be presented on the UI.
            if (allUsers != null && allUsers.size() > 0) {

                List<String> allUserNames = new ArrayList<>(allUsers.size());
                Iterator it = allUsers.iterator();
                while (it.hasNext()) {
                    UserDTO uDTO = (UserDTO) it.next();

                    if(userDTO.getId() == uDTO.getId()) {
                        it.remove();
                    } else {
                        allUserNames.add(uDTO.getFirstname() + " " + uDTO.getLastname());
                    }

                }

                ArrayAdapter<String> itemAdaptor = new ArrayAdapter<String>(FriendListActivity.this, android.R.layout.simple_list_item_1, allUserNames);
                // show the search resuts in the list.
                //setListAdapter(plantAdapter);

                //setProgressBarIndeterminateVisibility(false);
                //Intent intent = new Intent(SearchActivity.this, AddItemActivity.class);
                //intent.putExtra(EXTRA_ITEMDTO, allItems);
                //startActivity(intent);
                final ListView itemlistView = (ListView) findViewById(android.R.id.list);
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id., allItems);
// Assign adapter to ListView
                itemlistView.setAdapter(itemAdaptor);

                // ListView Item Click Listener
                itemlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition = position;

                        Intent intent = new Intent(FriendListActivity.this, GiftsActivity.class);
                        intent.putExtra(EXTRA_USERDTO, userDTO);
                        intent.putExtra(EXTRA_VIEWUSERDTO, allUsers.get(position));
                        startActivity(intent);

                        //Intent intent = new Intent(SearchActivity.this, AddItemActivity.class);
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
