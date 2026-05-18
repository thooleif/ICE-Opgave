package model;

public class ProgramExercise {
    private String exerciseName;
    private int sets;
    private String reps;
    private int restTimeSec;
    private String tag;

    public ProgramExercise(String exerciseName, int sets, String reps, int restTimeSec) {
        this(exerciseName, sets, reps, restTimeSec, "");
    }

    public ProgramExercise(String exerciseName, int sets, String reps, int restTimeSec, String tag) {
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.restTimeSec = restTimeSec;
        this.tag = tag;
    }

    public String getExerciseName() { return exerciseName; }
    public int getSets() { return sets; }
    public String getReps() { return reps; }
    public int getRestTimeSec() { return restTimeSec; }
    public String getTag() { return tag; }

    @Override
    public String toString() {
        String label = tag.isEmpty() ? "" : " [" + tag + "]";
        if (restTimeSec == 0) {
            return exerciseName + "  —  " + sets + " x " + reps + label;
        }
        return exerciseName + "  —  " + sets + " x " + reps + "  (hvil: " + restTimeSec + "s)" + label;
    }
}
