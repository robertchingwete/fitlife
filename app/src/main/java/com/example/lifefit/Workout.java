package com.example.lifefit;

import java.io.Serializable; // ADD THIS IMPORT
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Workout implements Serializable { // ADD: implements Serializable
    private static final long serialVersionUID = 1L; // ADD: serial version UID

    private String id;
    private String name;
    private int duration;
    private int exerciseCount;
    private List<String> equipment;
    private String instructions;
    private boolean isCompleted;
    private long createdAt;
    private String imageUrl;

    private String icon; // Emoji icon
    private String difficulty; // Beginner, Intermediate,

    // Empty constructor required for Firebase
    public Workout() {
    }

    // Constructor for creating new workouts
    public Workout(String id, String name, int duration, int exerciseCount,
                   List<String> equipment, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.exerciseCount = exerciseCount;
        this.equipment = equipment;
        this.instructions = "";
        this.isCompleted = isCompleted;
        this.createdAt = System.currentTimeMillis();
        this.imageUrl = "";
        this.icon = icon != null ? icon : "💪"; // Default icon
        this.difficulty = difficulty != null ? difficulty : "Beginner"; // Default
    }

    // Constructor with instructions
    public Workout(String id, String name, int duration, int exerciseCount,
                   List<String> equipment, String instructions, boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.exerciseCount = exerciseCount;
        this.equipment = equipment;
        this.instructions = instructions;
        this.isCompleted = isCompleted;
        this.createdAt = System.currentTimeMillis();
        this.imageUrl = "";
    }

    // Constructor with imageUrl
    public Workout(String id, String name, int duration, int exerciseCount,
                   List<String> equipment, String instructions, boolean isCompleted, String imageUrl) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.exerciseCount = exerciseCount;
        this.equipment = equipment;
        this.instructions = instructions;
        this.isCompleted = isCompleted;
        this.createdAt = System.currentTimeMillis();
        this.imageUrl = imageUrl;
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("duration", duration);
        map.put("exerciseCount", exerciseCount);
        map.put("equipment", equipment);
        map.put("instructions", instructions);
        map.put("isCompleted", isCompleted);
        map.put("createdAt", createdAt);
        map.put("imageUrl", imageUrl);
        map.put("icon", icon);
        map.put("difficulty", difficulty);
        return map;
    }

    // Create from Firebase DataSnapshot
    public static Workout fromMap(Map<String, Object> map) {
        Workout workout = new Workout();
        workout.id = (String) map.get("id");
        workout.name = (String) map.get("name");


        // Add to fromMap()
        workout.icon = map.get("icon") != null ? (String) map.get("icon") : "💪";
        workout.difficulty = map.get("difficulty") != null ? (String) map.get("difficulty") : "Beginner";


        // Handle number types
        Object durationObj = map.get("duration");
        if (durationObj instanceof Long) {
            workout.duration = ((Long) durationObj).intValue();
        } else {
            workout.duration = (Integer) durationObj;
        }

        Object exerciseCountObj = map.get("exerciseCount");
        if (exerciseCountObj instanceof Long) {
            workout.exerciseCount = ((Long) exerciseCountObj).intValue();
        } else {
            workout.exerciseCount = (Integer) exerciseCountObj;
        }

        // Handle equipment list
        Object equipmentObj = map.get("equipment");
        if (equipmentObj instanceof List) {
            workout.equipment = (List<String>) equipmentObj;
        } else {
            workout.equipment = new ArrayList<>();
        }

        workout.instructions = (String) map.get("instructions");
        workout.isCompleted = (Boolean) map.get("isCompleted");

        Object createdAtObj = map.get("createdAt");
        if (createdAtObj instanceof Long) {
            workout.createdAt = (Long) createdAtObj;
        } else {
            workout.createdAt = System.currentTimeMillis();
        }

        // Handle imageUrl
        Object imageUrlObj = map.get("imageUrl");
        if (imageUrlObj instanceof String) {
            workout.imageUrl = (String) imageUrlObj;
        } else {
            workout.imageUrl = "";
        }

        return workout;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getDuration() { return duration; }
    public int getExerciseCount() { return exerciseCount; }
    public List<String> getEquipment() { return equipment; }
    public String getInstructions() { return instructions; }
    public boolean isCompleted() { return isCompleted; }
    public long getCreatedAt() { return createdAt; }
    public String getImageUrl() { return imageUrl; }

    // Add getters and setters
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setExerciseCount(int exerciseCount) { this.exerciseCount = exerciseCount; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
}