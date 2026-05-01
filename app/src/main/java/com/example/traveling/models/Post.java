package com.example.traveling.models;
import java.util.ArrayList;
import java.util.List;

public class Post {

    private String id;
    private String userId;
    private String authorName;
    private String caption;
    private String imageUrl;
    private String locationName;
    private String placeType;
    private long createdAt;
    private int likeCount;
    private int commentCount;
    private boolean publicPost;
    private List<String> likedBy = new ArrayList<>();
    public Post() {
        // Required empty constructor for Firestore
    }

    public Post(String id, String userId, String authorName, String caption,
                String imageUrl, String locationName, String placeType, long createdAt,
                int likeCount, int commentCount,boolean publicPost) {
        this.id = id;
        this.userId = userId;
        this.authorName = authorName;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.locationName = locationName;
        this.placeType = placeType;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
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

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<String> getLikedBy() { return likedBy; }

    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }

    public boolean isLikedByUser(String userId) {
        return likedBy != null && likedBy.contains(userId);
    }

    public boolean isPublicPost() {
        return publicPost;
    }

    public void setPublicPost(boolean publicPost) {
        this.publicPost = publicPost;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }
}