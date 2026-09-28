package com.example.lifefit;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    // Views
    private CardView cvProfilePicture;
    private ImageView ivProfilePicture;
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private LinearLayout llEditProfile;
    private LinearLayout llSettings;
    private LinearLayout llHelpSupport;
    private LinearLayout llAbout;
    private Button btnLogout;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize loading dialog
        loadingDialog = new LoadingDialog(requireContext());

        // Initialize views
        initViews(view);

        // Load user data
        loadUserData();

        // Setup listeners
        setupListeners();
    }

    private void initViews(View view) {
        cvProfilePicture = view.findViewById(R.id.cvProfilePicture);
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        llEditProfile = view.findViewById(R.id.llEditProfile);
        llSettings = view.findViewById(R.id.llSettings);
        llHelpSupport = view.findViewById(R.id.llHelpSupport);
        llAbout = view.findViewById(R.id.llAbout);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        // Load user data from Firebase
        String userName = FirebaseAuthHelper.getUserName();
        String userEmail = FirebaseAuthHelper.getUserEmail();

        // Set user data to views
        if (tvProfileName != null) {
            tvProfileName.setText(userName);
        }
        if (tvProfileEmail != null) {
            tvProfileEmail.setText(userEmail != null ? userEmail : "No email");
        }
    }

    private void setupListeners() {
        // Profile Picture Click
        if (cvProfilePicture != null) {
            cvProfilePicture.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Change profile picture coming soon!", Toast.LENGTH_SHORT).show();
                // TODO: Implement image picker in future
            });
        }

        // Edit Profile
        if (llEditProfile != null) {
            llEditProfile.setOnClickListener(v -> {
                showEditProfileDialog();
            });
        }

        // Settings
        if (llSettings != null) {
            llSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to Settings screen
            });
        }

        // Help & Support
        if (llHelpSupport != null) {
            llHelpSupport.setOnClickListener(v -> {
                showHelpDialog();
            });
        }

        // About
        if (llAbout != null) {
            llAbout.setOnClickListener(v -> {
                showAboutDialog();
            });
        }

        // Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                showLogoutConfirmation();
            });
        }
    }

    private void showEditProfileDialog() {
        // Create dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_profile);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Get dialog views
        TextInputEditText etDialogName = dialog.findViewById(R.id.etDialogName);
        TextInputEditText etDialogEmail = dialog.findViewById(R.id.etDialogEmail);
        Button btnChangeProfilePicture = dialog.findViewById(R.id.btnChangeProfilePicture);
        Button btnDialogCancel = dialog.findViewById(R.id.btnDialogCancel);
        Button btnDialogSave = dialog.findViewById(R.id.btnDialogSave);

        // Pre-fill current data
        String currentName = FirebaseAuthHelper.getUserName();
        String currentEmail = FirebaseAuthHelper.getUserEmail();

        if (etDialogName != null) {
            etDialogName.setText(currentName);
        }
        if (etDialogEmail != null) {
            etDialogEmail.setText(currentEmail);
        }

        // Change profile picture button
        if (btnChangeProfilePicture != null) {
            btnChangeProfilePicture.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Profile picture upload coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Cancel button
        if (btnDialogCancel != null) {
            btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        }

        // Save button
        if (btnDialogSave != null) {
            btnDialogSave.setOnClickListener(v -> {
                String newName = etDialogName != null ?
                        etDialogName.getText().toString().trim() : "";

                // Validate name
                if (newName.isEmpty()) {
                    if (etDialogName != null) {
                        etDialogName.setError("Name cannot be empty");
                        etDialogName.requestFocus();
                    }
                    return;
                }

                if (newName.length() < 2) {
                    if (etDialogName != null) {
                        etDialogName.setError("Name must be at least 2 characters");
                        etDialogName.requestFocus();
                    }
                    return;
                }

                // Update profile
                updateProfile(newName, dialog);
            });
        }

        dialog.show();
    }

    private void updateProfile(String newName, Dialog dialog) {
        // Show loading
        loadingDialog.show("Updating Profile", "Saving your changes...");

        // Update display name in Firebase Auth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update in Realtime Database
                            updateNameInDatabase(newName, dialog);
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(getContext(),
                                    "Failed to update profile: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void updateNameInDatabase(String newName, Dialog dialog) {
        String userId = FirebaseAuthHelper.getUserId();
        if (userId == null) {
            loadingDialog.dismiss();
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);

        mDatabase.child("users").child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    loadingDialog.dismiss();

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "✓ Profile updated successfully!",
                                Toast.LENGTH_SHORT).show();

                        // Refresh UI
                        loadUserData();

                        // Close dialog
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(),
                                "Failed to save changes: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Help & Support")
                .setMessage("Need help?\n\n" +
                        "• Check our FAQ section\n" +
                        "• Contact support: support@fitlife.com\n" +
                        "• Visit our website: www.fitlife.com\n\n" +
                        "We're here to help you achieve your fitness goals!")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("About FitLife")
                .setMessage("FitLife - Your Personal Workout Companion\n\n" +
                        "Version 1.0\n\n" +
                        "Developed for CET343 Android Development Module\n\n" +
                        "Features:\n" +
                        "• Create custom workouts\n" +
                        "• Browse workout templates\n" +
                        "• Track your progress\n" +
                        "• Share workouts via SMS\n" +
                        "• Cloud synchronization\n\n" +
                        "© 2024 FitLife")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Show loading
        loadingDialog.show("Logging Out", "Signing you out...");

        // Sign out from Firebase
        FirebaseAuthHelper.signOut();

        // Hide loading
        loadingDialog.dismiss();

        // Show logout message
        Toast.makeText(getContext(), "✓ Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to Login screen
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish the current activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user data when returning to fragment
        loadUserData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dismiss loading dialog if showing
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}