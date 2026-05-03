package com.example.traveling.models;

public class UserProfile {

    private String uid;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private int followersCount;
    private int followingCount;
    private long createdAt;

    public UserProfile() {
        // Required empty constructor for Firestore
    }

    public UserProfile(String uid, String firstName, String lastName,
                       String username, String email, long createdAt) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getDisplayName() {
        if (username != null && !username.isEmpty()) {
            return username;
        }

        String fullName = ((firstName != null ? firstName : "") + " " +
                (lastName != null ? lastName : "")).trim();

        if (!fullName.isEmpty()) {
            return fullName;
        }

        return email != null ? email : "Utilisateur";
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}