package com.solidparts.gifts;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.solidparts.gifts.dto.GiftDTO;
import com.solidparts.gifts.dto.UserDTO;
import com.solidparts.gifts.service.GiftService;
import com.solidparts.gifts.service.UserService;

public class RegisterActivity extends ActionBarActivity {
    public final static String EXTRA_EMAIL = "email";
    public final static String EXTRA_USERDTO = "userDTO";

    private UserService userService;
    private MessageManager messageManager;
    private UserDTO user = null;

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

    public void onCancel(View v) {
        try {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private UserDTO getUserDTO(){
        //String giftName = ((EditText) findViewById(R.id.giftName)).getText().toString();
        String email = ((TextView) findViewById(R.id.email)).getText().toString();
        String firstName= ((EditText) findViewById(R.id.firstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.lastName)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        String groupname = ((EditText) findViewById(R.id.groupName)).getText().toString();


        if (email.equals("") || firstName.equals("") || lastName.equals("") || password.equals("") ||
                groupname.equals("")) {
            messageManager.show(getApplicationContext(), "ERROR: You need to fill in the complete form!", false);
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setFirstname(firstName);
        userDTO.setLastname(lastName);
        userDTO.setPassword(password);
        userDTO.setGroupName(groupname);

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
                try {
                    // Simulate network access.
                    //user = userService.getUser(getUserDTO().getEmail(), getUserDTO().getPassword());
                    UserLoginTask mAuthTask = new UserLoginTask(getUserDTO().getEmail(), getUserDTO().getPassword());
                    mAuthTask.execute((Void) null);

                } catch (Exception e) {

                }

                if(user != null) {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra(EXTRA_EMAIL, user);
                    startActivity(intent);
                } else {

                }

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




        public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

            private final String mEmail;
            private final String mPassword;

            UserLoginTask(String email, String password) {
                mEmail = email;
                mPassword = password;
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                // TODO: attempt authentication against a network service.


                try {
                    // Simulate network access.
                    user = userService.getUser(mEmail, mPassword);

                } catch (Exception e) {
                    return false;
                }

                if(user != null) {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra(EXTRA_USERDTO, user);
                    startActivity(intent);
                } else {
                    return false;
                }

            /*for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }*/

                // TODO: register the new account here.
                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                //mAuthTask = null;
                //showProgress(false);

                if (success) {
                    finish();
                } else {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }


        }
    }
}
