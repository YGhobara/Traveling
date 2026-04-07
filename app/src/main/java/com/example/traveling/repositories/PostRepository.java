package com.example.traveling.repositories;

import androidx.annotation.NonNull;

import com.example.traveling.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

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
}