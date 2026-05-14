import java.util.Date;

public class FitnessGoal {

    // Enum for goal type
    public enum GoalType {
        BULK_UP,
        LOSE_WEIGHT,
        MAINTAIN,
        RECOMP
    }

    // float bruges til at gemme decimaltal
    private GoalType goalType;
    private float targetWeightKg;
    private float startWeightKg;
    private Date deadlineDate;
    private float weeklyWeightChangeKg;

    // Constructor
    public FitnessGoal(GoalType goalType, float targetWeightKg, float startWeightKg, Date deadlineDate, float weeklyWeightChangeKg) {

        this.goalType = goalType;
        this.targetWeightKg = targetWeightKg;
        this.startWeightKg = startWeightKg;
        this.deadlineDate = deadlineDate;
        this.weeklyWeightChangeKg = weeklyWeightChangeKg;
    }

    // Set goal (initial setup)
    public void setGoal(GoalType goalType, float targetWeightKg, float startWeightKg, Date deadlineDate, float weeklyWeightChangeKg) {

        this.goalType = goalType;
        this.targetWeightKg = targetWeightKg;
        this.startWeightKg = startWeightKg;
        this.deadlineDate = deadlineDate;
        this.weeklyWeightChangeKg = weeklyWeightChangeKg;

        System.out.println("Goal is set, good luck.");
    }

    // Update goal
    public void updateGoal(GoalType goalType, float targetWeightKg, float startWeightKg, Date deadlineDate, float weeklyWeightChangeKg) {

        this.goalType = goalType;
        this.targetWeightKg = targetWeightKg;
        this.startWeightKg = startWeightKg;
        this.deadlineDate = deadlineDate;
        this.weeklyWeightChangeKg = weeklyWeightChangeKg;

        System.out.println("Goal updated successfully.");
    }

    // beregner målet for hver uge
    public float calculateWeeklyTarget() {
        return weeklyWeightChangeKg;
    }

    // er målet nået
    public boolean isGoalReached(float currentWeightKg) {

        if (goalType == GoalType.BULK_UP || goalType == GoalType.MAINTAIN || goalType == GoalType.RECOMP) {
            return currentWeightKg >= targetWeightKg;
        } else if (goalType == GoalType.LOSE_WEIGHT) {
            return currentWeightKg <= targetWeightKg;
        }

        return false;



    }


    public GoalType getGoalType() {
        return goalType;


    }

    public float getTargetWeightKg() {
        return targetWeightKg;
    }

    public float getStartWeightKg() {
        return startWeightKg;
    }

    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public float getWeeklyWeightChangeKg() {
        return weeklyWeightChangeKg;
    }

    // toString
    @Override
    public String toString() {

        return "FitnessGoal{" +
                "goalType=" + goalType +
                ", targetWeightKg=" + targetWeightKg +
                ", startWeightKg=" + startWeightKg +
                ", deadlineDate=" + deadlineDate +
                ", weeklyWeightChangeKg=" + weeklyWeightChangeKg +
                '}';
    }
}