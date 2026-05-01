package com.example.traveling.models;

public class Report {

    private String id;
    private String postId;
    private String reportedByUserId;
    private String reason;
    private long createdAt;

    public Report() {
        // Required empty constructor for Firestore
    }

    public Report(String id, String postId, String reportedByUserId, String reason, long createdAt) {
        this.id = id;
        this.postId = postId;
        this.reportedByUserId = reportedByUserId;
        this.reason = reason;
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

    public String getReportedByUserId() {
        return reportedByUserId;
    }

    public void setReportedByUserId(String reportedByUserId) {
        this.reportedByUserId = reportedByUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}