package com.example.lifefit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class WorkoutDashboardAdapter extends RecyclerView.Adapter<WorkoutDashboardAdapter.WorkoutViewHolder> {

    private List<Workout> workouts;
    private OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
    }

    public WorkoutDashboardAdapter(List<Workout> workouts, OnWorkoutClickListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_dashboard, parent, false);
        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout, listener);
    }

    @Override
    public int getItemCount() {
        return workouts != null ? workouts.size() : 0;
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        CardView cvWorkoutCard;
        TextView tvWorkoutName;
        TextView tvWorkoutDetails;
        TextView tvExerciseCount;
        LinearLayout llEquipmentContainer;
        ChipGroup chipGroupEquipment;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            cvWorkoutCard = itemView.findViewById(R.id.cvWorkoutCard);
            tvWorkoutName = itemView.findViewById(R.id.tvWorkoutName);
            tvWorkoutDetails = itemView.findViewById(R.id.tvWorkoutDetails);
            tvExerciseCount = itemView.findViewById(R.id.tvExerciseCount);
            llEquipmentContainer = itemView.findViewById(R.id.llEquipmentContainer);
            chipGroupEquipment = itemView.findViewById(R.id.chipGroupEquipment);
        }

        public void bind(Workout workout, OnWorkoutClickListener listener) {
            if (workout == null) return;

            // Set workout name
            tvWorkoutName.setText(workout.getName());

            // Set duration
            tvWorkoutDetails.setText(workout.getDuration() + " min");

            // Set exercise count
            tvExerciseCount.setText(workout.getExerciseCount() + " ex");

            // Clear existing chips
            chipGroupEquipment.removeAllViews();

            // Add equipment chips dynamically
            if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
                llEquipmentContainer.setVisibility(View.VISIBLE);

                for (String equipment : workout.getEquipment()) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(equipment);
                    chip.setTextSize(11f);
                    chip.setClickable(false);
                    chip.setCheckable(false);

                    try {
                        chip.setChipBackgroundColorResource(R.color.chipBackground);
                        chip.setTextColor(itemView.getContext().getResources()
                                .getColor(R.color.colorPrimaryDark));
                    } catch (Exception e) {
                        // Use defaults if colors don't exist
                    }

                    chip.setChipMinHeight(28f);
                    chipGroupEquipment.addView(chip);
                }
            } else {
                llEquipmentContainer.setVisibility(View.GONE);
            }

            // Set click listener for entire card
            cvWorkoutCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onWorkoutClick(workout);
                    }
                }
            });
        }
    }
}