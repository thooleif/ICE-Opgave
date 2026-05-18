package ui;

import model.FitnessGoal;
import model.TrainingPreference;
import model.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Questionnaire {

    private final Scanner scanner;

    public Questionnaire(Scanner scanner) {
        this.scanner = scanner;
    }

    public UserProfile askProfile() {
        UserProfile profile = new UserProfile();

        System.out.println("\n========== PROFIL ==========\n");

        profile.setGender(askChoice("Hvad er dit køn?", new String[]{
            "Mand", "Kvinde"
        }));

        profile.setAge(askInt("Hvor gammel er du?", 10, 100));
        profile.setWeightKg(askFloat("Hvad vejer du (kg)?", 30, 300));
        profile.setHeightCm(askFloat("Hvor høj er du (cm)?", 100, 250));

        profile.setExperienceLevel(askChoice("Hvad er dit erfaringsniveau?", new String[]{
            "Beginner", "Intermediate", "Advanced"
        }));

        return profile;
    }

    public FitnessGoal askGoal() {
        FitnessGoal goal = new FitnessGoal();

        System.out.println("\n========== MÅL ==========\n");

        goal.setGoalType(askChoice("Hvad er dit mål?", new String[]{
            "Bulk", "Cut", "Maintain"
        }));

        goal.setPhysicalGoals(askMultiChoice("Hvad vil du opnå fysisk? (vælg op til 2)", new String[]{
            "Stærkere", "Mere toned", "Eksplosiv", "Større"
        }, 2));

        return goal;
    }

    public TrainingPreference askPreferences() {
        TrainingPreference pref = new TrainingPreference();

        System.out.println("\n========== PRÆFERENCER ==========\n");

        pref.setFocus(askChoice("Hvad vil du fokusere på?", new String[]{
            "Styrke", "Cardio", "HIIT", "Blanding af alt"
        }));

        pref.setTrainingStyle(askChoice("Hvordan vil du træne?", new String[]{
            "Bodybuilder", "Atlet"
        }));

        pref.setDaysPerWeek(askInt("Hvor mange dage om ugen vil du træne?", 2, 6));

        pref.setSessionDurationMin(askChoiceInt("Hvor lang tid per session (minutter)?", new int[]{
            30, 45, 60, 90
        }));

        return pref;
    }

    private String askChoice(String question, String[] options) {
        System.out.println(question);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        while (true) {
            System.out.print("Vælg (1-" + options.length + "): ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.length) {
                    return options[choice - 1];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Ugyldigt valg, prøv igen.");
        }
    }

    private List<String> askMultiChoice(String question, String[] options, int maxSelections) {
        System.out.println(question);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        while (true) {
            System.out.print("Vælg op til " + maxSelections + " (f.eks. 1,3): ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(",");
            List<String> selected = new ArrayList<>();
            boolean valid = true;

            if (parts.length < 1 || parts.length > maxSelections) {
                valid = false;
            } else {
                for (String part : parts) {
                    try {
                        int choice = Integer.parseInt(part.trim());
                        if (choice >= 1 && choice <= options.length) {
                            String option = options[choice - 1];
                            if (!selected.contains(option)) {
                                selected.add(option);
                            }
                        } else {
                            valid = false;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid && !selected.isEmpty()) {
                return selected;
            }
            System.out.println("Ugyldigt valg. Vælg 1-" + maxSelections + " tal adskilt af komma.");
        }
    }

    private int askInt(String question, int min, int max) {
        while (true) {
            System.out.print(question + " (" + min + "-" + max + "): ");
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Indtast et tal mellem " + min + " og " + max + ".");
        }
    }

    private float askFloat(String question, float min, float max) {
        while (true) {
            System.out.print(question + " (" + min + "-" + max + "): ");
            String input = scanner.nextLine().trim();
            try {
                float value = Float.parseFloat(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Indtast et tal mellem " + min + " og " + max + ".");
        }
    }

    private int askChoiceInt(String question, int[] options) {
        System.out.println(question);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i] + " min");
        }
        while (true) {
            System.out.print("Vælg (1-" + options.length + "): ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.length) {
                    return options[choice - 1];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Ugyldigt valg, prøv igen.");
        }
    }
}
