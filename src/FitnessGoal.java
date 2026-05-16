import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FitnessGoal {

    // CSV-filen hvor målene gemmes
    private static final String GOALS_FILE = "Data/FitnessGoals.csv";

    // Format til datoer i CSV - skal være samme overalt så parse virker
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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

    // ============================================================
    // CSV - alle metoder til at gemme og indlæse mål fra fil
    // Ligger i klassen så Menu ikke skal bekymre sig om filformat
    // ============================================================

    // Lille helper der laver én CSV-linje for dette mål
    private String toCsvLine(UUID userId) {
        return userId + ";" +
                goalType + ";" +
                targetWeightKg + ";" +
                startWeightKg + ";" +
                DATE_FORMAT.format(deadlineDate) + ";" +
                weeklyWeightChangeKg + "\n";
    }

    // Tjekker om der allerede ligger et mål for brugeren - bruges for at undgå duplikater
    private static boolean existsInFile(UUID userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(GOALS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 0 && parts[0].equals(userId.toString())) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    // Gemmer målet for en bruger - kalder update hvis der allerede er en linje
    public void save(UUID userId) {

        if (existsInFile(userId)) {
            update(userId);
            return;
        }

        try (FileWriter writer = new FileWriter(GOALS_FILE, true)) {
            writer.write(toCsvLine(userId));
            System.out.println("Goal saved.");
        } catch (IOException e) {
            System.out.println("Error saving goal: " + e.getMessage());
        }
    }

    // Overskriver brugerens linje - læser hele filen ind først, skriver bagefter
    // for at undgå reader og writer åbne samtidig
    public void update(UUID userId) {

        StringBuilder sb = new StringBuilder();
        boolean foundUser = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(GOALS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 0 && parts[0].equals(userId.toString())) {
                    sb.append(toCsvLine(userId));
                    foundUser = true;
                } else {
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading goal: " + e.getMessage());
            return;
        }

        if (!foundUser) {
            sb.append(toCsvLine(userId));
        }

        try (FileWriter writer = new FileWriter(GOALS_FILE, false)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Error updating goal: " + e.getMessage());
        }
    }

    // Indlæser målet for en bruger - returnerer null hvis ikke fundet
    // Static fordi vi ikke har et objekt at indlæse på endnu når vi kalder den
    public static FitnessGoal load(UUID userId) {

        try (BufferedReader reader = new BufferedReader(new FileReader(GOALS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 6 && parts[0].equals(userId.toString())) {

                    GoalType type = GoalType.valueOf(parts[1]);
                    float target = Float.parseFloat(parts[2]);
                    float start = Float.parseFloat(parts[3]);
                    Date deadline = DATE_FORMAT.parse(parts[4]);
                    float weekly = Float.parseFloat(parts[5]);

                    return new FitnessGoal(type, target, start, deadline, weekly);
                }
            }
        } catch (IOException | ParseException e) {
            System.out.println("Error loading goal: " + e.getMessage());
        }
        return null;
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