package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.models.Group;
import com.example.traveling.repositories.GroupRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GroupDetailFragment extends Fragment {

    private static final String ARG_GROUP_ID = "groupId";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private GroupRepository groupRepository;

    private ImageButton buttonBack;
    private TextView textHeaderTitle;
    private TextView textGroupName;
    private TextView textGroupDescription;
    private TextView textGroupMeta;
    private TextView textGroupFeedStatus;
    private MaterialButton buttonJoinLeave;

    private String groupId;
    private Group currentGroup;

    public GroupDetailFragment() {
        // Required empty public constructor
    }

    public static GroupDetailFragment newInstance(String groupId) {
        GroupDetailFragment fragment = new GroupDetailFragment();

        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_detail, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        groupRepository = new GroupRepository();

        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }

        bindViews(view);
        setupActions();

        if (TextUtils.isEmpty(groupId)) {
            textGroupFeedStatus.setText("Groupe introuvable.");
            buttonJoinLeave.setVisibility(View.GONE);
        } else {
            loadGroup();
        }

        return view;
    }

    private void bindViews(View view) {
        buttonBack = view.findViewById(R.id.buttonBack);
        textHeaderTitle = view.findViewById(R.id.textHeaderTitle);
        textGroupName = view.findViewById(R.id.textGroupName);
        textGroupDescription = view.findViewById(R.id.textGroupDescription);
        textGroupMeta = view.findViewById(R.id.textGroupMeta);
        textGroupFeedStatus = view.findViewById(R.id.textGroupFeedStatus);
        buttonJoinLeave = view.findViewById(R.id.buttonJoinLeave);
    }

    private void setupActions() {
        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        buttonJoinLeave.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(requireContext(),
                        "Connectez-vous pour rejoindre un groupe.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentGroup == null || currentGroup.getId() == null) {
                return;
            }

            if (currentGroup.isMember(currentUser.getUid())) {
                leaveGroup();
            } else {
                joinGroup();
            }
        });
    }

    private void loadGroup() {
        textGroupFeedStatus.setText("Chargement du groupe...");

        groupRepository.getGroupById(groupId, new GroupRepository.GroupListener() {
            @Override
            public void onSuccess(Group group) {
                if (!isAdded()) return;

                currentGroup = group;
                displayGroup(group);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                textGroupFeedStatus.setText("Impossible de charger le groupe.");
                Toast.makeText(requireContext(),
                        "Erreur: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayGroup(Group group) {
        textHeaderTitle.setText(group.getName());
        textGroupName.setText(group.getName());
        textGroupDescription.setText(group.getDisplayDescription());

        String visibility = group.isPublicGroup() ? "Public" : "Privé";
        textGroupMeta.setText(group.getMemberCountText() + " · " + visibility);

        textGroupFeedStatus.setText("Les publications du groupe apparaîtront ici.");

        if (currentUser == null) {
            buttonJoinLeave.setText("Connexion requise");
            buttonJoinLeave.setEnabled(false);
            return;
        }

        boolean isMember = group.isMember(currentUser.getUid());

        if (isMember) {
            buttonJoinLeave.setText("Quitter le groupe");
            buttonJoinLeave.setEnabled(true);
        } else {
            buttonJoinLeave.setText("Rejoindre le groupe");
            buttonJoinLeave.setEnabled(true);
        }
    }

    private void joinGroup() {
        groupRepository.joinGroup(currentGroup.getId(), currentUser.getUid(), new GroupRepository.ActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Groupe rejoint.",
                        Toast.LENGTH_SHORT).show();

                loadGroup();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void leaveGroup() {
        groupRepository.leaveGroup(currentGroup.getId(), currentUser.getUid(), new GroupRepository.ActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Vous avez quitté le groupe.",
                        Toast.LENGTH_SHORT).show();

                loadGroup();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}