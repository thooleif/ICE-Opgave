package ui;

/**
 * Samlet input fra præference-spørgeskemaet (bruges til at oprette TrainingPreference).
 */
public class TrainingPreferenceInput {

    private String focus;
    private String trainingStyle;
    private boolean wantsStrength;
    private boolean wantsCardio;
    private int daysPerWeek;
    private int sessionDurationMin;

    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }

    public String getTrainingStyle() { return trainingStyle; }
    public void setTrainingStyle(String trainingStyle) { this.trainingStyle = trainingStyle; }

    public boolean isWantsStrength() { return wantsStrength; }
    public void setWantsStrength(boolean wantsStrength) { this.wantsStrength = wantsStrength; }

    public boolean isWantsCardio() { return wantsCardio; }
    public void setWantsCardio(boolean wantsCardio) { this.wantsCardio = wantsCardio; }

    public int getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(int daysPerWeek) { this.daysPerWeek = daysPerWeek; }

    public int getSessionDurationMin() { return sessionDurationMin; }
    public void setSessionDurationMin(int sessionDurationMin) { this.sessionDurationMin = sessionDurationMin; }
}
