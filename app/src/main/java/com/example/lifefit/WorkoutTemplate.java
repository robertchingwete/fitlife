package com.example.lifefit;

import java.util.List;

public class WorkoutTemplate {
    private String icon;
    private String name;
    private String description;
    private String difficulty;
    private int duration;
    private int exercises;
    private List<String> equipment;
    private String instructions;

    public WorkoutTemplate(String icon, String name, String description, String difficulty,
                           int duration, int exercises, List<String> equipment, String instructions) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.duration = duration;
        this.exercises = exercises;
        this.equipment = equipment;
        this.instructions = instructions;
    }

    public String getIcon() { return icon; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDifficulty() { return difficulty; }
    public int getDuration() { return duration; }
    public int getExercises() { return exercises; }
    public List<String> getEquipment() { return equipment; }
    public String getInstructions() { return instructions; }

    public int getDifficultyColor() {
        switch (difficulty.toLowerCase()) {
            case "beginner":
                return R.color.successGreen;
            case "intermediate":
                return R.color.warningAmber;
            case "advanced":
                return R.color.errorRed;
            default:
                return R.color.colorPrimary;
        }
    }
}