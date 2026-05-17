package com.example.traveling.models;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private String id;
    private String userId;
    private String authorName;
    private String caption;
    private String imageUrl;
    private String locationName;
    private double latitude;
    private double longitude;
    private String photonPlaceId;
    private String placeType;
    private long createdAt;
    private int likeCount;
    private int commentCount;
    private boolean publicPost;
    private String groupId;
    private String groupName;
    private List<String> likedBy = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private String audioUrl;
    public Post() {
        // Required empty constructor for Firestore
    }

    public Post(String id, String userId, String authorName, String caption,
                String imageUrl, String locationName, double latitude, double longitude,
                String photonPlaceId, String placeType, long createdAt,
                int likeCount, int commentCount, boolean publicPost,
                String groupId, String groupName) {
        this.id = id;
        this.userId = userId;
        this.authorName = authorName;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photonPlaceId = photonPlaceId;
        this.placeType = placeType;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.publicPost = publicPost;
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public Post(String id, String userId, String authorName, String caption,
                String imageUrl, String locationName, double latitude, double longitude,
                String photonPlaceId, String placeType, long createdAt,
                int likeCount, int commentCount, boolean publicPost,
                String groupId, String groupName,
                List<String> tags, String audioUrl) {
        this.id = id;
        this.userId = userId;
        this.authorName = authorName;
        this.caption = caption;
        this.imageUrl = imageUrl;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photonPlaceId = photonPlaceId;
        this.placeType = placeType;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.publicPost = publicPost;
        this.groupId = groupId;
        this.groupName = groupName;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.audioUrl = audioUrl;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPhotonPlaceId() {
        return photonPlaceId;
    }

    public void setPhotonPlaceId(String photonPlaceId) {
        this.photonPlaceId = photonPlaceId;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Exclude
    public boolean isGroupPost() {
        return groupId != null && !groupId.trim().isEmpty();
    }

    @Exclude
    public boolean hasAudio() {
        return audioUrl != null && !audioUrl.trim().isEmpty();
    }

    @Exclude
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }
}