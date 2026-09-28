package com.example.lifefit;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import androidx.cardview.widget.CardView;

public class WorkoutsFragment extends Fragment {

    // Views
    private EditText etSearchWorkout;

    private CardView cvStatsSummary;
    private TextView tvTotalWorkouts;
    private TextView tvCompletedWorkouts;
    private TextView tvPendingWorkouts;
    private TextView tvSectionHeader;
    private Button btnEmptyStateCreate;
    private RecyclerView rvAllWorkouts;
    private LinearLayout llEmptyWorkouts;
    private FloatingActionButton fabAddWorkout;

    // Data
    private List<Workout> workoutList;
    private List<Workout> filteredList;
    private WorkoutManageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_workouts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup gesture controls
        setupGestureControls();

        // Load workouts
        loadWorkouts();

        // Setup search
        setupSearch();

        // Setup FAB
        setupFAB();
    }

    private void initViews(View view) {
        etSearchWorkout = view.findViewById(R.id.etSearchWorkout);
        rvAllWorkouts = view.findViewById(R.id.rvAllWorkouts);
        llEmptyWorkouts = view.findViewById(R.id.llEmptyWorkouts);
        fabAddWorkout = view.findViewById(R.id.fabAddWorkout);

        cvStatsSummary = view.findViewById(R.id.cvStatsSummary);
        tvTotalWorkouts = view.findViewById(R.id.tvTotalWorkouts);
        tvCompletedWorkouts = view.findViewById(R.id.tvCompletedWorkouts);
        tvPendingWorkouts = view.findViewById(R.id.tvPendingWorkouts);
        tvSectionHeader = view.findViewById(R.id.tvSectionHeader);
        btnEmptyStateCreate = view.findViewById(R.id.btnEmptyStateCreate);

        // Setup empty state button
        if (btnEmptyStateCreate != null) {
            btnEmptyStateCreate.setOnClickListener(v -> {
                if (getActivity() instanceof DashboardActivity) {
                    ((DashboardActivity) getActivity()).navigateToCreateTab();
                }
            });
        }

    }




    private void setupRecyclerView() {
        workoutList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Initialize adapter with action listeners
        adapter = new WorkoutManageAdapter(filteredList, new WorkoutManageAdapter.OnWorkoutActionListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                // Launch WorkoutDetailsActivity
                openWorkoutDetails(workout);
            }



            @Override
            public void onEditClick(Workout workout) {
                // Edit workout - CALL THE METHOD HERE
                editWorkoutWithDialog(workout);
            }

            @Override
            public void onDeleteClick(Workout workout) {
                // Show delete confirmation
                showDeleteConfirmation(workout);
            }

            @Override
            public void onCheckChanged(Workout workout, boolean isChecked) {
                updateWorkoutCompletion(workout, isChecked);
            }

            // SMS Sharing
            @Override
            public void onShareClick(Workout workout) {
                shareWorkoutViaSMS(workout);
            }
        });

        // Set up RecyclerView
        rvAllWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAllWorkouts.setAdapter(adapter);
    }



    private void setupGestureControls() {
        // 1. Setup swipe gestures
        setupSwipeGestures();

        // 2. Setup long press gesture
        setupLongPressGesture();
    }

    // SWIPE GESTURES: Swipe left to delete, Swipe right to toggle complete
    private void setupSwipeGestures() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, // No drag & drop
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT // Enable both swipe directions
        ) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't support drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position < 0 || position >= filteredList.size()) return;

                Workout workout = filteredList.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe LEFT to delete
                    showSwipeDeleteConfirmation(workout, position);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe RIGHT to toggle complete/incomplete
                    toggleWorkoutCompletionWithSwipe(workout, position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas canvas,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                // Draw background based on swipe direction
                if (dX > 0) {
                    // Swiping RIGHT - Green background for complete/incomplete
                    ColorDrawable background = new ColorDrawable(
                            ContextCompat.getColor(getContext(), R.color.successGreen)
                    );
                    background.setBounds(
                            itemView.getLeft(),
                            itemView.getTop(),
                            itemView.getLeft() + (int) dX,
                            itemView.getBottom()
                    );
                    background.draw(canvas);

                    // Draw check icon
                    Drawable icon = ContextCompat.getDrawable(getContext(),
                            R.drawable.ic_check);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = iconLeft + icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(canvas);
                    }
                } else if (dX < 0) {
                    // Swiping LEFT - Red background for delete
                    ColorDrawable background = new ColorDrawable(
                            ContextCompat.getColor(getContext(), R.color.errorRed)
                    );
                    background.setBounds(
                            itemView.getRight() + (int) dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom()
                    );
                    background.draw(canvas);

                    // Draw delete icon
                    Drawable icon = ContextCompat.getDrawable(getContext(),
                            R.drawable.ic_delete);
                    if (icon != null) {
                        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();
                        int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(canvas);
                    }
                }
            }
        };

        // Attach ItemTouchHelper to RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvAllWorkouts);
    }

    // LONG PRESS GESTURE: Long press to edit
    private void setupLongPressGesture() {
        // Make sure your adapter has setOnItemLongClickListener method
        // If not, you'll need to update the adapter first
        try {
            adapter.setOnItemLongClickListener(new WorkoutManageAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(int position) {
                    if (position >= 0 && position < filteredList.size()) {
                        Workout workout = filteredList.get(position);
                        showLongPressOptions(workout);
                    }
                }
            });
        } catch (Exception e) {
            // Adapter doesn't have long click listener yet
            // We'll handle this when we update the adapter
        }
    }

    // METHOD FOR SWIPE DELETE
    private void showSwipeDeleteConfirmation(Workout workout, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Workout");
        builder.setMessage("Delete '" + workout.getName() + "'?");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteWorkout(workout);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Undo the swipe (restore item)
            adapter.notifyItemChanged(position);
        });

        builder.setOnCancelListener(dialog -> {
            // If user cancels by touching outside, restore item
            adapter.notifyItemChanged(position);
        });

        builder.show();
    }

    // METHOD FOR SWIPE TOGGLE COMPLETION
    private void toggleWorkoutCompletionWithSwipe(Workout workout, int position) {
        boolean newState = !workout.isCompleted();
        workout.setCompleted(newState);

        // Update UI immediately
        adapter.notifyItemChanged(position);

        // Show toast message
        String message = newState ?
                "✓ " + workout.getName() + " marked as completed" :
                "○ " + workout.getName() + " marked as incomplete";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        // Update in Firebase
        FirebaseWorkoutHelper.toggleWorkoutCompletion(workout.getId(), newState,
                new FirebaseWorkoutHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Success
                    }

                    @Override
                    public void onFailure(String error) {
                        // Revert on failure
                        workout.setCompleted(!newState);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(getContext(),
                                "Failed to update: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // METHOD FOR LONG PRESS OPTIONS
    private void showLongPressOptions(Workout workout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Workout Options");
        builder.setMessage("Choose an action for '" + workout.getName() + "'");

        builder.setPositiveButton("Edit", (dialog, which) -> {
            editWorkoutWithDialog(workout);
        });

        builder.setNeutralButton("Share", (dialog, which) -> {
            shareWorkoutViaSMS(workout);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void openWorkoutDetails(Workout workout) {
        // Create intent to launch WorkoutDetailsActivity
        Intent intent = new Intent(getActivity(), WorkoutDetailActivity.class);
        intent.putExtra("workout", workout);
        startActivity(intent);

        // Optional: Add transition animation
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    // EDIT WORKOUT METHOD
    private void editWorkoutWithDialog(Workout workout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Workout");

        // Check if layout file exists
        try {
            // Inflate the edit dialog layout
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_workout, null);

            // Get references to input fields
            EditText etName = dialogView.findViewById(R.id.etEditName);
            EditText etDuration = dialogView.findViewById(R.id.etEditDuration);
            EditText etExerciseCount = dialogView.findViewById(R.id.etEditExerciseCount);
            EditText etEquipment = dialogView.findViewById(R.id.etEditEquipment);
            EditText etInstructions = dialogView.findViewById(R.id.etEditInstructions);

            // Pre-fill with current workout data
            etName.setText(workout.getName());
            etDuration.setText(String.valueOf(workout.getDuration()));
            etExerciseCount.setText(String.valueOf(workout.getExerciseCount()));

            // Convert equipment list to comma-separated string
            if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
                StringBuilder equipmentStr = new StringBuilder();
                for (int i = 0; i < workout.getEquipment().size(); i++) {
                    equipmentStr.append(workout.getEquipment().get(i));
                    if (i < workout.getEquipment().size() - 1) {
                        equipmentStr.append(", ");
                    }
                }
                etEquipment.setText(equipmentStr.toString());
            }

            etInstructions.setText(workout.getInstructions() != null ? workout.getInstructions() : "");

            builder.setView(dialogView);

            builder.setPositiveButton("Save Changes", (dialog, which) -> {
                // Validate and save changes
                if (validateEditInputs(etName, etDuration, etExerciseCount, etEquipment)) {
                    updateWorkoutInFirebase(workout, etName, etDuration,
                            etExerciseCount, etEquipment, etInstructions);
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            });

            // Add delete option
            builder.setNeutralButton("Delete", (dialog, which) -> {
                showDeleteConfirmation(workout);
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            // Customize button colors
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.errorRed));

        } catch (Exception e) {
            // If layout file doesn't exist yet, show simple dialog
            Toast.makeText(getContext(), "Edit feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    // VALIDATION METHOD
    private boolean validateEditInputs(EditText etName, EditText etDuration,
                                       EditText etExerciseCount, EditText etEquipment) {

        boolean isValid = true;

        // Validate workout name
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Workout name is required");
            etName.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            etName.setError("Name must be at least 3 characters");
            etName.requestFocus();
            isValid = false;
        }

        // Validate duration
        String durationStr = etDuration.getText().toString().trim();
        if (durationStr.isEmpty()) {
            etDuration.setError("Duration is required");
            etDuration.requestFocus();
            isValid = false;
        } else {
            try {
                int duration = Integer.parseInt(durationStr);
                if (duration <= 0) {
                    etDuration.setError("Duration must be greater than 0");
                    etDuration.requestFocus();
                    isValid = false;
                } else if (duration > 300) {
                    etDuration.setError("Duration seems too long (max 300 minutes)");
                    etDuration.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etDuration.setError("Please enter a valid number");
                etDuration.requestFocus();
                isValid = false;
            }
        }

        // Validate exercise count
        String exerciseCountStr = etExerciseCount.getText().toString().trim();
        if (exerciseCountStr.isEmpty()) {
            etExerciseCount.setError("Exercise count is required");
            etExerciseCount.requestFocus();
            isValid = false;
        } else {
            try {
                int exerciseCount = Integer.parseInt(exerciseCountStr);
                if (exerciseCount <= 0) {
                    etExerciseCount.setError("Must have at least 1 exercise");
                    etExerciseCount.requestFocus();
                    isValid = false;
                } else if (exerciseCount > 50) {
                    etExerciseCount.setError("Too many exercises (max 50)");
                    etExerciseCount.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                etExerciseCount.setError("Please enter a valid number");
                etExerciseCount.requestFocus();
                isValid = false;
            }
        }

        // Validate equipment
        String equipmentStr = etEquipment.getText().toString().trim();
        if (equipmentStr.isEmpty()) {
            etEquipment.setError("Please enter required equipment");
            etEquipment.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    // METHOD TO UPDATE WORKOUT IN FIREBASE
    private void updateWorkoutInFirebase(Workout workout, EditText etName,
                                         EditText etDuration, EditText etExerciseCount,
                                         EditText etEquipment, EditText etInstructions) {

        // Get updated values
        String name = etName.getText().toString().trim();
        int duration = Integer.parseInt(etDuration.getText().toString().trim());
        int exerciseCount = Integer.parseInt(etExerciseCount.getText().toString().trim());
        String equipmentStr = etEquipment.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        // Parse equipment list
        List<String> equipmentList = new ArrayList<>();
        if (!equipmentStr.isEmpty()) {
            String[] items = equipmentStr.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    equipmentList.add(trimmed);
                }
            }
        }

        // Update workout object
        workout.setName(name);
        workout.setDuration(duration);
        workout.setExerciseCount(exerciseCount);
        workout.setEquipment(equipmentList);
        workout.setInstructions(instructions);

        // Show loading
        Toast.makeText(getContext(), "Updating workout...", Toast.LENGTH_SHORT).show();

        // Update in Firebase
        FirebaseWorkoutHelper.updateWorkout(workout, new FirebaseWorkoutHelper.OnCompleteListener() {
            @Override
            public void onSuccess() {
                // Success - update UI
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(),
                        "✓ Workout updated successfully!",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                // Failure - show error
                Toast.makeText(getContext(),
                        "Failed to update workout: " + error,
                        Toast.LENGTH_LONG).show();

                // Refresh data to show original values
                loadWorkouts();
            }
        });
    }

    // SMS Methods
    private void shareWorkoutViaSMS(Workout workout) {
        // Build the SMS message
        StringBuilder smsBody = new StringBuilder();

        // Header
        smsBody.append("🏋️ FitLife Workout Checklist 🏋️\n");
        smsBody.append("═══════════════════════════\n\n");

        // Workout details
        smsBody.append("📋 WORKOUT DETAILS:\n");
        smsBody.append("• Name: ").append(workout.getName()).append("\n");
        smsBody.append("• Duration: ").append(workout.getDuration()).append(" minutes\n");
        smsBody.append("• Exercises: ").append(workout.getExerciseCount()).append("\n");
        smsBody.append("• Status: ").append(workout.isCompleted() ? "✅ Completed" : "⏳ Pending").append("\n\n");

        // Equipment checklist
        smsBody.append("📦 EQUIPMENT CHECKLIST:\n");
        if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
            for (String equipment : workout.getEquipment()) {
                smsBody.append("□ ").append(equipment).append("\n");
            }
        } else {
            smsBody.append("• No equipment needed\n");
        }
        smsBody.append("\n");

        // Instructions
        if (workout.getInstructions() != null && !workout.getInstructions().isEmpty()) {
            smsBody.append("📝 INSTRUCTIONS:\n");
            smsBody.append(workout.getInstructions()).append("\n\n");
        }

        // Footer
        smsBody.append("💪 Stay consistent!\n");
        smsBody.append("Sent via FitLife App");

        // Create SMS intent
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse("smsto:")); // Opens default SMS app
        smsIntent.putExtra("sms_body", smsBody.toString());

        // Check if there's an app to handle SMS
        if (smsIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(smsIntent);
        } else {
            // If no SMS app is available, show an alternative
            showAlternativeSharingOptions(workout, smsBody.toString());
        }
    }

    private void showAlternativeSharingOptions(Workout workout, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Share Workout Checklist");
        builder.setMessage("No SMS app found. Would you like to share via other apps?");

        builder.setPositiveButton("Share via Other Apps", (dialog, which) -> {
            // Create a generic share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "FitLife Workout: " + workout.getName());
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(shareIntent, "Share Workout Checklist"));
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Update the completion method to be reusable
    private void updateWorkoutCompletion(Workout workout, boolean isChecked) {
        workout.setCompleted(isChecked);
        String message = isChecked ? "Marked as completed" : "Marked as incomplete";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

        // Update in Firebase
        FirebaseWorkoutHelper.toggleWorkoutCompletion(workout.getId(), isChecked,
                new FirebaseWorkoutHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Success - already updated locally
                    }

                    @Override
                    public void onFailure(String error) {
                        // Revert on failure
                        workout.setCompleted(!isChecked);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Failed to update: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadWorkouts() {
        // Load from Firebase Database
        FirebaseWorkoutHelper.getAllWorkouts(new FirebaseWorkoutHelper.OnWorkoutsLoadedListener() {
            @Override
            public void onWorkoutsLoaded(List<Workout> workouts) {
                workoutList.clear();
                workoutList.addAll(workouts);

                // Initialize filtered list
                filteredList.clear();
                filteredList.addAll(workoutList);

                // Update UI
                updateUI();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(),
                        "Failed to load workouts: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update the existing updateUI() method to call updateStats()
    private void updateUI() {
        if (filteredList.isEmpty()) {
            llEmptyWorkouts.setVisibility(View.VISIBLE);
            rvAllWorkouts.setVisibility(View.GONE);
        } else {
            llEmptyWorkouts.setVisibility(View.GONE);
            rvAllWorkouts.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

        // Update statistics
        updateStats();
    }

    private void setupSearch() {
        etSearchWorkout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter workouts
                filterWorkouts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void filterWorkouts(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            // Show all workouts if search is empty
            filteredList.addAll(workoutList);
        } else {
            // Filter by workout name
            String lowerQuery = query.toLowerCase();
            for (Workout workout : workoutList) {
                if (workout.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(workout);
                }
            }
        }

        updateUI();
    }

    // Add this new method to update stats
    private void updateStats() {
        int total = workoutList.size();
        int completed = 0;
        int pending = 0;

        for (Workout workout : workoutList) {
            if (workout.isCompleted()) {
                completed++;
            } else {
                pending++;
            }
        }

        // Update stat TextViews
        if (tvTotalWorkouts != null) {
            tvTotalWorkouts.setText(String.valueOf(total));
        }
        if (tvCompletedWorkouts != null) {
            tvCompletedWorkouts.setText(String.valueOf(completed));
        }
        if (tvPendingWorkouts != null) {
            tvPendingWorkouts.setText(String.valueOf(pending));
        }

        // Show/hide stats card based on whether there are workouts
        if (cvStatsSummary != null) {
            cvStatsSummary.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
        }

        // Show/hide section header
        if (tvSectionHeader != null) {
            tvSectionHeader.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
        }
    }
    private void showDeleteConfirmation(Workout workout) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete '" + workout.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteWorkout(workout);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWorkout(Workout workout) {
        FirebaseWorkoutHelper.deleteWorkout(workout.getId(),
                new FirebaseWorkoutHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Remove from lists
                        workoutList.remove(workout);
                        filteredList.remove(workout);

                        // Update UI
                        adapter.notifyDataSetChanged();
                        updateUI();

                        Toast.makeText(getContext(), "Workout deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(getContext(),
                                "Failed to delete workout: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupFAB() {
        fabAddWorkout.setOnClickListener(v -> {
            // Navigate to Create tab
            if (getActivity() instanceof DashboardActivity) {
                ((DashboardActivity) getActivity()).navigateToCreateTab();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadWorkouts();
    }
}