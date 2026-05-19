package model;

import java.util.ArrayList;
import java.util.List;

public class TrainingDay {
    private String dayName;
    private String focus;
    private List<ProgramExercise> exercises;

    public TrainingDay(String dayName, String focus, List<ProgramExercise> exercises) {
        this.dayName = dayName;
        this.focus = focus;
        this.exercises = exercises != null ? new ArrayList<>(exercises) : new ArrayList<>();
    }

    public String getDayName() { return dayName; }
    public String getFocus() { return focus; }
    public List<ProgramExercise> getExercises() { return exercises; }

    public void setDayName(String dayName) { this.dayName = dayName; }
    public void setFocus(String focus) { this.focus = focus; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ┌─────────────────────────────────────────\n");
        sb.append("  │ ").append(dayName).append(" — ").append(focus).append("\n");
        sb.append("  ├─────────────────────────────────────────\n");
        for (int i = 0; i < exercises.size(); i++) {
            sb.append("  │  ").append(i + 1).append(". ").append(exercises.get(i)).append("\n");
        }
        sb.append("  └─────────────────────────────────────────\n");
        return sb.toString();
    }
}
