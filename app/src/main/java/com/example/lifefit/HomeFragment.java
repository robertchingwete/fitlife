package com.example.lifefit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HomeFragment extends Fragment {

    // Views
    private TextView tvCurrentDate;
    private TextView tvWelcomeUser;
    private TextView tvMotivationalQuote;
    private TextView tvTotalWorkouts;
    private TextView tvCompletedWorkouts;
    private TextView tvWorkoutCount;
    private TextView tvProgressPercentage;
    private ProgressBar progressBar;
    private RecyclerView rvDashboardWorkouts;
    private LinearLayout llEmptyState;
    private Button btnViewAll;
    private Button btnCreateFirstWorkout;

    // Data
    private List<Workout> workoutList;
    private WorkoutDashboardAdapter adapter;

    // Motivational quotes array
    private final String[] motivationalQuotes = {
            "Push yourself, because no one else will.",
            "The only bad workout is the one you didn't do.",
            "Your body can stand almost anything. It's your mind you have to convince.",
            "Strength doesn't come from what you can do. It comes from overcoming what you thought you couldn't.",
            "The pain you feel today will be the strength you feel tomorrow.",
            "Don't limit your challenges, challenge your limits.",
            "Success starts with self-discipline.",
            "The difference between try and triumph is a little 'umph'.",
            "Wake up with determination. Go to bed with satisfaction.",
            "Your only limit is you.",
            "Train insane or remain the same.",
            "Sweat is just fat crying.",
            "The body achieves what the mind believes.",
            "Excuses don't burn calories.",
            "Make yourself proud."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        initViews(view);

        // Set up date
        setupCurrentDate();

        // Set up welcome message with greeting
        setupWelcomeMessage();

        // Set up motivational quote
        setupMotivationalQuote();

        // Set up RecyclerView
        setupRecyclerView();

        // Load workouts
        loadWorkouts();

        // Set up button listeners
        setupButtonListeners();
    }

    private void initViews(View view) {
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate);
        tvWelcomeUser = view.findViewById(R.id.tvWelcomeUser);
        tvMotivationalQuote = view.findViewById(R.id.tvMotivationalQuote);
        tvTotalWorkouts = view.findViewById(R.id.tvTotalWorkouts);
        tvCompletedWorkouts = view.findViewById(R.id.tvCompletedWorkouts);
        tvWorkoutCount = view.findViewById(R.id.tvWorkoutCount);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);
        progressBar = view.findViewById(R.id.progressBar);
        rvDashboardWorkouts = view.findViewById(R.id.rvDashboardWorkouts);
        llEmptyState = view.findViewById(R.id.llEmptyState);
        btnViewAll = view.findViewById(R.id.btnViewAll);
        btnCreateFirstWorkout = view.findViewById(R.id.btnCreateFirstWorkout);
    }

    private void setupCurrentDate() {
        // Get current date
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);

        if (tvCurrentDate != null) {
            tvCurrentDate.setText(formattedDate);
        }
    }

    private void setupWelcomeMessage() {
        // Get user name from Firebase
        String userName = FirebaseAuthHelper.getUserName();

        // Get greeting based on time of day
        String greeting = getGreetingBasedOnTime();

        if (tvWelcomeUser != null) {
            tvWelcomeUser.setText(greeting + ", " + userName + "!");
        }
    }

    private String getGreetingBasedOnTime() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 5 && hourOfDay < 12) {
            return "Good Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            return "Good Afternoon";
        } else if (hourOfDay >= 17 && hourOfDay < 21) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
    }

    private void setupMotivationalQuote() {
        // Get random motivational quote
        Random random = new Random();
        int randomIndex = random.nextInt(motivationalQuotes.length);
        String quote = motivationalQuotes[randomIndex];

        if (tvMotivationalQuote != null) {
            tvMotivationalQuote.setText(quote);
        }
    }

    private void setupRecyclerView() {
        workoutList = new ArrayList<>();

        // Initialize adapter with click listener
        adapter = new WorkoutDashboardAdapter(workoutList, new WorkoutDashboardAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                // Navigate to workout detail
                openWorkoutDetails(workout);
            }
        });

        // Set up RecyclerView
        if (rvDashboardWorkouts != null) {
            rvDashboardWorkouts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvDashboardWorkouts.setAdapter(adapter);
            rvDashboardWorkouts.setHasFixedSize(true);
        }
    }

    private void openWorkoutDetails(Workout workout) {
        try {
            Intent intent = new Intent(getActivity(), WorkoutDetailActivity.class);
            intent.putExtra("workout", workout);
            startActivity(intent);

            // Optional: Add transition animation
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Error opening workout details: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadWorkouts() {
        // Load from Firebase Database
        FirebaseWorkoutHelper.getAllWorkouts(new FirebaseWorkoutHelper.OnWorkoutsLoadedListener() {
            @Override
            public void onWorkoutsLoaded(List<Workout> workouts) {
                workoutList.clear();
                workoutList.addAll(workouts);

                // Update UI
                if (workoutList.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    adapter.notifyDataSetChanged();
                }

                updateStats();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(),
                        "Failed to load workouts: " + error,
                        Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void updateStats() {
        int total = workoutList.size();
        int completed = 0;

        for (Workout workout : workoutList) {
            if (workout.isCompleted()) {
                completed++;
            }
        }

        // Update stat TextViews
        if (tvTotalWorkouts != null) {
            tvTotalWorkouts.setText(String.valueOf(total));
        }

        if (tvCompletedWorkouts != null) {
            tvCompletedWorkouts.setText(String.valueOf(completed));
        }

        // Update workout count text
        if (tvWorkoutCount != null) {
            String countText = total + (total == 1 ? " workout scheduled" : " workouts scheduled");
            tvWorkoutCount.setText(countText);
        }

        // Calculate and update progress percentage
        updateProgress(completed, total);
    }

    private void updateProgress(int completed, int total) {
        if (progressBar != null && tvProgressPercentage != null) {
            if (total > 0) {
                int percentage = (int) ((completed / (float) total) * 100);
                progressBar.setProgress(percentage);
                tvProgressPercentage.setText(percentage + "%");
            } else {
                progressBar.setProgress(0);
                tvProgressPercentage.setText("0%");
            }
        }
    }

    private void showEmptyState() {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
        }
        if (rvDashboardWorkouts != null) {
            rvDashboardWorkouts.setVisibility(View.GONE);
        }
        if (tvWorkoutCount != null) {
            tvWorkoutCount.setText("0 workouts scheduled");
        }

        // Set progress to 0
        updateProgress(0, 0);
    }

    private void hideEmptyState() {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
        if (rvDashboardWorkouts != null) {
            rvDashboardWorkouts.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtonListeners() {
        // View All button
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Workouts tab
                    if (getActivity() instanceof DashboardActivity) {
                        ((DashboardActivity) getActivity()).navigateToWorkoutsTab();
                    }
                }
            });
        }

        // Create First Workout button (in empty state)
        if (btnCreateFirstWorkout != null) {
            btnCreateFirstWorkout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navigate to Create tab
                    if (getActivity() instanceof DashboardActivity) {
                        ((DashboardActivity) getActivity()).navigateToCreateTab();
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        setupCurrentDate();
        setupWelcomeMessage();
        setupMotivationalQuote();
        loadWorkouts();
    }
}