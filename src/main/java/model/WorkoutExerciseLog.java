package model;

import logic.WeightInputParser;

/**
 * Én øvelse under en aktiv træning — plan + sidste + PB + det du logger nu.
 */
public class WorkoutExerciseLog {

    private final String exerciseName;
    private final String plannedTag;
    private int plannedSets;
    private String plannedReps;
    private float plannedWeightKg;

    private int lastSets;
    private String lastReps;
    private float lastWeightKg;
    private boolean hasLast;

    private float pbWeightKg;
    private String pbReps;
    private boolean hasPb;

    private int currentSets;
    private String currentReps;
    private float currentWeightKg;
    private boolean plannedBodyweight;
    private boolean currentBodyweight;
    private boolean lastBodyweight;

    public WorkoutExerciseLog(ProgramExercise planned) {
        this.exerciseName = planned.getExerciseName();
        this.plannedTag = planned.getTag() != null ? planned.getTag() : "";
        this.plannedSets = planned.getSets();
        this.plannedReps = planned.getReps();
        this.plannedWeightKg = planned.getWeightKg();
        this.plannedBodyweight = planned.isBodyweight();
        this.currentSets = planned.getSets();
        this.currentReps = planned.getReps();
        this.currentWeightKg = planned.getWeightKg() > 0 ? planned.getWeightKg() : -1f;
        this.currentBodyweight = planned.isBodyweight();
    }

    public String getExerciseName() { return exerciseName; }
    public String getPlannedTag() { return plannedTag; }
    public int getPlannedSets() { return plannedSets; }
    public String getPlannedReps() { return plannedReps; }
    public float getPlannedWeightKg() { return plannedWeightKg; }

    public int getLastSets() { return lastSets; }
    public String getLastReps() { return lastReps; }
    public float getLastWeightKg() { return lastWeightKg; }
    public boolean hasLast() { return hasLast; }

    public float getPbWeightKg() { return pbWeightKg; }
    public String getPbReps() { return pbReps; }
    public boolean hasPb() { return hasPb; }

    public int getCurrentSets() { return currentSets; }
    public String getCurrentReps() { return currentReps; }
    public float getCurrentWeightKg() { return currentWeightKg; }

    public void setLast(int sets, String reps, float weightKg, boolean bodyweight) {
        this.lastSets = sets;
        this.lastReps = reps != null ? reps : "";
        this.lastWeightKg = weightKg;
        this.lastBodyweight = bodyweight;
        this.hasLast = true;
    }

    public void setPb(float weightKg, String reps) {
        this.pbWeightKg = weightKg;
        this.pbReps = reps != null ? reps : "";
        this.hasPb = weightKg > 0;
    }

    public void setCurrentSets(int currentSets) { this.currentSets = currentSets; }
    public void setCurrentReps(String currentReps) { this.currentReps = currentReps != null ? currentReps : ""; }
    public void setCurrentWeightKg(float currentWeightKg) { this.currentWeightKg = currentWeightKg; }
    public boolean isCurrentBodyweight() { return currentBodyweight; }
    public boolean isPlannedBodyweight() { return plannedBodyweight; }

    public void applyCurrentWeightInput(WeightInputParser.Result parsed) {
        if (!parsed.parsed) {
            return;
        }
        if (parsed.cleared) {
            currentWeightKg = -1f;
            currentBodyweight = false;
            return;
        }
        currentWeightKg = parsed.kg;
        currentBodyweight = parsed.bodyweight;
    }

    public boolean isLoggable() {
        return plannedSets > 0 && !exerciseName.startsWith("──");
    }

    public String formatLast() {
        if (!hasLast) {
            return "—";
        }
        return formatPerformance(lastSets, lastReps, lastWeightKg, lastBodyweight);
    }

    public String formatPb() {
        if (!hasPb) {
            return "—";
        }
        return formatPerformance(-1, pbReps, pbWeightKg, false);
    }

    public String formatCurrent() {
        return formatPerformance(currentSets, currentReps, currentWeightKg, currentBodyweight);
    }

    private String formatPerformance(int sets, String reps, float weight, boolean bodyweight) {
        String w = WeightInputParser.formatDisplay(bodyweight, weight);
        if (sets > 0) {
            return sets + " x " + reps + " @ " + w;
        }
        return reps + " @ " + w;
    }
}
