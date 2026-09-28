package com.example.lifefit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class WorkoutManageAdapter extends RecyclerView.Adapter<WorkoutManageAdapter.ViewHolder> {

    private List<Workout> workouts;
    private OnWorkoutActionListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnWorkoutActionListener {
        void onWorkoutClick(Workout workout);
        void onEditClick(Workout workout);
        void onDeleteClick(Workout workout);
        void onCheckChanged(Workout workout, boolean isChecked);
        void onShareClick(Workout workout);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public WorkoutManageAdapter(List<Workout> workouts, OnWorkoutActionListener listener) {
        this.workouts = workouts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout, listener, position, longClickListener);
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cvWorkoutManage;
        CheckBox cbWorkoutComplete;
        TextView tvManageWorkoutName;
        TextView tvManageWorkoutDetails;
        TextView tvExerciseCount;
        LinearLayout llEquipmentSection;
        ChipGroup chipGroupManageEquipment;
        LinearLayout llShareAction;
        LinearLayout llEditAction;
        LinearLayout llDeleteAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvWorkoutManage = itemView.findViewById(R.id.cvWorkoutManage);
            cbWorkoutComplete = itemView.findViewById(R.id.cbWorkoutComplete);
            tvManageWorkoutName = itemView.findViewById(R.id.tvManageWorkoutName);
            tvManageWorkoutDetails = itemView.findViewById(R.id.tvManageWorkoutDetails);
            tvExerciseCount = itemView.findViewById(R.id.tvExerciseCount);
            llEquipmentSection = itemView.findViewById(R.id.llEquipmentSection);
            chipGroupManageEquipment = itemView.findViewById(R.id.chipGroupManageEquipment);
            llShareAction = itemView.findViewById(R.id.llShareAction);
            llEditAction = itemView.findViewById(R.id.llEditAction);
            llDeleteAction = itemView.findViewById(R.id.llDeleteAction);
        }

        public void bind(Workout workout, OnWorkoutActionListener listener,
                         int position, OnItemLongClickListener longClickListener) {

            // Set workout name
            tvManageWorkoutName.setText(workout.getName());

            // Set duration details
            tvManageWorkoutDetails.setText(workout.getDuration() + " min");

            // Set exercise count
            tvExerciseCount.setText(workout.getExerciseCount() + " ex");

            // Set checkbox state
            cbWorkoutComplete.setChecked(workout.isCompleted());

            // Handle equipment chips with enhanced styling
            if (workout.getEquipment() != null && !workout.getEquipment().isEmpty()) {
                llEquipmentSection.setVisibility(View.VISIBLE);
                chipGroupManageEquipment.removeAllViews();

                for (String equipment : workout.getEquipment()) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(equipment);
                    chip.setTextSize(12f);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    chip.setChipBackgroundColorResource(R.color.chipBackground);
                    chip.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimaryDark));
                    chip.setChipMinHeight(32f);
                    chipGroupManageEquipment.addView(chip);
                }
            } else {
                llEquipmentSection.setVisibility(View.GONE);
            }

            // Card click listener (view details)
            cvWorkoutManage.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutClick(workout);
                }
            });

            // Long press gesture
            cvWorkoutManage.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(position);
                    return true;
                }
                return false;
            });

            // Checkbox listener
            cbWorkoutComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && buttonView.isPressed()) {
                    listener.onCheckChanged(workout, isChecked);
                }
            });

            // Share action
            llShareAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShareClick(workout);
                }
            });

            // Edit action
            llEditAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(workout);
                }
            });

            // Delete action
            llDeleteAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(workout);
                }
            });
        }
    }
}