import java.util.ArrayList;
import java.util.List;

/**
 * Maps team CSV/domain classes to model classes used by ProgramGenerator.
 */
public final class ProfileMapper {

    private ProfileMapper() {}

    public static model.UserProfile toGeneratorProfile(UserProfile team) {
        model.UserProfile profile = new model.UserProfile();
        profile.setGender(team.getGender() == UserProfile.Gender.MALE ? "Mand" : "Kvinde");
        profile.setAge(team.getAge());
        profile.setWeightKg(team.getWeightKg());
        profile.setHeightCm(team.getHeightCm());
        profile.setExperienceLevel(mapExperience(team.getExperienceLevel()));
        return profile;
    }

    public static model.FitnessGoal toGeneratorGoal(FitnessGoal team) {
        model.FitnessGoal goal = new model.FitnessGoal();
        goal.setGoalType(mapGoalType(team.getGoalType()));
        if (team.getPhysicalGoals() != null && !team.getPhysicalGoals().isEmpty()) {
            goal.setPhysicalGoals(new ArrayList<>(team.getPhysicalGoals()));
        } else {
            goal.setPhysicalGoals(inferPhysicalGoals(team));
        }
        return goal;
    }

    public static model.TrainingPreference toGeneratorPrefs(TrainingPreference team) {
        model.TrainingPreference prefs = new model.TrainingPreference();
        prefs.setFocus(team.resolveGeneratorFocus());
        prefs.setTrainingStyle(team.getTrainingStyle());
        prefs.setDaysPerWeek(Math.min(7, Math.max(1, team.getTrainingDaysPerWeek())));
        prefs.setSessionDurationMin(team.getSessionDurationMin());
        return prefs;
    }

    private static String mapExperience(UserProfile.ExperienceLevel level) {
        return switch (level) {
            case BEGINNER, NOVICE -> "Beginner";
            case INTERMEDIATE -> "Intermediate";
            case ADVANCED -> "Advanced";
        };
    }

    private static String mapGoalType(FitnessGoal.GoalType type) {
        return switch (type) {
            case BULK_UP -> "Bulk";
            case LOSE_WEIGHT -> "Cut";
            case MAINTAIN, RECOMP -> "Maintain";
        };
    }

    private static List<String> inferPhysicalGoals(FitnessGoal team) {
        List<String> goals = new ArrayList<>();
        switch (team.getGoalType()) {
            case BULK_UP -> goals.add("Større");
            case LOSE_WEIGHT -> goals.add("Mere toned");
            case RECOMP -> {
                goals.add("Mere toned");
                goals.add("Stærkere");
            }
            case MAINTAIN -> { }
        }
        if (goals.isEmpty()) {
            goals.add("Mere toned");
        }
        return goals;
    }
}
