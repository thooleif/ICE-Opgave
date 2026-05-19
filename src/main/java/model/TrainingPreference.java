package model;

public class TrainingPreference {
    private String focus;
    private String trainingStyle;
    private int daysPerWeek;
    private int sessionDurationMin;

    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }

    public String getTrainingStyle() { return trainingStyle; }
    public void setTrainingStyle(String trainingStyle) { this.trainingStyle = trainingStyle; }

    public int getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(int daysPerWeek) { this.daysPerWeek = daysPerWeek; }

    public int getSessionDurationMin() { return sessionDurationMin; }
    public void setSessionDurationMin(int sessionDurationMin) { this.sessionDurationMin = sessionDurationMin; }

    @Override
    public String toString() {
        return "Præferencer: " + focus + ", stil: " + trainingStyle +
               ", " + daysPerWeek + " dage/uge, " + sessionDurationMin + " min/session";
    }
}
