package com.example.traveling.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.models.UserProfile;
import com.example.traveling.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        TextInputEditText etFirstName = view.findViewById(R.id.et_register_first_name);
        TextInputEditText etLastName = view.findViewById(R.id.et_register_last_name);
        TextInputEditText etUsername = view.findViewById(R.id.et_register_username);
        TextInputEditText etEmail = view.findViewById(R.id.et_register_email);
        TextInputEditText etPassword = view.findViewById(R.id.et_register_password);

        MaterialButton btnRegister = view.findViewById(R.id.btn_register);
        MaterialButton btnGoLogin = view.findViewById(R.id.btn_go_login);

        btnGoLogin.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit()
        );

        btnRegister.setOnClickListener(v -> {
            String firstName = etFirstName.getText() != null ? etFirstName.getText().toString().trim() : "";
            String lastName = etLastName.getText() != null ? etLastName.getText().toString().trim() : "";
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (TextUtils.isEmpty(firstName)
                    || TextUtils.isEmpty(lastName)
                    || TextUtils.isEmpty(username)
                    || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.length() < 3) {
                Toast.makeText(requireContext(), "Le nom d'utilisateur doit contenir au moins 3 caractères", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(requireContext(), "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            btnRegister.setEnabled(true);

                            Toast.makeText(
                                    requireContext(),
                                    "Échec d'inscription : " + (task.getException() != null ? task.getException().getMessage() : ""),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser == null) {
                            btnRegister.setEnabled(true);
                            Toast.makeText(requireContext(), "Erreur : utilisateur introuvable", Toast.LENGTH_LONG).show();
                            return;
                        }

                        UserProfile userProfile = new UserProfile(
                                firebaseUser.getUid(),
                                firstName,
                                lastName,
                                username,
                                email,
                                System.currentTimeMillis()
                        );

                        userRepository.createUserProfile(userProfile, new UserRepository.OnUserProfileActionListener() {
                            @Override
                            public void onSuccess() {
                                if (!isAdded()) return;

                                Toast.makeText(requireContext(), "Compte créé avec succès", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(requireActivity(), MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                            }

                            @Override
                            public void onError(Exception exception) {
                                if (!isAdded()) return;

                                btnRegister.setEnabled(true);

                                Toast.makeText(
                                        requireContext(),
                                        "Compte créé, mais erreur profil : " + exception.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    });
        });

        return view;
    }
}