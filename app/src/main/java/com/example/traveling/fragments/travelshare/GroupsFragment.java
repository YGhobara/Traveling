package com.example.traveling.fragments.travelshare;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.adapters.GroupAdapter;
import com.example.traveling.models.Group;
import com.example.traveling.repositories.GroupRepository;
import com.example.traveling.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class GroupsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private GroupRepository groupRepository;
    private UserRepository userRepository;

    private MaterialButton buttonCreateGroup;
    private TextView textGroupsStatus;
    private TextView textGuestHint;

    private RecyclerView recyclerMyGroups;
    private RecyclerView recyclerDiscoverGroups;

    private GroupAdapter myGroupsAdapter;
    private GroupAdapter discoverGroupsAdapter;

    private String currentUserDisplayName = "Voyageur";

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        groupRepository = new GroupRepository();
        userRepository = new UserRepository();

        bindViews(view);
        setupRecyclerViews();
        setupActions();

        if (currentUser == null) {
            displayGuestState();
        } else {
            loadCurrentUserName();
            loadGroups();
        }

        return view;
    }

    private void bindViews(View view) {
        buttonCreateGroup = view.findViewById(R.id.buttonCreateGroup);
        textGroupsStatus = view.findViewById(R.id.textGroupsStatus);
        textGuestHint = view.findViewById(R.id.textGuestHint);
        recyclerMyGroups = view.findViewById(R.id.recyclerMyGroups);
        recyclerDiscoverGroups = view.findViewById(R.id.recyclerDiscoverGroups);
    }

    private void setupRecyclerViews() {
        String userId = currentUser != null ? currentUser.getUid() : null;

        myGroupsAdapter = new GroupAdapter(
                userId,
                group -> openGroup(group),
                group -> joinGroup(group)
        );

        discoverGroupsAdapter = new GroupAdapter(
                userId,
                group -> openGroup(group),
                group -> joinGroup(group)
        );

        recyclerMyGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerMyGroups.setAdapter(myGroupsAdapter);

        recyclerDiscoverGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerDiscoverGroups.setAdapter(discoverGroupsAdapter);
    }

    private void setupActions() {
        buttonCreateGroup.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(requireContext(),
                        "Connectez-vous pour créer un groupe.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            showCreateGroupDialog();
        });
    }

    private void displayGuestState() {
        buttonCreateGroup.setEnabled(false);
        textGroupsStatus.setText("Mode invité");
        textGuestHint.setVisibility(View.VISIBLE);

        groupRepository.getPublicGroups(new GroupRepository.GroupListListener() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (!isAdded()) return;
                discoverGroupsAdapter.submitList(groups);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                textGroupsStatus.setText("Impossible de charger les groupes publics.");
            }
        });
    }

    private void loadCurrentUserName() {
        userRepository.getUserProfile(currentUser.getUid(), new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(com.example.traveling.models.UserProfile userProfile) {
                if (!isAdded()) return;

                String displayName = userProfile.getDisplayName();
                if (!TextUtils.isEmpty(displayName)) {
                    currentUserDisplayName = displayName;
                }
            }

            @Override
            public void onError(Exception exception) {
                // Keep default name
            }
        });
    }

    private void loadGroups() {
        textGroupsStatus.setText("Chargement des groupes...");
        textGuestHint.setVisibility(View.GONE);

        groupRepository.getMyGroups(currentUser.getUid(), new GroupRepository.GroupListListener() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (!isAdded()) return;

                myGroupsAdapter.submitList(groups);
                textGroupsStatus.setText(groups.size() + " groupe(s) rejoint(s)");
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                textGroupsStatus.setText("Impossible de charger vos groupes.");
            }
        });

        groupRepository.getPublicGroups(new GroupRepository.GroupListListener() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (!isAdded()) return;
                discoverGroupsAdapter.submitList(groups);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Impossible de charger les groupes publics.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateGroupDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(createGroupDialogLayout(), null);

        EditText editName = dialogView.findViewById(R.id.editGroupName);
        EditText editDescription = dialogView.findViewById(R.id.editGroupDescription);
        Switch switchPublic = dialogView.findViewById(R.id.switchPublicGroup);

        new AlertDialog.Builder(requireContext())
                .setTitle("Créer un groupe")
                .setView(dialogView)
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Créer", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();
                    boolean publicGroup = switchPublic.isChecked();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(requireContext(),
                                "Le nom du groupe est obligatoire.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createGroup(name, description, publicGroup);
                })
                .show();
    }

    private int createGroupDialogLayout() {
        // Temporary simple XML-free dialog.
        // We will replace this by a proper layout file if needed.
        return R.layout.dialog_create_group;
    }

    private void createGroup(String name, String description, boolean publicGroup) {
        Group group = Group.createNew(
                name,
                description,
                currentUser.getUid(),
                currentUserDisplayName,
                publicGroup
        );

        groupRepository.createGroup(group, new GroupRepository.ActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Groupe créé.",
                        Toast.LENGTH_SHORT).show();

                loadGroups();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur création groupe: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void joinGroup(Group group) {
        if (currentUser == null || group == null || group.getId() == null) return;

        groupRepository.joinGroup(group.getId(), currentUser.getUid(), new GroupRepository.ActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Groupe rejoint.",
                        Toast.LENGTH_SHORT).show();

                loadGroups();
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

    private void openGroup(Group group) {
        if (group == null || group.getId() == null) return;

        if (requireActivity() instanceof com.example.traveling.activities.MainActivity) {
            ((com.example.traveling.activities.MainActivity) requireActivity())
                    .openFragmentWithBackStack(GroupDetailFragment.newInstance(group.getId()));
        }
    }
}