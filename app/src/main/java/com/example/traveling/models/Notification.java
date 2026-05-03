package com.example.traveling.models;

public class Notification {

    private String id;
    private String userId;
    private String title;
    private String message;
    private String type;
    private long createdAt;
    private boolean read;
    private String relatedPostId;
    private String relatedGroupId;

    public Notification() {
        // Required empty constructor for Firestore
    }

    public Notification(String id, String userId, String title, String message,
                        String type, long createdAt, boolean read,
                        String relatedPostId, String relatedGroupId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
        this.relatedPostId = relatedPostId;
        this.relatedGroupId = relatedGroupId;
    }

    public static Notification createPostNotification(String userId,
                                                      String title,
                                                      String message,
                                                      String type,
                                                      String relatedPostId,
                                                      String relatedGroupId) {
        return new Notification(
                null,
                userId,
                title,
                message,
                type,
                System.currentTimeMillis(),
                false,
                relatedPostId,
                relatedGroupId
        );
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getRelatedPostId() {
        return relatedPostId;
    }

    public void setRelatedPostId(String relatedPostId) {
        this.relatedPostId = relatedPostId;
    }

    public String getRelatedGroupId() {
        return relatedGroupId;
    }

    public void setRelatedGroupId(String relatedGroupId) {
        this.relatedGroupId = relatedGroupId;
    }
}