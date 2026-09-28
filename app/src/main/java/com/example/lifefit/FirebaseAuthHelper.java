package com.example.lifefit;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthHelper {

    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Get current user
    public static FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    // Check if user is logged in
    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    // Get user ID
    public static String getUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Get user email
    public static String getUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    // Get user display name
    public static String getUserName() {
        FirebaseUser user = getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            return user.getDisplayName();
        }
        return "User";
    }

    // Sign out
    public static void signOut() {
        mAuth.signOut();
    }
}