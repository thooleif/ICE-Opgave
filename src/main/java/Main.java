import db.DatabaseManager;
import logic.ProgramGenerator;
import model.*;
import ui.Questionnaire;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initialize();

        Scanner scanner = new Scanner(System.in);
        Questionnaire q = new Questionnaire(scanner);

        System.out.println("""
        
         _______ _____ _______ _   _ ______  _____ _____
        |  _____|_   _|__   __| \\ | |  ____|/ ____/ ____|
        | |__     | |    | |  |  \\| | |__  | (___| (___
        |  __|    | |    | |  | . ` |  __|  \\___ \\\\___ \\
        | |      _| |_   | |  | |\\  | |____ ____) ____) |
        |_|     |_____|  |_|  |_| \\_|______|_____/_____/
        
         Besvar spoergsmaalene saa vi kan bygge
         det perfekte program til dig.
        """);

        UserProfile profile = q.askProfile();
        FitnessGoal goal = q.askGoal();
        TrainingPreference prefs = q.askPreferences();

        System.out.println("\n========== OPSUMMERING ==========\n");
        System.out.println(profile);
        System.out.println(goal);
        System.out.println(prefs);

        System.out.println("\n========== DIT TRÆNINGSPROGRAM ==========\n");

        try {
            ProgramGenerator generator = new ProgramGenerator(profile, goal, prefs);
            List<TrainingDay> program = generator.generate();

            if (program.isEmpty()) {
                System.out.println("Kunne ikke generere et program. Prøv igen.");
            } else {
                for (TrainingDay day : program) {
                    System.out.println(day);
                }
            }
        } catch (Exception e) {
            System.out.println("Fejl ved generering: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("""

         ██████   ██████  ██████      ████████ ██████   █████  ███████ ███    ██ ██ ███    ██  ██████
        ██       ██    ██ ██   ██        ██    ██   ██ ██   ██ ██      ████   ██ ██ ████   ██ ██
        ██   ███ ██    ██ ██   ██        ██    ██████  ███████ █████   ██ ██  ██ ██ ██ ██  ██ ██   ███
        ██    ██ ██    ██ ██   ██        ██    ██   ██ ██   ██ ██      ██  ██ ██ ██ ██  ██ ██ ██    ██
         ██████   ██████  ██████         ██    ██   ██ ██   ██ ███████ ██   ████ ██ ██   ████  ██████
        """);

        scanner.close();
    }
}
