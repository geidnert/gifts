package com.solidparts.gifts;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;
import com.solidparts.gifts.service.UserService;

public class RegisterActivity extends AppCompatActivity {

    private UserService userService;
    private MessageManager messageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userService = new UserService(this);
        messageManager = new MessageManager();

        String email = getIntent().getStringExtra("email");

        ((TextView) findViewById(R.id.email)).setText(email);


    }

    public void onRegister(View v) {
        try {
            AddUserTask addUserTask = new AddUserTask();
            UserDTO[] users = new UserDTO[1];
            users[0] = getUserDTO();
            addUserTask.execute(users);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private UserDTO getUserDTO(){
        //String giftName = ((EditText) findViewById(R.id.giftName)).getText().toString();
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String firstName= ((EditText) findViewById(R.id.firstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.lastName)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        String group = ((EditText) findViewById(R.id.groupName)).getText().toString();


        if (email.equals("") || firstName.equals("") || lastName.equals("") || password.equals("") ||
                group.equals("")) {
            messageManager.show(getApplicationContext(), "ERROR: You need to fill in the complete form, generate a qr code and add a image!", false);
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setFirstname(firstName);
        userDTO.setLastname(lastName);
        userDTO.setPassword(password);
        userDTO.setGroup(group);

        return userDTO;
    }

    private class AddUserTask extends AsyncTask<UserDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(UserDTO... userDTO) {
            try {
                userService.addUser(getUserDTO());
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
                messageManager.show(getApplicationContext(), "User Saved!", false);
                //SearchGiftTask searchGiftTask = new SearchGiftTask();

                //searchGiftTask.execute(new String[]{""+userDTO.getId()});
            }
            else
                messageManager.show(getApplicationContext(), "User not saved!", false);

            //startActivity(new Intent(AddItemActivity.this, MainActivity.class));
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            //disableButtons();
        }
    }
}
