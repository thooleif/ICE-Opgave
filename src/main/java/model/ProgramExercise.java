package model;

import logic.WeightInputParser;

public class ProgramExercise {
    private String exerciseName;
    private int sets;
    private String reps;
    private int restTimeSec;
    private String tag;
    /** kg; -1 betyder ikke angivet */
    private float weightKg;
    private boolean bodyweight;

    public ProgramExercise(String exerciseName, int sets, String reps, int restTimeSec) {
        this(exerciseName, sets, reps, restTimeSec, "");
    }

    public ProgramExercise(String exerciseName, int sets, String reps, int restTimeSec, String tag) {
        this(exerciseName, sets, reps, restTimeSec, tag, -1f);
    }

    public ProgramExercise(String exerciseName, int sets, String reps, int restTimeSec, String tag, float weightKg) {
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.restTimeSec = restTimeSec;
        this.tag = tag != null ? tag : "";
        this.weightKg = weightKg;
        this.bodyweight = false;
    }

    public String getExerciseName() { return exerciseName; }
    public int getSets() { return sets; }
    public String getReps() { return reps; }
    public int getRestTimeSec() { return restTimeSec; }
    public String getTag() { return tag; }
    public float getWeightKg() { return weightKg; }
    public boolean isBodyweight() { return bodyweight; }

    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }
    public void setSets(int sets) { this.sets = sets; }
    public void setReps(String reps) { this.reps = reps; }
    public void setRestTimeSec(int restTimeSec) { this.restTimeSec = restTimeSec; }
    public void setTag(String tag) { this.tag = tag != null ? tag : ""; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
    public void setBodyweight(boolean bodyweight) { this.bodyweight = bodyweight; }

    public void applyWeightInput(WeightInputParser.Result parsed) {
        if (!parsed.parsed) {
            return;
        }
        if (parsed.cleared) {
            weightKg = -1f;
            bodyweight = false;
            return;
        }
        weightKg = parsed.kg;
        bodyweight = parsed.bodyweight;
    }

    public boolean hasWeight() {
        return bodyweight || weightKg > 0;
    }

    @Override
    public String toString() {
        String label = tag.isEmpty() ? "" : " [" + tag + "]";
        String weight = hasWeight() ? " @ " + WeightInputParser.formatDisplay(bodyweight, weightKg) : "";
        if (restTimeSec == 0) {
            return exerciseName + weight + "  —  " + sets + " x " + reps + label;
        }
        return exerciseName + weight + "  —  " + sets + " x " + reps + "  (hvil: " + restTimeSec + "s)" + label;
    }
}
