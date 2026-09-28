// WorkoutTemplateAdapter.java
        package com.example.lifefit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class WorkoutTemplateAdapter extends RecyclerView.Adapter<WorkoutTemplateAdapter.ViewHolder> {

    private List<WorkoutTemplate> templates;
    private OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(WorkoutTemplate template);
    }

    public WorkoutTemplateAdapter(List<WorkoutTemplate> templates, OnTemplateClickListener listener) {
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_template, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutTemplate template = templates.get(position);
        holder.bind(template, listener);
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cvTemplate;
        TextView tvTemplateIcon;
        TextView tvDifficulty;
        LinearLayout llDifficultyBadge;
        TextView tvTemplateName;
        TextView tvTemplateDescription;
        TextView tvTemplateDuration;
        TextView tvTemplateExercises;
        LinearLayout llEquipmentPreview;
        ChipGroup chipGroupTemplateEquipment;
        Button btnUseTemplate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cvTemplate = itemView.findViewById(R.id.cvTemplate);
            tvTemplateIcon = itemView.findViewById(R.id.tvTemplateIcon);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            llDifficultyBadge = itemView.findViewById(R.id.llDifficultyBadge);
            tvTemplateName = itemView.findViewById(R.id.tvTemplateName);
            tvTemplateDescription = itemView.findViewById(R.id.tvTemplateDescription);
            tvTemplateDuration = itemView.findViewById(R.id.tvTemplateDuration);
            tvTemplateExercises = itemView.findViewById(R.id.tvTemplateExercises);
            llEquipmentPreview = itemView.findViewById(R.id.llEquipmentPreview);
            chipGroupTemplateEquipment = itemView.findViewById(R.id.chipGroupTemplateEquipment);
            btnUseTemplate = itemView.findViewById(R.id.btnUseTemplate);
        }

        public void bind(WorkoutTemplate template, OnTemplateClickListener listener) {
            tvTemplateIcon.setText(template.getIcon());
            tvTemplateName.setText(template.getName());
            tvTemplateDescription.setText(template.getDescription());
            tvDifficulty.setText(template.getDifficulty());
            tvTemplateDuration.setText(template.getDuration() + " min");
            tvTemplateExercises.setText(template.getExercises() + " exercises");

            // Set difficulty badge color
            llDifficultyBadge.setBackgroundResource(template.getDifficultyColor());

            // Setup equipment chips
            if (template.getEquipment() != null && !template.getEquipment().isEmpty()) {
                llEquipmentPreview.setVisibility(View.VISIBLE);
                chipGroupTemplateEquipment.removeAllViews();

                for (String equipment : template.getEquipment()) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(equipment);
                    chip.setTextSize(12f);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    chip.setChipBackgroundColorResource(R.color.chipBackground);
                    chip.setTextColor(itemView.getContext().getResources()
                            .getColor(R.color.colorPrimaryDark));
                    chipGroupTemplateEquipment.addView(chip);
                }
            } else {
                llEquipmentPreview.setVisibility(View.GONE);
            }

            btnUseTemplate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTemplateClick(template);
                }
            });
        }
    }
}