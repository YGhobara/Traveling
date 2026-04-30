package com.example.traveling.repositories;

import androidx.annotation.NonNull;

import com.example.traveling.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {

    private final FirebaseFirestore db;

    public PostRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnPostsLoadedListener {
        void onSuccess(List<Post> posts);
        void onError(Exception exception);
    }

    public void getPublicPosts(final OnPostsLoadedListener listener) {
        db.collection("posts")
                .whereEqualTo("publicPost", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Post> posts = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Post post = document.toObject(Post.class);

                            if (post != null) {
                                post.setId(document.getId());
                                posts.add(post);
                            }
                        }

                        listener.onSuccess(posts);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e);
                    }
                });
    }

    public interface OnPostActionListener {
        void onSuccess();
        void onError(Exception exception);
    }

    public void toggleLike(Post post, String userId, final OnPostActionListener listener) {
        if (post == null || post.getId() == null || userId == null) {
            listener.onError(new IllegalArgumentException("Invalid post or user."));
            return;
        }

        DocumentReference postRef = db.collection("posts").document(post.getId());

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    Post freshPost = transaction.get(postRef).toObject(Post.class);

                    if (freshPost == null) {
                        throw new IllegalStateException("Post not found.");
                    }

                    boolean alreadyLiked = freshPost.getLikedBy() != null
                            && freshPost.getLikedBy().contains(userId);

                    if (alreadyLiked) {
                        transaction.update(postRef,
                                "likedBy", FieldValue.arrayRemove(userId),
                                "likeCount", FieldValue.increment(-1));
                    } else {
                        transaction.update(postRef,
                                "likedBy", FieldValue.arrayUnion(userId),
                                "likeCount", FieldValue.increment(1));
                    }

                    return null;
                }).addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }
}