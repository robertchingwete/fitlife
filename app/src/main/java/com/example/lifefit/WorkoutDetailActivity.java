package com.example.lifefit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import androidx.cardview.widget.CardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class WorkoutDetailActivity extends AppCompatActivity {

    private static final String TAG = "WorkoutDetailActivity";

    // Views
    private ImageView ivBack;
    private TextView tvWorkoutName;
    private TextView tvWorkoutDuration;
    private TextView tvWorkoutExercises;
    private TextView tvInstructions;
    private ChipGroup chipGroupEquipment;
    private CardView cvInstructions; // Now using CardView
    private Button btnSendEquipmentList;
    private Button btnSendFullDetails;
    private Button btnSendReminder;

    // Data
    private Workout workout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_workout_detail);

            // Get workout from intent
            workout = (Workout) getIntent().getSerializableExtra("workout");

            if (workout == null) {
                Log.e(TAG, "Workout is null - not passed correctly from previous activity");
                Toast.makeText(this, "Error: Workout not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Workout loaded: " + workout.getName());

            // Initialize views
            if (!initViews()) {
                Log.e(TAG, "Failed to initialize views");
                Toast.makeText(this, "Error loading workout details", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Setup workout data
            setupWorkoutData();

            // Setup button listeners
            setupButtonListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean initViews() {
        try {
            ivBack = findViewById(R.id.ivBack);
            tvWorkoutName = findViewById(R.id.tvWorkoutName);
            tvWorkoutDuration = findViewById(R.id.tvWorkoutDuration);
            tvWorkoutExercises = findViewById(R.id.tvWorkoutExercises);
            tvInstructions = findViewById(R.id.tvInstructions);
            chipGroupEquipment = findViewById(R.id.chipGroupEquipment);
            cvInstructions = findViewById(R.id.cvInstructions); // CardView
            btnSendEquipmentList = findViewById(R.id.btnSendEquipmentList);
            btnSendFullDetails = findViewById(R.id.btnSendFullDetails);
            btnSendReminder = findViewById(R.id.btnSendReminder);

            // Check if any critical views are null
            if (ivBack == null || tvWorkoutName == null || tvWorkoutDuration == null) {
                Log.e(TAG, "One or more critical views are null");
                return false;
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            return false;
        }
    }

    private void setupWorkoutData() {
        try {
            Log.d(TAG, "Setting up workout data for: " + workout.getName());

            // Set workout name
            if (workout.getName() != null && !workout.getName().isEmpty()) {
                tvWorkoutName.setText(workout.getName());
                Log.d(TAG, "Workout name set: " + workout.getName());
            } else {
                tvWorkoutName.setText("Unnamed Workout");
                Log.w(TAG, "Workout has no name");
            }

            // Set duration
            Log.d(TAG, "Duration: " + workout.getDuration());
            tvWorkoutDuration.setText(workout.getDuration() + " min");

            // Set exercise count
            Log.d(TAG, "Exercise count: " + workout.getExerciseCount());
            tvWorkoutExercises.setText(workout.getExerciseCount() + " exercises");

            // Set instructions (hide section if no instructions)
            if (workout.getInstructions() != null && !workout.getInstructions().isEmpty()) {
                tvInstructions.setText(workout.getInstructions());
                cvInstructions.setVisibility(View.VISIBLE);
                Log.d(TAG, "Instructions set and visible");
            } else {
                cvInstructions.setVisibility(View.GONE);
                Log.d(TAG, "No instructions, section hidden");
            }

            // Setup equipment chips
            Log.d(TAG, "Setting up equipment chips");
            setupEquipmentChips();

            Log.d(TAG, "Workout data setup completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up workout data: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(this, "Error displaying workout data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupEquipmentChips() {
        try {
            Log.d(TAG, "Clearing existing chips");
            chipGroupEquipment.removeAllViews();

            if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
                Log.d(TAG, "Adding " + workout.getEquipment().size() + " equipment chips");
                for (String equipment : workout.getEquipment()) {
                    if (equipment != null && !equipment.trim().isEmpty()) {
                        Log.d(TAG, "Adding chip: " + equipment);
                        Chip chip = new Chip(this);
                        chip.setText(equipment);
                        chip.setClickable(false);
                        chip.setCheckable(false);

                        try {
                            // Enhanced chip styling
                            chip.setChipBackgroundColorResource(R.color.chipBackground);
                            chip.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            chip.setTextSize(14f);
                            chip.setChipMinHeight(40f);
                            chip.setPadding(16, 8, 16, 8);
                        } catch (Exception colorException) {
                            Log.w(TAG, "Could not set chip colors, using defaults: " + colorException.getMessage());
                            // Use default colors if custom colors don't exist
                        }

                        chipGroupEquipment.addView(chip);
                    }
                }
                Log.d(TAG, "All equipment chips added successfully");
            } else {
                Log.d(TAG, "No equipment for this workout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up equipment chips: " + e.getMessage(), e);
            e.printStackTrace();
            // Don't crash, just log the error
        }
    }

    private void setupButtonListeners() {
        // Back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Send Equipment List via SMS
        btnSendEquipmentList.setOnClickListener(v -> shareEquipmentListViaSMS());

        // Send Full Details via SMS
        btnSendFullDetails.setOnClickListener(v -> shareFullWorkoutViaSMS());

        // Send Workout Reminder via SMS
        btnSendReminder.setOnClickListener(v -> shareWorkoutReminderViaSMS());
    }

    private void shareEquipmentListViaSMS() {
        try {
            StringBuilder smsBody = new StringBuilder();

            // Build equipment list message
            smsBody.append("🏋️ FitLife Equipment Checklist 🏋️\n");
            smsBody.append("═══════════════════════════════\n\n");
            smsBody.append("For workout: ").append(workout.getName()).append("\n\n");
            smsBody.append("📦 EQUIPMENT TO BRING:\n");

            if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
                for (String equipment : workout.getEquipment()) {
                    smsBody.append("□ ").append(equipment).append("\n");
                }
            } else {
                smsBody.append("• No equipment needed\n");
            }

            smsBody.append("\n💪 Don't forget anything!\n");
            smsBody.append("Sent via FitLife App");

            sendSMS(smsBody.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error sharing equipment list: " + e.getMessage(), e);
            Toast.makeText(this, "Error creating SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFullWorkoutViaSMS() {
        try {
            StringBuilder smsBody = new StringBuilder();

            // Build full workout details message
            smsBody.append("🏋️ FitLife Workout Details 🏋️\n");
            smsBody.append("═══════════════════════════\n\n");

            // Workout details
            smsBody.append("📋 WORKOUT DETAILS:\n");
            smsBody.append("• Name: ").append(workout.getName()).append("\n");
            smsBody.append("• Duration: ").append(workout.getDuration()).append(" minutes\n");
            smsBody.append("• Exercises: ").append(workout.getExerciseCount()).append("\n\n");

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

            smsBody.append("💪 Stay consistent!\n");
            smsBody.append("Sent via FitLife App");

            sendSMS(smsBody.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error sharing full workout: " + e.getMessage(), e);
            Toast.makeText(this, "Error creating SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareWorkoutReminderViaSMS() {
        try {
            StringBuilder smsBody = new StringBuilder();

            // Build reminder message
            smsBody.append("⏰ FitLife Workout Reminder ⏰\n");
            smsBody.append("═══════════════════════════\n\n");
            smsBody.append("Don't forget your workout today!\n\n");
            smsBody.append("📋 Scheduled Workout:\n");
            smsBody.append("• ").append(workout.getName()).append("\n");
            smsBody.append("• Duration: ").append(workout.getDuration()).append(" minutes\n");
            smsBody.append("• Exercises: ").append(workout.getExerciseCount()).append("\n\n");

            smsBody.append("📦 Remember to bring:\n");
            if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
                for (String equipment : workout.getEquipment()) {
                    smsBody.append("• ").append(equipment).append("\n");
                }
            } else {
                smsBody.append("• No equipment needed\n");
            }

            smsBody.append("\n💪 You got this! Stay motivated!\n");
            smsBody.append("Sent via FitLife App");

            sendSMS(smsBody.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error sharing reminder: " + e.getMessage(), e);
            Toast.makeText(this, "Error creating SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSMS(String message) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:"));
            smsIntent.putExtra("sms_body", message);

            if (smsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(smsIntent, "Send SMS via"));
            } else {
                Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening SMS app", Toast.LENGTH_SHORT).show();
        }
    }
}