package model;

import java.util.List;

public class FitnessGoal {
    private String goalType;
    private List<String> physicalGoals;

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public List<String> getPhysicalGoals() { return physicalGoals; }
    public void setPhysicalGoals(List<String> physicalGoals) { this.physicalGoals = physicalGoals; }

    @Override
    public String toString() {
        return "Mål: " + goalType + ", fokus: " + String.join(", ", physicalGoals);
    }
}
