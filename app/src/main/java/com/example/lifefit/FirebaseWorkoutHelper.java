package com.example.lifefit;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirebaseWorkoutHelper {

    private static DatabaseReference getWorkoutsRef() {
        String userId = FirebaseAuthHelper.getUserId();
        if (userId == null) return null;

        return FirebaseDatabase.getInstance()
                .getReference("workouts")
                .child(userId);
    }

    // Create a new workout
    public static void createWorkout(Workout workout, OnCompleteListener listener) {
        DatabaseReference ref = getWorkoutsRef();
        if (ref == null) {
            listener.onFailure("User not logged in");
            return;
        }

        // Generate unique ID
        String workoutId = ref.push().getKey();
        if (workoutId == null) {
            listener.onFailure("Failed to generate workout ID");
            return;
        }

        workout.setId(workoutId);

        // Save to Firebase
        ref.child(workoutId).setValue(workout.toMap())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Get all workouts for current user
    public static void getAllWorkouts(OnWorkoutsLoadedListener listener) {
        DatabaseReference ref = getWorkoutsRef();
        if (ref == null) {
            listener.onWorkoutsLoaded(new ArrayList<>());
            return;
        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Workout> workouts = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map != null) {
                        Workout workout = Workout.fromMap(map);
                        workouts.add(workout);
                    }
                }

                listener.onWorkoutsLoaded(workouts);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    // Update workout
    // In FirebaseWorkoutHelper.java, add this method if not present:
    public static void updateWorkout(Workout workout, OnCompleteListener listener) {
        DatabaseReference ref = getWorkoutsRef();
        if (ref == null) {
            listener.onFailure("User not logged in");
            return;
        }

        // Update workout in Firebase
        ref.child(workout.getId()).setValue(workout.toMap())
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Delete workout
    public static void deleteWorkout(String workoutId, OnCompleteListener listener) {
        DatabaseReference ref = getWorkoutsRef();
        if (ref == null) {
            listener.onFailure("User not logged in");
            return;
        }

        ref.child(workoutId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Mark workout as completed/incomplete
    public static void toggleWorkoutCompletion(String workoutId, boolean isCompleted,
                                               OnCompleteListener listener) {
        DatabaseReference ref = getWorkoutsRef();
        if (ref == null) {
            listener.onFailure("User not logged in");
            return;
        }

        ref.child(workoutId).child("isCompleted").setValue(isCompleted)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Interfaces for callbacks
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnWorkoutsLoadedListener {
        void onWorkoutsLoaded(List<Workout> workouts);
        void onError(String error);
    }
}