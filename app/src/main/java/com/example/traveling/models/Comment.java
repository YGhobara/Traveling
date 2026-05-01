package com.example.traveling.models;

public class Comment {

    private String id;
    private String postId;
    private String userId;
    private String authorName;
    private String text;
    private long createdAt;

    public Comment() {
        // Required empty constructor for Firestore
    }

    public Comment(String id, String postId, String userId, String authorName,
                   String text, long createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.authorName = authorName;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}