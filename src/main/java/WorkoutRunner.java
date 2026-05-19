import logic.WeightInputParser;
import logic.WeightSuggester;
import model.FitnessGoal;
import model.ProgramExercise;
import model.TrainingDay;
import model.UserProfile;
import model.WorkoutExerciseLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Aktiv træningssession: vis PB, sidste og nuværende; gem fremskridt.
 */
public class WorkoutRunner {

    private final Scanner scanner;
    private final UUID userId;
    private final TrainingDay day;
    private final UserProfile profile;
    private final FitnessGoal goal;
    private final List<WorkoutExerciseLog> logs;

    public WorkoutRunner(Scanner scanner, UUID userId, TrainingDay day,
                         UserProfile profile, FitnessGoal goal) {
        this.scanner = scanner;
        this.userId = userId;
        this.day = day;
        this.profile = profile;
        this.goal = goal;
        this.logs = new ArrayList<>();
        for (ProgramExercise ex : day.getExercises()) {
            logs.add(new WorkoutExerciseLog(ex));
        }
        WorkoutLogStore.applyHistory(userId, day.getDayName(), logs);
        applyStartingWeights();
    }

    private void applyStartingWeights() {
        for (WorkoutExerciseLog log : logs) {
            if (!log.isLoggable()) {
                continue;
            }
            if (log.hasLast()) {
                log.setCurrentSets(log.getLastSets());
                log.setCurrentReps(log.getLastReps());
                if (log.getLastWeightKg() > 0) {
                    log.setCurrentWeightKg(log.getLastWeightKg());
                }
            } else if (log.getCurrentWeightKg() <= 0) {
                float suggested = WeightSuggester.suggest(
                        profile, goal, log.getExerciseName(), log.getPlannedTag());
                if (suggested > 0) {
                    log.setCurrentWeightKg(suggested);
                }
            }
        }
    }

    public void run() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.println("║           BEGIN WORKOUT                  ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println(day.getDayName() + " — " + day.getFocus());
        System.out.println("Vægte er forslag ud fra din profil — ret dem under hver øvelse.\n");

        boolean running = true;
        while (running) {
            printOverview();
            System.out.println("\n1. Log / edit an exercise");
            System.out.println("2. Finish workout (save PB + last workout)");
            System.out.println("3. Cancel (don't save)");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                editExercise();
            } else if (choice == 2) {
                finishWorkout();
                running = false;
            } else if (choice == 3) {
                System.out.println("Workout cancelled — nothing saved.");
                running = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void printOverview() {
        System.out.println("\n--- " + day.getDayName() + " | " + day.getFocus() + " ---");
        System.out.printf("%-3s %-28s %-18s %-18s %-18s%n",
                "#", "Exercise", "PB", "Last", "Current");
        System.out.println("─".repeat(88));

        int num = 1;
        for (WorkoutExerciseLog log : logs) {
            if (!log.isLoggable()) {
                System.out.println("    " + log.getExerciseName());
                continue;
            }
            System.out.printf("%-3d %-28s %-18s %-18s %-18s%n",
                    num++,
                    truncate(log.getExerciseName(), 28),
                    truncate(log.formatPb(), 18),
                    truncate(log.formatLast(), 18),
                    truncate(log.formatCurrent(), 18));
        }
    }

    private void editExercise() {
        List<WorkoutExerciseLog> loggable = logs.stream().filter(WorkoutExerciseLog::isLoggable).toList();
        if (loggable.isEmpty()) {
            System.out.println("No exercises to log on this day.");
            return;
        }

        System.out.println("\nPick exercise:");
        for (int i = 0; i < loggable.size(); i++) {
            WorkoutExerciseLog log = loggable.get(i);
            System.out.println("  " + (i + 1) + ". " + log.getExerciseName());
            String planWeight = WeightInputParser.formatDisplay(
                    log.isPlannedBodyweight(),
                    log.getPlannedWeightKg() > 0 ? log.getPlannedWeightKg()
                            : (log.getCurrentWeightKg() > 0 ? log.getCurrentWeightKg() : -1));
            System.out.println("      Plan:    " + log.getPlannedSets() + " x " + log.getPlannedReps()
                    + " @ " + planWeight);
            System.out.println("      PB:      " + log.formatPb());
            System.out.println("      Last:    " + log.formatLast());
            System.out.println("      Current: " + log.formatCurrent());
        }

        System.out.print("Exercise (0 = cancel): ");
        int idx = readInt() - 1;
        if (idx < 0 || idx >= loggable.size()) {
            return;
        }

        WorkoutExerciseLog log = loggable.get(idx);
        System.out.println("\nEditing: " + log.getExerciseName());
        float suggest = log.getCurrentWeightKg() > 0 ? log.getCurrentWeightKg()
                : WeightSuggester.suggest(profile, goal, log.getExerciseName(), log.getPlannedTag());
        if (suggest > 0) {
            System.out.println("Suggested weight: " + suggest + " kg (from your profile)");
        }

        System.out.print("Sets [" + log.getCurrentSets() + "]: ");
        String setsIn = scanner.nextLine().trim();
        if (!setsIn.isEmpty()) {
            int s = readIntFrom(setsIn);
            if (s > 0) {
                log.setCurrentSets(s);
            }
        }

        System.out.print("Reps [" + log.getCurrentReps() + "]: ");
        String repsIn = scanner.nextLine().trim();
        if (!repsIn.isEmpty()) {
            log.setCurrentReps(repsIn);
        }

        String weightHint = log.getCurrentWeightKg() > 0 ? String.valueOf(log.getCurrentWeightKg()) : "—";
        System.out.print("Weight kg (or type 'body' for bodyweight) [" + weightHint + "]: ");
        String weightIn = scanner.nextLine().trim();
        if (!weightIn.isEmpty()) {
            WeightInputParser.Result parsed = WeightInputParser.parse(weightIn, profile.getWeightKg());
            if (parsed.parsed) {
                log.applyCurrentWeightInput(parsed);
            } else {
                System.out.println("Could not read weight — try a number or 'body'.");
            }
        } else if (suggest > 0 && log.getCurrentWeightKg() <= 0) {
            log.setCurrentWeightKg(suggest);
        }

        if (log.getCurrentWeightKg() > 0 && log.hasPb() && log.getCurrentWeightKg() > log.getPbWeightKg()) {
            System.out.println("*** NEW PERSONAL BEST! ***");
        } else if (log.getCurrentWeightKg() > 0 && !log.hasPb()) {
            System.out.println("*** First logged weight — counts as PB when you finish! ***");
        }

        System.out.println("Updated: " + log.formatCurrent());
    }

    private void finishWorkout() {
        WorkoutLogStore.saveCompletedWorkout(userId, day.getDayName(), logs);
        System.out.println("\nWorkout saved!");
        System.out.println("  • Last workout updated for " + day.getDayName());
        System.out.println("  • Personal bests updated where you lifted heavier");
        System.out.println("\nKeep pushing — beat your numbers next time!");
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int readIntFrom(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private float readFloatFrom(String s) {
        try {
            return Float.parseFloat(s.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return -1f;
        }
    }
}
