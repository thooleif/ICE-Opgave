import db.DatabaseManager;
import db.ExerciseLoader;
import logic.WeightInputParser;
import logic.WeightSuggester;
import model.Exercise;
import model.FitnessGoal;
import model.ProgramExercise;
import model.TrainingDay;
import model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interaktiv editor til gemte træningsprogrammer.
 */
public class ProgramTweaker {

    @FunctionalInterface
    public interface SaveAction {
        void save();
    }

    @FunctionalInterface
    public interface VoidAction {
        void run();
    }

    private final Scanner scanner;
    private final SavedTrainingProgram program;
    private final SaveAction saveAction;
    private final VoidAction editPreferences;
    private final VoidAction editGoal;
    private final VoidAction regenerateProgram;
    private final UserProfile profile;
    private final FitnessGoal goal;

    public ProgramTweaker(Scanner scanner,
                          SavedTrainingProgram program,
                          UserProfile profile,
                          FitnessGoal goal,
                          SaveAction saveAction,
                          VoidAction editPreferences,
                          VoidAction editGoal,
                          VoidAction regenerateProgram) {
        this.scanner = scanner;
        this.program = program;
        this.profile = profile;
        this.goal = goal;
        this.saveAction = saveAction;
        this.editPreferences = editPreferences;
        this.editGoal = editGoal;
        this.regenerateProgram = regenerateProgram;
    }

    public void run() {
        DatabaseManager.initialize();
        boolean running = true;
        while (running) {
            System.out.println("\n=== Tweak Program ===");
            System.out.println(program.getOverview());
            System.out.println("\n1. Edit a training day");
            System.out.println("2. View full program");
            System.out.println("3. Update training preferences");
            System.out.println("4. Update fitness goal");
            System.out.println("5. Regenerate program (uses current prefs — overwrites tweaks)");
            System.out.println("6. Save and back");
            System.out.println("7. Back (discard unsaved changes since last save)");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                editDayMenu();
            } else if (choice == 2) {
                viewProgram();
            } else if (choice == 3) {
                editPreferences.run();
                offerRegenerateAfterPrefChange();
            } else if (choice == 4) {
                editGoal.run();
            } else if (choice == 5) {
                System.out.print("Regenerate will replace your tweaked program. Continue? (j/n): ");
                if (isYes()) {
                    regenerateProgram.run();
                    return;
                }
            } else if (choice == 6) {
                saveAction.save();
                return;
            } else if (choice == 7) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void offerRegenerateAfterPrefChange() {
        System.out.print("Regenerate program from updated preferences? (j/n): ");
        if (isYes()) {
            regenerateProgram.run();
        }
    }

    private void viewProgram() {
        System.out.println();
        for (TrainingDay day : program.getDays()) {
            System.out.println(day);
        }
    }

    private void editDayMenu() {
        List<TrainingDay> days = program.getDays();
        if (days.isEmpty()) {
            System.out.println("No days in program.");
            return;
        }

        System.out.println("\nPick a day:");
        for (int i = 0; i < days.size(); i++) {
            TrainingDay d = days.get(i);
            System.out.println("  " + (i + 1) + ". " + d.getDayName() + " — " + d.getFocus()
                    + " (" + d.getExercises().size() + " exercises)");
        }
        System.out.print("Day (0 = cancel): ");
        int dayIdx = readInt() - 1;
        if (dayIdx < 0 || dayIdx >= days.size()) {
            return;
        }

        editSingleDay(days.get(dayIdx));
    }

    private void editSingleDay(TrainingDay day) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- " + day.getDayName() + " | " + day.getFocus() + " ---");
            List<ProgramExercise> exercises = day.getExercises();
            for (int i = 0; i < exercises.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + exercises.get(i));
            }

            System.out.println("\n1. Change day focus");
            System.out.println("2. Edit an exercise");
            System.out.println("3. Add exercise");
            System.out.println("4. Remove exercise");
            System.out.println("5. Save changes");
            System.out.println("6. Back");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                changeDayFocus(day);
            } else if (choice == 2) {
                editExerciseMenu(day);
            } else if (choice == 3) {
                addExercise(day);
            } else if (choice == 4) {
                removeExercise(day);
            } else if (choice == 5) {
                saveAction.save();
                System.out.println("Saved.");
            } else if (choice == 6) {
                running = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void changeDayFocus(TrainingDay day) {
        System.out.print("New focus for " + day.getDayName() + ": ");
        String focus = scanner.nextLine().trim();
        if (!focus.isEmpty()) {
            day.setFocus(focus);
            System.out.println("Focus updated.");
        }
    }

    private void editExerciseMenu(TrainingDay day) {
        List<ProgramExercise> exercises = day.getExercises();
        if (exercises.isEmpty()) {
            System.out.println("No exercises on this day.");
            return;
        }

        System.out.print("Exercise number (0 = cancel): ");
        int idx = readInt() - 1;
        if (idx < 0 || idx >= exercises.size()) {
            return;
        }

        ProgramExercise ex = exercises.get(idx);
        boolean running = true;
        while (running) {
            System.out.println("\nEditing: " + ex);
            System.out.println("1. Change exercise");
            System.out.println("2. Change sets");
            System.out.println("3. Change reps");
            System.out.println("4. Change rest (seconds)");
            System.out.println("5. Change / add weight (kg)");
            System.out.println("6. Change tag (e.g. Hovedøvelse, Isolation)");
            System.out.println("7. Back");
            System.out.print("Choose: ");

            int choice = readInt();
            if (choice == 1) {
                pickExerciseFromDatabase(ex);
            } else if (choice == 2) {
                System.out.print("Sets: ");
                int sets = readInt();
                if (sets > 0) {
                    ex.setSets(sets);
                }
            } else if (choice == 3) {
                System.out.print("Reps (e.g. 8-10 or 30s): ");
                String reps = scanner.nextLine().trim();
                if (!reps.isEmpty()) {
                    ex.setReps(reps);
                }
            } else if (choice == 4) {
                System.out.print("Rest seconds: ");
                ex.setRestTimeSec(Math.max(0, readInt()));
            } else if (choice == 5) {
                float suggested = WeightSuggester.suggest(profile, goal, ex.getExerciseName(), ex.getTag());
                if (suggested > 0) {
                    System.out.println("Suggested: " + suggested + " kg");
                }
                System.out.print("Weight in kg (or 'body' for bodyweight, 0 to clear): ");
                WeightInputParser.Result parsed = WeightInputParser.parse(scanner.nextLine().trim(), profile.getWeightKg());
                if (parsed.parsed) {
                    ex.applyWeightInput(parsed);
                }
            } else if (choice == 6) {
                System.out.print("Tag: ");
                ex.setTag(scanner.nextLine().trim());
            } else if (choice == 7) {
                running = false;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void addExercise(TrainingDay day) {
        System.out.print("Tag (e.g. Hovedøvelse, Isolation, Accessory): ");
        String tag = scanner.nextLine().trim();
        if (tag.isEmpty()) {
            tag = "Custom";
        }

        ProgramExercise ex = new ProgramExercise("New Exercise", 3, "8-12", 90, tag, -1f);
        pickExerciseFromDatabase(ex);

        float suggested = WeightSuggester.suggest(profile, goal, ex.getExerciseName(), tag);
        if (suggested > 0) {
            System.out.println("Suggested weight: " + suggested + " kg (from your profile)");
            ex.setWeightKg(suggested);
        }

        System.out.print("Sets [" + ex.getSets() + "]: ");
        String setsIn = scanner.nextLine().trim();
        if (!setsIn.isEmpty()) {
            int sets = readIntFrom(setsIn);
            if (sets > 0) {
                ex.setSets(sets);
            }
        }

        System.out.print("Reps [" + ex.getReps() + "]: ");
        String reps = scanner.nextLine().trim();
        if (!reps.isEmpty()) {
            ex.setReps(reps);
        }

        System.out.print("Rest seconds [" + ex.getRestTimeSec() + "]: ");
        String restIn = scanner.nextLine().trim();
        if (!restIn.isEmpty()) {
            ex.setRestTimeSec(Math.max(0, readIntFrom(restIn)));
        }

        String weightLabel = WeightInputParser.formatDisplay(ex.isBodyweight(), ex.getWeightKg());
        System.out.print("Weight kg (or 'body') [" + weightLabel + "]: ");
        String weightIn = scanner.nextLine().trim();
        if (!weightIn.isEmpty()) {
            WeightInputParser.Result parsed = WeightInputParser.parse(weightIn, profile.getWeightKg());
            if (parsed.parsed) {
                ex.applyWeightInput(parsed);
            }
        }

        day.getExercises().add(ex);
        System.out.println("Exercise added: " + ex);
    }

    private void removeExercise(TrainingDay day) {
        List<ProgramExercise> exercises = day.getExercises();
        if (exercises.isEmpty()) {
            return;
        }
        System.out.print("Remove exercise number: ");
        int idx = readInt() - 1;
        if (idx >= 0 && idx < exercises.size()) {
            System.out.println("Removed: " + exercises.remove(idx).getExerciseName());
        }
    }

    private void pickExerciseFromDatabase(ProgramExercise target) {
        List<String> groups = ExerciseLoader.getAllMuscleGroups();
        System.out.println("\nMuscle group:");
        for (int i = 0; i < groups.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + groups.get(i));
        }
        System.out.print("Choose group (0 = cancel): ");
        int g = readInt() - 1;
        if (g < 0 || g >= groups.size()) {
            return;
        }

        List<Exercise> pool = ExerciseLoader.getByMuscleGroup(groups.get(g));
        if (pool.isEmpty()) {
            System.out.println("No exercises in that group.");
            return;
        }

        int pageSize = 15;
        int page = 0;
        while (true) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, pool.size());
            System.out.println("\n" + groups.get(g) + " (showing " + (start + 1) + "-" + end + " of " + pool.size() + "):");
            for (int i = start; i < end; i++) {
                System.out.println("  " + (i + 1) + ". " + pool.get(i).getName());
            }
            if (end < pool.size()) {
                System.out.println("  n. Next page");
            }
            if (page > 0) {
                System.out.println("  p. Previous page");
            }
            System.out.println("  Or type exercise number to select (0 = cancel)");
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("n") && end < pool.size()) {
                page++;
                continue;
            }
            if (input.equals("p") && page > 0) {
                page--;
                continue;
            }
            try {
                int pick = Integer.parseInt(input);
                if (pick == 0) {
                    return;
                }
                if (pick >= 1 && pick <= pool.size()) {
                    target.setExerciseName(pool.get(pick - 1).getName());
                    System.out.println("Exercise set to: " + target.getExerciseName());
                    return;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid input.");
        }
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

    private float readFloat() {
        try {
            return Float.parseFloat(scanner.nextLine().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return -1f;
        }
    }

    private boolean isYes() {
        String s = scanner.nextLine().trim().toLowerCase();
        return s.startsWith("j") || s.startsWith("y") || s.isEmpty();
    }
}
