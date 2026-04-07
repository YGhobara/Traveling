package com.example.traveling.models;

public class Post {

    private String id;
    private String userId;
    private String authorName;
    private String caption;
    private String imageUrl;
    private String locationName;
    private long createdAt;
    private int likeCount;
    private boolean publicPost;
    public Post() {
        // Required empty constructor for Firestore
    }

    public Post(String id, String userId, String authorName, String caption,
                String imageUrl, String locationName, long createdAt,
                int likeCount, boolean publicPost) {
        this.id = id;
        this.userId = userId;
        this.authorName = authorName;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.locationName = locationName;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.publicPost = publicPost;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isPublicPost() {
        return publicPost;
    }

    public void setPublicPost(boolean publicPost) {
        this.publicPost = publicPost;
    }
}