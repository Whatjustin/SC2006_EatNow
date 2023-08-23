package com.example.eatnow.model;

import java.util.ArrayList;

/*
    Entity class for user's account.

    Subclass of Account
    To inherit the same attributes.

    Singleton Class
    To ensure same object reference.
 */
public class UserAccount extends Account {
    // Instance of user's account
    private static UserAccount user_instance = null;

    // Constructor
    public UserAccount(int account_id, byte[] profile, String username, ArrayList<Integer> followers, ArrayList<Integer> followings) {
        super(account_id, profile, username, followers, followings);
    }

    // Mutators and Accessors
    public static UserAccount getInstance() {
        return user_instance;
    }

    public static void setInstance(UserAccount user_instance) {
        UserAccount.user_instance = user_instance;
    }
}
