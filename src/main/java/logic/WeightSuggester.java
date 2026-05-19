package logic;

import model.FitnessGoal;
import model.UserProfile;

/**
 * Estimerer startvægt ud fra kropsvægt, køn, alder, erfaring og øvelsestype.
 */
public final class WeightSuggester {

    private WeightSuggester() {}

    public static float suggest(UserProfile profile, FitnessGoal goal, String exerciseName, String tag) {
        if (profile == null || profile.getWeightKg() <= 0) {
            return -1f;
        }
        if (exerciseName == null || exerciseName.startsWith("──")) {
            return -1f;
        }
        if (isNoWeightTag(tag) || isCardioName(exerciseName)) {
            return -1f;
        }

        String lower = exerciseName.toLowerCase();
        float bw = profile.getWeightKg();
        float baseFraction = liftFraction(lower, tag);
        float weight = bw * baseFraction;

        weight *= experienceFactor(profile.getExperienceLevel());
        weight *= genderFactor(profile.getGender());
        weight *= ageFactor(profile.getAge());
        weight *= goalFactor(goal != null ? goal.getGoalType() : "Maintain");
        weight *= tagFactor(tag);

        return roundWeight(weight);
    }

    private static float liftFraction(String name, String tag) {
        if (name.contains("squat") && !name.contains("split") && !name.contains("pistol")) {
            return 0.95f;
        }
        if (name.contains("deadlift") || name.contains("romanian")) {
            return 1.1f;
        }
        if (name.contains("bench press") || name.contains("floor press") || name.contains("chest press")) {
            return 0.65f;
        }
        if (name.contains("overhead press") || name.contains("military") || name.contains("ohp")
                || name.contains("shoulder press") || name.contains("arnold press")) {
            return 0.42f;
        }
        if (name.contains("row") || name.contains("pull-up") || name.contains("chin-up")
                || name.contains("lat pulldown")) {
            return 0.55f;
        }
        if (name.contains("leg press") || name.contains("hack squat")) {
            return 1.15f;
        }
        if (name.contains("lunge") || name.contains("split squat") || name.contains("step up")) {
            return 0.35f;
        }
        if (name.contains("hip thrust") || name.contains("glute bridge")) {
            return 0.75f;
        }
        if (name.contains("curl") || name.contains("extension") || name.contains("fly")
                || name.contains("raise") || name.contains("kickback")) {
            return 0.14f;
        }
        if (name.contains("machine") || name.contains("smith")) {
            return 0.5f;
        }
        if ("Hovedøvelse".equals(tag)) {
            return 0.7f;
        }
        if ("Isolation".equals(tag) || "Abs".equals(tag)) {
            return 0.12f;
        }
        if ("Accessory".equals(tag) || "Compound".equals(tag)) {
            return 0.45f;
        }
        if ("Eksplosiv".equals(tag)) {
            return 0.5f;
        }
        return 0.4f;
    }

    private static float experienceFactor(String level) {
        if (level == null) {
            return 0.65f;
        }
        return switch (level) {
            case "Beginner" -> 0.55f;
            case "Intermediate" -> 0.75f;
            case "Advanced" -> 0.95f;
            default -> 0.65f;
        };
    }

    private static float genderFactor(String gender) {
        if ("Kvinde".equals(gender)) {
            return 0.72f;
        }
        return 1f;
    }

    private static float ageFactor(int age) {
        if (age < 18) {
            return 0.75f;
        }
        if (age >= 55) {
            return 0.78f;
        }
        if (age >= 45) {
            return 0.88f;
        }
        return 1f;
    }

    private static float goalFactor(String goalType) {
        if (goalType == null) {
            return 1f;
        }
        return switch (goalType) {
            case "Bulk" -> 1.08f;
            case "Cut" -> 0.94f;
            default -> 1f;
        };
    }

    private static float tagFactor(String tag) {
        if (tag == null) {
            return 1f;
        }
        return switch (tag) {
            case "Hovedøvelse" -> 1f;
            case "Compound", "Accessory" -> 0.88f;
            case "Isolation", "Abs" -> 0.75f;
            case "Eksplosiv" -> 0.85f;
            default -> 1f;
        };
    }

    private static boolean isNoWeightTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return false;
        }
        return tag.contains("HIIT") || tag.contains("Cardio") || tag.contains("Superset")
                || tag.contains("Burnout") || tag.equals("HIIT");
    }

    private static boolean isCardioName(String name) {
        String n = name.toLowerCase();
        return n.contains("treadmill") || n.contains("bike") || n.contains("rower")
                || n.contains("cardio") || n.contains("jump rope") || n.contains("elliptical")
                || n.contains("stair") || n.contains("assault");
    }

    private static float roundWeight(float kg) {
        if (kg <= 0) {
            return -1f;
        }
        if (kg < 10) {
            return Math.max(2.5f, Math.round(kg * 2) / 2f);
        }
        return Math.round(kg / 2.5f) * 2.5f;
    }
}
