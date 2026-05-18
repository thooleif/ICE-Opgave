package logic;

import model.Exercise;

import java.util.List;
import java.util.stream.Collectors;

public class ExerciseClassifier {

    private static final String[] COMPOUND_KEYWORDS = {
        "Bench Press", "Squat", "Deadlift", "Row", "Lunge", "Pull-Up", "Chin-Up",
        "Dip", "Push-Up", "Clean", "Snatch", "Jerk", "Hip Thrust", "Good Morning",
        "Muscle-Up", "Overhead Press", "Floor Press", "Leg Press", "Step Up",
        "Split Squat", "Thrusters", "Turkish Get-Up", "Inverted Row", "T-Bar Row",
        "Pendlay Row", "Seal Row", "Rack Pull", "Ground to Overhead"
    };

    private static final String[] ISOLATION_KEYWORDS = {
        "Curl", "Extension", "Fly", "Flyes", "Raise", "Kickback", "Pullover",
        "Crunch", "Plank", "Shrug", "Wrist", "Pec Deck", "Leg Curl",
        "Leg Extension", "Calf Raise", "Side Bend", "Sit-Up", "Dead Bug",
        "Hollow", "L-Sit", "Mountain Climbers", "Pull Through", "Clamshells",
        "Fire Hydrants", "Donkey Kicks", "Frog Pumps", "Glute Bridge",
        "Pallof Press", "Wood Chop", "Rotation", "Windshield Wiper"
    };

    private static final String[] EXPLOSIVE_KEYWORDS = {
        "Clean", "Snatch", "Jerk", "Jump", "Swing", "Thrusters", "Slam",
        "Bound", "Clap", "Power", "Explosive", "Depth Jump", "Box Jump",
        "Devils Press", "Ground to Overhead", "Muscle-Up"
    };

    private static final String[] ADVANCED_KEYWORDS = {
        "Muscle-Up", "Pistol Squat", "Dragon Flag", "Handstand", "Ring",
        "L-Sit", "Nordic", "Deficit Deadlift", "Pause Deadlift", "Pin Squat",
        "Pause Squat", "Zercher", "Snatch", "Clean and Jerk", "Turkish Get-Up",
        "Copenhagen Plank", "Hanging Windshield Wiper", "Wall Walk"
    };

    private static final String[] BEGINNER_KEYWORDS = {
        "Machine", "Assisted", "Kneeling", "Against Wall", "Shallow",
        "Half Air Squat", "Chair Squat", "Body Weight Lunge"
    };

    private static final String[] HIGH_IMPACT_KEYWORDS = {
        "Jump", "Clap", "Bound", "Depth Jump", "Box Jump", "Jumping",
        "Snatch", "Clean", "Jerk", "Muscle-Up", "Handstand", "Dragon Flag",
        "Pistol Squat", "Nordic", "Zercher", "Deficit Deadlift",
        "Ground to Overhead", "Devils Press", "Wall Walk", "Sled"
    };

    private static final String[] JOINT_FRIENDLY_KEYWORDS = {
        "Machine", "Cable", "Seated", "Lying", "Band", "Resistance Band",
        "Smith Machine", "Assisted", "Glute Bridge", "Hip Thrust Machine",
        "Leg Press", "Pec Deck"
    };

    public static boolean isHighImpact(Exercise ex) {
        return matchesAny(ex.getName(), HIGH_IMPACT_KEYWORDS);
    }

    public static boolean isJointFriendly(Exercise ex) {
        return matchesAny(ex.getName(), JOINT_FRIENDLY_KEYWORDS);
    }

    public static boolean isCompound(Exercise ex) {
        return matchesAny(ex.getName(), COMPOUND_KEYWORDS)
            && !matchesAny(ex.getName(), ISOLATION_KEYWORDS);
    }

    public static boolean isIsolation(Exercise ex) {
        return matchesAny(ex.getName(), ISOLATION_KEYWORDS)
            || !isCompound(ex);
    }

    public static boolean isExplosive(Exercise ex) {
        return matchesAny(ex.getName(), EXPLOSIVE_KEYWORDS);
    }

    public static boolean isAdvanced(Exercise ex) {
        return matchesAny(ex.getName(), ADVANCED_KEYWORDS);
    }

    public static boolean isBeginner(Exercise ex) {
        return matchesAny(ex.getName(), BEGINNER_KEYWORDS);
    }

    public static boolean isSuitableFor(Exercise ex, String experienceLevel) {
        return switch (experienceLevel) {
            case "Beginner" -> !isAdvanced(ex);
            case "Intermediate" -> true;
            case "Advanced" -> true;
            default -> true;
        };
    }

    public static List<Exercise> filterCompounds(List<Exercise> exercises) {
        return exercises.stream().filter(ExerciseClassifier::isCompound).collect(Collectors.toList());
    }

    public static List<Exercise> filterIsolation(List<Exercise> exercises) {
        return exercises.stream().filter(ExerciseClassifier::isIsolation).collect(Collectors.toList());
    }

    public static List<Exercise> filterExplosive(List<Exercise> exercises) {
        return exercises.stream().filter(ExerciseClassifier::isExplosive).collect(Collectors.toList());
    }

    public static List<Exercise> filterByExperience(List<Exercise> exercises, String level) {
        return exercises.stream().filter(ex -> isSuitableFor(ex, level)).collect(Collectors.toList());
    }

    public static List<Exercise> filterByAge(List<Exercise> exercises, int age) {
        if (age < 18) {
            return exercises.stream()
                .filter(ex -> !isHighImpact(ex))
                .collect(Collectors.toList());
        }
        if (age >= 50) {
            return exercises.stream()
                .filter(ex -> !isHighImpact(ex))
                .collect(Collectors.toList());
        }
        return exercises;
    }

    public static List<Exercise> preferJointFriendly(List<Exercise> exercises) {
        List<Exercise> friendly = exercises.stream()
            .filter(ExerciseClassifier::isJointFriendly)
            .collect(Collectors.toList());
        if (friendly.size() >= 2) return friendly;
        return exercises;
    }

    private static boolean matchesAny(String name, String[] keywords) {
        String lower = name.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
