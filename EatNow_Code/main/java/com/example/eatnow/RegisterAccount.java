package com.example.eatnow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eatnow.fragment.LoginFragment;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
/*
    Represents Dialog Fragment (Pop-up) for registering account
 */

public class RegisterAccount extends DialogFragment implements ErrorPopUp {

    // CONSTANT to check action code in parent fragment
    public static final int REGISTER_ACC = 5;
    // Maximum input length of username and password
    static final int MAX_NAME_LENGTH = 20;
    static final int MAX_PW_LENGTH = 20;
    // UIs
    TextInputEditText email;
    TextInputEditText username;
    TextInputEditText password;
    TextInputEditText re_password;
    Button register;
    ImageView exit;
    // Store user's input
    String email_string;
    String username_string;
    String password_string;
    String re_password_string;
    // Represents new account id for registration
    int new_account_id;
    // Store lists of emails and usernames to check if email or username has existed
    ArrayList<String> emailAccounts = new ArrayList<>();
    ArrayList<String> usernameAccounts = new ArrayList<>();
    // To check whether database have retrieved the necessary information
    boolean retrievedAccountData = false;
    boolean retrievedAccountID = false;

    // Called when view is created
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.register_popup, container, false);

        // Retrieving list of emails and usernames from firebase
        retrieveAccountData();
        // Retrieving last account id from firebase
        retrieveAccountID();

        // Setting up UIs
        email = view.findViewById(R.id.EmailReg);
        username = view.findViewById(R.id.UsernameReg);
        password = view.findViewById(R.id.PasswordReg);
        re_password = view.findViewById(R.id.RePasswordReg);

        // Set Register button as clickable
        register = view.findViewById(R.id.btnRegister);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get User's inputs
                email_string = email.getText().toString();
                username_string = username.getText().toString();
                password_string = password.getText().toString();
                re_password_string = re_password.getText().toString();
                // Check if data has been retrieved from Firebase
                if (retrievedAccountData && retrievedAccountID) {
                    // Check if Registration is valid
                    if (checkRegistration(email_string, username_string, password_string, re_password_string)) {
                        // Setting up User's account and Adding the account data to firebase
                        addAccountData(new_account_id, email_string, username_string, password_string);
                        // Call successful registration back to Login Fragment to change to profile
                        Intent intent = new Intent();
                        getTargetFragment().onActivityResult(LoginFragment.LOGIN_FRAGMENT, REGISTER_ACC, intent);
                        Toast.makeText(getActivity(), "Welcome " + UserAccount.getInstance().getUsername(), Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    }
                } else {
                    errorPopUp("Error", "Please wait for data to load.");
                }
            }
        });
        // Set Exit as clickable
        exit = view.findViewById(R.id.register_exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        // Setting Counter to TextInputLayout (Username and Password)
        TextInputLayout textInputLayout;
        // For username
        textInputLayout = view.findViewById(R.id.usernameLayout);
        textInputLayout.setCounterEnabled(true);
        textInputLayout.setCounterMaxLength(MAX_NAME_LENGTH);
        // For password
        textInputLayout = view.findViewById(R.id.passwordLayout);
        textInputLayout.setCounterEnabled(true);
        textInputLayout.setCounterMaxLength(MAX_PW_LENGTH);

        // Limit Text for TextInputEditText (Username and Password)
        username.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});
        password.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PW_LENGTH)});

        return view;
    }

    // Retrieving List of Emails and Usernames from database. Set retrievedAccountData to true once done.
    private void retrieveAccountData() {
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("accounts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String em;
                String user;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    em = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                    user = Objects.requireNonNull(snapshot.child("username").getValue()).toString();
                    emailAccounts.add(em);
                    usernameAccounts.add(user);
                }
                retrievedAccountData = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                try {
                    errorPopUp("Error", "Unable to retrieve data.");
                } catch (Exception e) {
                }
            }
        });
    }

    // Retrieving last account id from database. Set retrievedAccountID to true once done.
    private void retrieveAccountID() {
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("id_counter").child("account_id");
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                new_account_id = dataSnapshot.getValue(Integer.class);
                // Set new account id to +1
                new_account_id += 1;
                retrievedAccountID = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /* Setting up User's account and Adding the account to firebase
        Default creation of Account will be:
            account_id -> assigned by checking id_counter in firebase ('\id_counter\account_id')
            email -> entered by user
            followers -> "" by default
            followings -> "" by default
            password -> entered by user
            profile -> null by default
            username -> entered by user
    */
    private void addAccountData(int account_id, String email, String username, String password) {

        HashMap<String, Object> account = new HashMap<>();

        account.put("account_id", account_id);
        account.put("email", email);
        account.put("followers", "");
        account.put("followings", "");
        account.put("password", password);
        account.put("profile", "");
        account.put("username", username);

        // Adding Account to database
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("accounts").child(String.valueOf(account_id - 1));
        reference.setValue(account);

        // Updating last account id
        reference = FirebaseDatabase.getInstance().getReference().child("id_counter").child("account_id");
        reference.setValue(account_id);

        // Setting user's account
        UserAccount.setInstance(new UserAccount(account_id, null, username, new ArrayList<>(), new ArrayList<>()));
    }

    /*
     Check if Registration is valid
     Account must hold the following requisites:

        (1) Email
        -> Unique (i.e. not taken)
        -> Contains @ (e.g. 123@h.com)
        -> No Whitespaces

        (2) Username
        -> Unique (i.e. not taken)
        -> Minimally 6 characters long

        (3) Password
        -> Minimally 8 characters long.
        -> Must contain both digits and alphabets.
        -> Must contain a special character (i.e. !?#$%&*)
        -> No Whitespaces

        (4) Retyped-Password
        -> Must match Password
     */
    private boolean checkRegistration(String email, String username, String password, String re_password) {

        // Check Email
        if (emailAccounts.contains(email)) {
            errorPopUp("Invalid Registration", "Email has been taken.");
            return false;
        }
        if (!email.contains("@")) {
            errorPopUp("Invalid Registration", "Invalid email.");
            return false;
        }
        if (email.contains(" ")) {
            errorPopUp("Invalid Registration", "Invalid email. Email contains whitespaces");
            return false;
        }

        // Check Username
        if (usernameAccounts.contains(username)) {
            errorPopUp("Invalid Registration",
                    "Username has been taken.");
            return false;
        }
        if (username.length() < 6) {
            errorPopUp("Invalid Registration",
                    "Invalid username. Please ensure that username is minimally 6 characters long.");
            return false;
        }

        // Check Password
        boolean hasDigit = false, hasAlphabet = false, hasSpecial = false;
        String special_char = "!?#$%&*";

        if (password.length() < 8) {
            errorPopUp("Invalid Registration",
                    "Invalid Password. Please ensure that password is minimally 8 characters long.");
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isAlphabetic(ch)) {
                hasAlphabet = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (special_char.contains(Character.toString(ch))) {
                hasSpecial = true;
            }
        }
        if (!(hasAlphabet && hasDigit && hasSpecial)) {
            errorPopUp("Invalid Registration",
                    "Invalid Password. Please ensure that password has:\n" +
                            "1.Alphabets (A-Z)\n" +
                            "2.Digits (0-9)\n" +
                            "3.Special Characters (!?#$%&*)");
            return false;
        }
        if (password.contains(" ")) {
            errorPopUp("Invalid Registration", "Invalid Password. Password contains whitespaces");
            return false;
        }

        // Check Re_password
        if (!password.equals(re_password)) {
            errorPopUp("Invalid Registration",
                    "Passwords do not match.");
            return false;
        }
        return true;
    }

    // Construct Error Pop Up
    @Override
    public void errorPopUp(String title, String error) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(error)
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }

    // Adjust Layout of Pop-up to maximize size
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

}
