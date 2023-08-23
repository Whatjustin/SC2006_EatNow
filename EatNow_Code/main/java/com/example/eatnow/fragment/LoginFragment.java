package com.example.eatnow.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.eatnow.R;
import com.example.eatnow.RegisterAccount;
import com.example.eatnow.databinding.FragmentLoginBinding;
import com.example.eatnow.model.Account;
import com.example.eatnow.model.UserAccount;
import com.example.eatnow.utility.ErrorPopUp;
import com.example.eatnow.utility.ListConverter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/*
    Represents Fragment for Logging In
    Upon creation, User must login or register to continue to profile.
 */
public class LoginFragment extends Fragment implements ErrorPopUp {

    // CONSTANT to check RequestCode for this fragment
    public static final int LOGIN_FRAGMENT = 4;
    // UIs
    TextInputEditText email;
    TextInputEditText password;
    Button login;
    TextView register;
    String email_string;
    String password_string;
    // Store <Email : Account_id> or <Password : Account_id> to verify account login
    HashMap<String, Integer> accountDataMap = new HashMap<>();
    // Store lists of all accounts to set user's account once login is verified
    ArrayList<Account> accounts = new ArrayList<>();
    // To check whether database have retrieved the necessary information
    boolean retrievedAccountMap = false;
    boolean retrievedAccounts = false;

    FragmentLoginBinding binding;

    // Called when view is created
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setting up UIs
        email = root.findViewById(R.id.EmailLogin);
        password = root.findViewById(R.id.PasswordLogin);

        // Set Login Button as clickable
        login = root.findViewById(R.id.btnLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve User's inputs
                email_string = email.getText().toString();
                password_string = password.getText().toString();
                // Check if Data has been retrieved
                if (retrievedAccountMap && retrievedAccounts) {
                    // Check if Login is valid
                    if (checkLogin(email_string, password_string)) {
                        // Retrieve User Data
                        retrieveUserData(email_string);
                        Toast.makeText(getActivity(), "Welcome " + UserAccount.getInstance().getUsername(), Toast.LENGTH_SHORT).show();
                        // Change Fragment to Profile
                        switchToProfile();
                    }
                } else {
                    // Else User has to wait for data to be retrieved.
                    errorPopUp("Error", "Please wait for data to load.");
                }
            }
        });

        // Set Registration TextView as clickable
        register = root.findViewById(R.id.RegisterText);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Configuring Dialog Fragment (Pop-up) for registering account
                FragmentManager fm = getFragmentManager();
                Fragment ft = fm.findFragmentById(R.id.fragment_container);

                fm = getActivity().getSupportFragmentManager();

                DialogFragment registerAccount = new RegisterAccount();
                // Setting target fragment to pass data from Dialog Fragment to this Fragment to ensure registration is valid.
                registerAccount.setTargetFragment(ft, LOGIN_FRAGMENT);
                registerAccount.show(fm, "Register Account");

                // Toast.makeText(getActivity(), "Registering Account", Toast.LENGTH_SHORT).show();
            }
        });
        // Retrieving list of matched emails and passwords based on account_id from firebase
        retrieveAccountData();
        // Retrieving list of accounts from firebase
        retrieveAccounts();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Called from RegisterAccount Dialog Fragment to switch to profile upon successful creation.
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_FRAGMENT) {
            if (resultCode == RegisterAccount.REGISTER_ACC) {
                // Change this fragment to profile fragment
                switchToProfile();
            }
        }
    }

    // To switch fragment to Profile
    private void switchToProfile() {
        FragmentManager fm = getFragmentManager();

        ProfileFragment profileFragment = new ProfileFragment();

        fm.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit();
    }

    // Retrieve HashMap of <Email,Account_id> and <Password,Account_id> from firebase. Set retrievedAccountData to true once done.
    private void retrieveAccountData() {
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("accounts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String em;
                String pw;
                int account_id;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    account_id = snapshot.child("account_id").getValue(Integer.class);
                    em = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                    pw = Objects.requireNonNull(snapshot.child("password").getValue()).toString();

                    accountDataMap.put(em, account_id);
                    accountDataMap.put(pw, account_id);
                }
                retrievedAccountMap = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Retrieve List of Accounts from firebase. Set retrievedAccounts to true once done.
    private void retrieveAccounts() {
        DatabaseReference reference;
        reference = FirebaseDatabase.getInstance().getReference().child("accounts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int account_id;
                byte[] profile;
                String username;
                ArrayList<Integer> followers;
                ArrayList<Integer> followings;

                Object dummy;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    account_id = snapshot.child("account_id").getValue(Integer.class);

                    dummy = snapshot.child("profile").getValue();
                    if (dummy != null) {
                        profile = dummy.toString().getBytes(StandardCharsets.UTF_8);
                    } else {
                        profile = null;
                    }

                    username = snapshot.child("username").getValue().toString();

                    dummy = snapshot.child("followers").getValue();
                    if (dummy != null) {
                        followers = ListConverter.convertStringToInts(dummy.toString());
                    } else {
                        followers = new ArrayList<>();
                    }

                    dummy = snapshot.child("followings").getValue();
                    if (dummy != null) {
                        followings = ListConverter.convertStringToInts(dummy.toString());
                    } else {
                        followings = new ArrayList<>();
                    }

                    accounts.add(new Account(account_id, profile, username, followers, followings));
                }
                retrievedAccounts = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Retrieving User Account based on email String.
    // Iterate through each account in the accounts Arraylist to match email.
    private void retrieveUserData(String email) {
        int account_id = accountDataMap.get(email);
        byte[] profile;
        String username;
        ArrayList<Integer> followers;
        ArrayList<Integer> followings;

        for (Account account : accounts) {
            if (account.getAccount_id() == account_id) {
                profile = account.getProfile();
                username = account.getUsername();
                followers = account.getFollowers();
                followings = account.getFollowings();
                // Setting instance to the user's account
                UserAccount.setInstance(new UserAccount(account_id, profile, username, followers, followings));
                return;
            }
        }
    }

    // Check if Login is valid by matching email and password with id
    private boolean checkLogin(String email, String password) {
        try {
            int account_id = accountDataMap.get(email);
            if (account_id == accountDataMap.get(password)) {
                return true;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            errorPopUp("Invalid Login", "Please ensure that you have entered the correct email and password.");
            return false;
        }
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


}
