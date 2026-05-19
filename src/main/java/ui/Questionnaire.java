package ui;

import model.FitnessGoal;
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

        goal.setPhysicalGoals(askPhysicalGoals());

        return goal;
    }

    public List<String> askPhysicalGoals() {
        System.out.println("\n--- Fysiske delmål ---\n");
        return askMultiChoice("Hvad vil du opnå fysisk? (vælg op til 2)", new String[]{
            "Stærkere", "Mere toned", "Eksplosiv", "Større"
        }, 2);
    }

    /**
     * Kombineret præference-flow: dine fokus-valg + teamets styrke/cardio-toggles og smarte defaults.
     */
    public TrainingPreferenceInput askFullPreferences() {
        TrainingPreferenceInput input = new TrainingPreferenceInput();

        System.out.println("\n========== TRÆNINGSPRÆFERENCER ==========\n");

        input.setFocus(askFocusWithHints());

        boolean[] defaults = defaultStrengthCardio(input.getFocus());
        input.setWantsStrength(askYesNo(
                "Vil du have styrketræning med? (default: " + (defaults[0] ? "ja" : "nej") + ")",
                defaults[0]));
        input.setWantsCardio(askYesNo(
                "Vil du have cardio med? (default: " + (defaults[1] ? "ja" : "nej") + ")",
                defaults[1]));

        if (shouldAskTrainingStyle(input)) {
            input.setTrainingStyle(askTrainingStyle());
        } else {
            input.setTrainingStyle("Atlet");
            System.out.println("Træningsstil sat til Atlet (passer bedst til dit fokus).");
        }

        input.setDaysPerWeek(askInt("Hvor mange dage om ugen vil du træne?", 1, 7));

        input.setSessionDurationMin(askChoiceInt("Hvor lang tid per session (minutter)?", new int[]{
            30, 45, 60, 90
        }));

        return input;
    }

    public String askFocus() {
        return askFocusWithHints();
    }

    private String askFocusWithHints() {
        System.out.println("Hvad er dit primære træningsfokus?");
        System.out.println("  1. Styrke — tunge løft, squat/bench/deadlift-inspireret");
        System.out.println("  2. Cardio — løb, cykel, steady state");
        System.out.println("  3. HIIT — korte intense intervaller/circuits");
        System.out.println("  4. Blanding af alt — balanceret mix af styrke og cardio");

        while (true) {
            System.out.print("Vælg (1-4): ");
            String raw = scanner.nextLine().trim();
            try {
                return switch (Integer.parseInt(raw)) {
                    case 1 -> "Styrke";
                    case 2 -> "Cardio";
                    case 3 -> "HIIT";
                    case 4 -> "Blanding af alt";
                    default -> throw new NumberFormatException();
                };
            } catch (NumberFormatException e) {
                System.out.println("Ugyldigt valg, prøv igen.");
            }
        }
    }

    private boolean[] defaultStrengthCardio(String focus) {
        return switch (focus) {
            case "Styrke" -> new boolean[]{true, false};
            case "Cardio" -> new boolean[]{false, true};
            case "HIIT" -> new boolean[]{false, true};
            case "Blanding af alt" -> new boolean[]{true, true};
            default -> new boolean[]{true, false};
        };
    }

    private boolean shouldAskTrainingStyle(TrainingPreferenceInput input) {
        if ("HIIT".equals(input.getFocus()) || "Cardio".equals(input.getFocus())) {
            return input.isWantsStrength();
        }
        return !"Cardio".equals(input.getFocus());
    }

    public String askTrainingStyle() {
        System.out.println("Hvordan vil du primært træne? (vælg én)");
        System.out.println("  1. Bodybuilder — volumen, hypertrofi, isolation");
        System.out.println("  2. Atlet — compounds, power, funktionel styrke");

        while (true) {
            System.out.print("Vælg (1-2): ");
            String raw = scanner.nextLine().trim();
            try {
                return switch (Integer.parseInt(raw)) {
                    case 1 -> "Bodybuilder";
                    case 2 -> "Atlet";
                    default -> throw new NumberFormatException();
                };
            } catch (NumberFormatException e) {
                System.out.println("Ugyldigt valg — vælg 1 eller 2.");
            }
        }
    }

    public boolean askYesNo(String prompt, boolean defaultValue) {
        System.out.print(prompt + " (j/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.isEmpty()) {
            return defaultValue;
        }
        if (input.startsWith("j") || input.startsWith("y")) {
            return true;
        }
        if (input.startsWith("n")) {
            return false;
        }
        return defaultValue;
    }

    public int askSessionDuration() {
        return askChoiceInt("Hvor lang tid per session (minutter)?", new int[]{30, 45, 60, 90});
    }

    private String askChoice(String question, String[] options) {
        if (!question.isBlank()) {
            System.out.println(question);
        }
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
