package com.example.eatnow.model;

import java.util.ArrayList;

/*
    Entity class for Account.
    Email / Password is not stored due to security issues.
 */
public class Account {

    // Unique id of account
    private int account_id;
    // Profile picture of account
    // If there is no profile picture, it is set to null.
    private byte[] profile;
    // Username of account
    private String username;
    // List of Ids of accounts that the account is being followed by
    // If there are no followers, it is set to an empty ArrayList. (i.e. new ArrayList<Integer>)
    private ArrayList<Integer> followers;
    // List of Ids of accounts that the account is following
    // If there are no followings, it is set to an empty ArrayList. (i.e. new ArrayList<Integer>)
    private ArrayList<Integer> followings;

    // Constructors
    public Account() {
    }

    public Account(int account_id, byte[] profile, String username, ArrayList<Integer> followers, ArrayList<Integer> followings) {
        this.account_id = account_id;
        this.profile = profile;
        this.username = username;
        this.followers = followers;
        this.followings = followings;
    }

    // Mutators and Accessors
    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public byte[] getProfile() {
        return profile;
    }

    public void setProfile(byte[] profile) {
        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<Integer> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<Integer> followers) {
        this.followers = followers;
    }

    public ArrayList<Integer> getFollowings() {
        return followings;
    }

    public void setFollowings(ArrayList<Integer> followings) {
        this.followings = followings;
    }
}
