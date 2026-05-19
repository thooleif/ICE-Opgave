import db.DatabaseManager;
import logic.ProgramGenerator;
import model.TrainingDay;
import ui.ProgramBanner;

import java.util.List;

public final class TrainingProgramService {

    private static boolean dbInitialized;

    private TrainingProgramService() {}

    public static void ensureDatabase() {
        if (!dbInitialized) {
            DatabaseManager.initialize();
            dbInitialized = true;
        }
    }

    public static List<TrainingDay> generate(UserProfile profile, FitnessGoal goal, TrainingPreference prefs) {
        ensureDatabase();
        ProgramGenerator generator = new ProgramGenerator(
                ProfileMapper.toGeneratorProfile(profile),
                ProfileMapper.toGeneratorGoal(goal),
                ProfileMapper.toGeneratorPrefs(prefs)
        );
        return generator.generate();
    }

    public static String buildProgramInfo(UserProfile profile, FitnessGoal goal, TrainingPreference prefs) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PROGRAM INFO ===\n\n");
        sb.append("Profile:\n  ").append(profile).append("\n");
        sb.append("  BMI: ").append(String.format("%.1f", profile.calculateBMI())).append("\n\n");
        sb.append("Fitness goal:\n  ").append(goal).append("\n\n");
        sb.append("Training preferences:\n  ").append(prefs).append("\n");
        sb.append("  Strength included: ").append(prefs.getWantsStrength() ? "yes" : "no").append("\n");
        sb.append("  Cardio included: ").append(prefs.getWantsCardio() ? "yes" : "no").append("\n");
        sb.append("  Generator focus: ").append(prefs.resolveGeneratorFocus()).append("\n");
        sb.append("  Recommended split: ").append(prefs.getRecommendedSplit()).append("\n\n");
        sb.append("The program is built from your experience level, age, goals, and preferences.\n");
        sb.append("Exercises are picked from the database and filtered for safety and level.\n");
        sb.append("Starting weights are estimated from your body weight and experience — adjust in Tweak or at workout.");
        return sb.toString();
    }

    public static void printProgram(List<TrainingDay> program) {
        printProgram(program, true);
    }

    public static void printProgram(List<TrainingDay> program, boolean withBanner) {
        if (program.isEmpty()) {
            System.out.println("Kunne ikke generere et program. Tjek din profil og dine præferencer.");
            return;
        }

        if (withBanner) {
            ProgramBanner.printProgramHeader();
        } else {
            System.out.println("\n========== DIT TRÆNINGSPROGRAM ==========\n");
        }

        for (TrainingDay day : program) {
            System.out.println(day);
        }

        if (withBanner) {
            ProgramBanner.printSuccessFooter();
        }
    }
}
