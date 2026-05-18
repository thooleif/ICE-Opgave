package logic;

import db.ExerciseLoader;
import model.*;

import java.util.*;

public class ProgramGenerator {

    private final UserProfile profile;
    private final FitnessGoal goal;
    private final TrainingPreference prefs;
    private final Random random = new Random();
    private final Set<String> usedExercises = new HashSet<>();
    private final Set<String> usedMainLifts = new HashSet<>();
    private final boolean wantsVariation;

    public ProgramGenerator(UserProfile profile, FitnessGoal goal, TrainingPreference prefs) {
        this.profile = profile;
        this.goal = goal;
        this.prefs = prefs;
        this.wantsVariation = prefs.getTrainingStyle().equals("Atlet")
            || goal.getPhysicalGoals().contains("Eksplosiv");
    }

    public List<TrainingDay> generate() {
        String[][] split = determineSplit();
        int exercisesPerSession = exercisesForDuration(prefs.getSessionDurationMin());
        boolean isHIIT = prefs.getFocus().equals("HIIT");

        List<TrainingDay> program = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
            String dayName = "Dag " + (i + 1);
            String focus = split[i][0];
            String[] muscleGroups = Arrays.copyOfRange(split[i], 1, split[i].length);

            List<ProgramExercise> dayExercises;

            if (isHIIT) {
                dayExercises = buildHIITDay(muscleGroups, exercisesPerSession);
            } else {
                dayExercises = buildSmartDay(muscleGroups, exercisesPerSession, focus);
            }

            if (shouldAddCardio() && !isHIIT) {
                dayExercises.add(buildCardioBlock());
            }

            if (shouldAddAbs(focus) && !isHIIT) {
                dayExercises.addAll(buildAbsFinisher());
            }

            program.add(new TrainingDay(dayName, focus, dayExercises));
        }

        return program;
    }

    // ========== SMART DAY BUILDER ==========

    private List<ProgramExercise> buildSmartDay(String[] muscleGroups, int totalExercises, String dayFocus) {
        List<ProgramExercise> result = new ArrayList<>();
        boolean isAtlet = prefs.getTrainingStyle().equals("Atlet");
        boolean isBodybuilder = prefs.getTrainingStyle().equals("Bodybuilder");
        boolean isFullBody = dayFocus.equals("Full Body");

        // --- Phase 1: Explosive (kun atlet) ---
        if (isAtlet && goal.getPhysicalGoals().contains("Eksplosiv")) {
            List<Exercise> allExplosive = new ArrayList<>();
            for (String group : muscleGroups) {
                allExplosive.addAll(ExerciseClassifier.filterExplosive(
                    filterAvailable(ExerciseLoader.getByMuscleGroup(group))
                ));
            }
            if (!allExplosive.isEmpty()) {
                Exercise ex = pickNewRandom(allExplosive);
                if (ex != null) {
                    result.add(buildExercise(ex.getName(), 4, "3-5", 120, "Eksplosiv"));
                }
            }
        }

        // --- Phase 2: Main compounds (hovedøvelser - tunge) ---
        int mainCompoundCount = isFullBody ? totalExercises : (isBodybuilder ? 1 : 2);

        if (isFullBody) {
            result.addAll(buildFullBodyCompounds(muscleGroups, totalExercises));
            return result;
        }

        for (int g = 0; g < muscleGroups.length && result.size() < mainCompoundCount + 1; g++) {
            List<Exercise> compounds = ExerciseClassifier.filterCompounds(
                filterAvailable(ExerciseLoader.getByMuscleGroup(muscleGroups[g]))
            );
            if (compounds.isEmpty()) continue;

            Exercise main = pickMainLift(compounds);
            if (main != null) {
                result.add(buildExercise(
                    main.getName(),
                    mainCompoundSets(), mainCompoundReps(), mainCompoundRest(),
                    "Hovedøvelse"
                ));
            }
        }

        // --- Phase 3: Accessory compounds (lettere) ---
        int accessoryTarget = isBodybuilder ? 1 : 2;
        int accessoryCount = 0;
        for (String group : muscleGroups) {
            if (accessoryCount >= accessoryTarget) break;
            List<Exercise> compounds = ExerciseClassifier.filterCompounds(
                filterAvailable(ExerciseLoader.getByMuscleGroup(group))
            );
            if (compounds.isEmpty()) continue;
            Exercise acc = pickNewRandom(compounds);
            if (acc != null) {
                result.add(buildExercise(
                    acc.getName(),
                    accessoryCompoundSets(), accessoryCompoundReps(), accessoryCompoundRest(),
                    "Accessory"
                ));
                accessoryCount++;
            }
        }

        // --- Phase 4: Isolation (fyld resten op) ---
        int remaining = totalExercises - result.size();
        if (remaining > 0) {
            List<Exercise> allIsolation = new ArrayList<>();
            for (String group : muscleGroups) {
                allIsolation.addAll(ExerciseClassifier.filterIsolation(
                    filterAvailable(ExerciseLoader.getByMuscleGroup(group))
                ));
            }
            List<Exercise> picked = pickNewRandomMultiple(allIsolation, remaining);
            for (Exercise ex : picked) {
                result.add(buildExercise(
                    ex.getName(),
                    isolationSets(), isolationReps(), isolationRest(),
                    "Isolation"
                ));
            }
        }

        return result;
    }

    // ========== FULL BODY ==========

    private List<ProgramExercise> buildFullBodyCompounds(String[] muscleGroups, int totalExercises) {
        List<ProgramExercise> result = new ArrayList<>();
        boolean isKvinde = profile.getGender().equals("Kvinde");

        String[] priorityOrder = isKvinde
            ? new String[]{"Glutes", "Legs", "Back", "Shoulders", "Chest"}
            : new String[]{"Chest", "Back", "Legs", "Shoulders", "Glutes"};
        List<String> available = Arrays.asList(muscleGroups);

        for (String group : priorityOrder) {
            if (!available.contains(group)) continue;
            if (result.size() >= totalExercises) break;

            List<Exercise> compounds = ExerciseClassifier.filterCompounds(
                filterAvailable(ExerciseLoader.getByMuscleGroup(group))
            );
            if (compounds.isEmpty()) continue;

            Exercise ex = pickMainLift(compounds);
            if (ex == null) continue;

            boolean isFirst = result.isEmpty();
            if (isFirst) {
                result.add(buildExercise(
                    ex.getName(), mainCompoundSets(), mainCompoundReps(), mainCompoundRest(), "Hovedøvelse"
                ));
            } else {
                result.add(buildExercise(
                    ex.getName(), accessoryCompoundSets(), accessoryCompoundReps(), accessoryCompoundRest(), "Compound"
                ));
            }
        }

        int remaining = totalExercises - result.size();
        if (remaining > 0) {
            List<Exercise> allIsolation = new ArrayList<>();
            for (String group : muscleGroups) {
                allIsolation.addAll(ExerciseClassifier.filterIsolation(
                    filterAvailable(ExerciseLoader.getByMuscleGroup(group))
                ));
            }
            for (Exercise ex : pickNewRandomMultiple(allIsolation, remaining)) {
                result.add(buildExercise(
                    ex.getName(), isolationSets(), isolationReps(), isolationRest(), "Isolation"
                ));
            }
        }

        return result;
    }

    // ========== HIIT DAY BUILDER ==========

    private List<ProgramExercise> buildHIITDay(String[] muscleGroups, int totalExercises) {
        List<ProgramExercise> result = new ArrayList<>();
        int circuitSize = Math.min(totalExercises, 6);
        int rounds = prefs.getSessionDurationMin() <= 30 ? 3 : 4;

        result.add(new ProgramExercise(
            "── CIRCUIT (" + rounds + " runder, 30s arbejde / 15s hvil) ──",
            0, "", 0, "HIIT"
        ));

        List<Exercise> pool = new ArrayList<>();
        for (String group : muscleGroups) {
            pool.addAll(filterAvailable(ExerciseLoader.getByMuscleGroup(group)));
        }

        List<Exercise> cardioPool = ExerciseLoader.getByMuscleGroup("Cardio");
        List<Exercise> absPool = ExerciseLoader.getByMuscleGroup("Abs");
        absPool = ExerciseClassifier.filterByExperience(absPool, profile.getExperienceLevel());

        List<Exercise> circuitExercises = new ArrayList<>();

        Exercise cardio = pickNewRandom(cardioPool);
        if (cardio != null) circuitExercises.add(cardio);

        List<Exercise> compounds = ExerciseClassifier.filterCompounds(pool);
        circuitExercises.addAll(pickNewRandomMultiple(compounds, 2));

        List<Exercise> bodyweight = pool.stream()
            .filter(e -> ExerciseClassifier.isBeginner(e) || e.getName().toLowerCase().contains("body weight"))
            .toList();
        if (!bodyweight.isEmpty()) {
            Exercise bw = pickNewRandom(new ArrayList<>(bodyweight));
            if (bw != null) circuitExercises.add(bw);
        }

        if (!absPool.isEmpty()) {
            Exercise abs = pickNewRandom(absPool);
            if (abs != null) circuitExercises.add(abs);
        }

        int fill = circuitSize - circuitExercises.size();
        if (fill > 0) {
            circuitExercises.addAll(pickNewRandomMultiple(pool, fill));
        }

        for (Exercise ex : circuitExercises) {
            result.add(buildExercise(ex.getName(), rounds, "30s", 15, "Superset"));
        }

        result.add(new ProgramExercise("── FINISHER ──", 0, "", 0, ""));
        Exercise finisher = pickNewRandom(cardioPool);
        if (finisher != null) {
            result.add(new ProgramExercise(finisher.getName(), 1, "5 min max effort", 0, "Burnout"));
        }

        return result;
    }

    // ========== MAIN COMPOUND: sets/reps/rest ==========

    private int mainCompoundSets() {
        List<String> physical = goal.getPhysicalGoals();
        String age = getAgeGroup();

        if (age.equals("ung")) return 3;
        if (age.equals("ældre")) return 3;

        if (physical.contains("Stærkere")) return 5;
        if (physical.contains("Større") || goal.getGoalType().equals("Bulk")) return 4;
        return 4;
    }

    private String mainCompoundReps() {
        List<String> physical = goal.getPhysicalGoals();
        String age = getAgeGroup();

        if (age.equals("ung")) return "10-12";
        if (age.equals("ældre")) return "10-15";

        if (physical.contains("Stærkere")) return "3-5";
        if (physical.contains("Større")) return "5-8";
        if (goal.getGoalType().equals("Bulk")) return "5-8";
        if (goal.getGoalType().equals("Cut")) return "6-8";
        return "6-8";
    }

    private int mainCompoundRest() {
        List<String> physical = goal.getPhysicalGoals();
        String age = getAgeGroup();

        if (age.equals("ældre")) return 150;

        if (physical.contains("Stærkere")) return 180;
        if (goal.getGoalType().equals("Bulk") || physical.contains("Større")) return 150;
        return 120;
    }

    // ========== ACCESSORY COMPOUND: sets/reps/rest ==========

    private int accessoryCompoundSets() {
        if (prefs.getTrainingStyle().equals("Bodybuilder")) return 4;
        return 3;
    }

    private String accessoryCompoundReps() {
        List<String> physical = goal.getPhysicalGoals();
        if (physical.contains("Stærkere")) return "6-8";
        if (physical.contains("Større") || prefs.getTrainingStyle().equals("Bodybuilder")) return "8-10";
        if (goal.getGoalType().equals("Cut") || physical.contains("Mere toned")) return "10-12";
        return "8-10";
    }

    private int accessoryCompoundRest() {
        if (goal.getGoalType().equals("Cut")) return 75;
        return 90;
    }

    // ========== ISOLATION: sets/reps/rest ==========

    private int isolationSets() {
        String age = getAgeGroup();
        if (age.equals("ung") || age.equals("ældre")) return 3;

        if (prefs.getTrainingStyle().equals("Bodybuilder") || goal.getPhysicalGoals().contains("Større")) return 4;
        if (goal.getGoalType().equals("Cut")) return 3;
        return 3;
    }

    private String isolationReps() {
        List<String> physical = goal.getPhysicalGoals();
        String age = getAgeGroup();

        if (age.equals("ung")) return "12-15";
        if (age.equals("ældre")) return "12-15";

        if (physical.contains("Mere toned") || goal.getGoalType().equals("Cut")) return "12-15";
        if (physical.contains("Større") || prefs.getTrainingStyle().equals("Bodybuilder")) return "10-12";
        return "10-12";
    }

    private int isolationRest() {
        String age = getAgeGroup();
        if (age.equals("ældre")) return 90;

        if (goal.getGoalType().equals("Cut") || goal.getPhysicalGoals().contains("Mere toned")) return 45;
        if (prefs.getTrainingStyle().equals("Bodybuilder")) return 60;
        return 60;
    }

    // ========== CARDIO & ABS ==========

    private boolean shouldAddCardio() {
        return prefs.getFocus().equals("Cardio")
            || prefs.getFocus().equals("Blanding af alt")
            || goal.getGoalType().equals("Cut");
    }

    private ProgramExercise buildCardioBlock() {
        List<Exercise> cardio = ExerciseLoader.getByMuscleGroup("Cardio");
        String name = cardio.isEmpty() ? "Cardio efter eget valg"
            : cardio.get(random.nextInt(cardio.size())).getName();

        if (goal.getGoalType().equals("Cut")) {
            return new ProgramExercise(name, 1, "15 min intervaller (30s on / 30s off)", 0, "HIIT Cardio");
        }
        return new ProgramExercise(name, 1, "15-20 min steady state", 0, "Cardio");
    }

    private boolean shouldAddAbs(String dayFocus) {
        if (dayFocus.contains("Legs") || dayFocus.contains("Lower") || dayFocus.contains("Full")) return false;
        return prefs.getTrainingStyle().equals("Atlet") || goal.getPhysicalGoals().contains("Mere toned");
    }

    private List<ProgramExercise> buildAbsFinisher() {
        List<Exercise> abs = ExerciseLoader.getByMuscleGroup("Abs");
        abs = ExerciseClassifier.filterByExperience(abs, profile.getExperienceLevel());
        List<ProgramExercise> result = new ArrayList<>();
        for (Exercise ex : pickNewRandomMultiple(abs, 2)) {
            result.add(buildExercise(ex.getName(), 3, "12-15", 45, "Abs"));
        }
        return result;
    }

    // ========== SPLIT LOGIC ==========

    private String[][] determineSplit() {
        int days = prefs.getDaysPerWeek();
        boolean isKvinde = profile.getGender().equals("Kvinde");

        if (days == 1) {
            if (isKvinde) {
                return new String[][]{
                    {"Full Body", "Glutes", "Legs", "Back", "Shoulders", "Chest"}
                };
            }
            return new String[][]{
                {"Full Body", "Chest", "Back", "Legs", "Shoulders", "Glutes"}
            };
        }

        if (days <= 3) {
            String[][] split = new String[days][];
            for (int i = 0; i < days; i++) {
                if (isKvinde) {
                    split[i] = new String[]{"Full Body", "Glutes", "Legs", "Back", "Shoulders", "Chest"};
                } else {
                    split[i] = new String[]{"Full Body", "Chest", "Back", "Legs", "Shoulders", "Glutes"};
                }
            }
            return split;
        }

        if (days == 4) {
            if (isKvinde) {
                return new String[][]{
                    {"Upper Body", "Back", "Shoulders", "Chest", "Biceps", "Triceps"},
                    {"Lower Body", "Glutes", "Legs", "Calves", "Abs"},
                    {"Upper Body", "Back", "Shoulders", "Chest", "Biceps", "Triceps"},
                    {"Lower Body (Glute-fokus)", "Glutes", "Legs", "Calves", "Abs"}
                };
            }
            return new String[][]{
                {"Upper Body", "Chest", "Back", "Shoulders", "Biceps", "Triceps"},
                {"Lower Body", "Legs", "Glutes", "Calves"},
                {"Upper Body", "Chest", "Back", "Shoulders", "Biceps", "Triceps"},
                {"Lower Body", "Legs", "Glutes", "Calves"}
            };
        }

        if (days == 5) {
            if (isKvinde) {
                return new String[][]{
                    {"Push", "Shoulders", "Chest", "Triceps"},
                    {"Pull", "Back", "Biceps"},
                    {"Legs & Glutes", "Glutes", "Legs", "Calves"},
                    {"Upper Body", "Back", "Shoulders", "Chest", "Biceps", "Triceps"},
                    {"Legs & Glutes", "Glutes", "Legs", "Calves"}
                };
            }
            return new String[][]{
                {"Push", "Chest", "Shoulders", "Triceps"},
                {"Pull", "Back", "Biceps"},
                {"Legs", "Legs", "Glutes", "Calves"},
                {"Push", "Chest", "Shoulders", "Triceps"},
                {"Pull", "Back", "Biceps"}
            };
        }

        if (days == 7) {
            if (isKvinde) {
                return new String[][]{
                    {"Push", "Shoulders", "Chest", "Triceps"},
                    {"Pull", "Back", "Biceps"},
                    {"Legs & Glutes", "Glutes", "Legs", "Calves"},
                    {"Push", "Shoulders", "Chest", "Triceps"},
                    {"Pull", "Back", "Biceps"},
                    {"Legs & Glutes", "Glutes", "Legs", "Calves"},
                    {"Full Body", "Glutes", "Legs", "Back", "Shoulders", "Chest"}
                };
            }
            return new String[][]{
                {"Push", "Chest", "Shoulders", "Triceps"},
                {"Pull", "Back", "Biceps"},
                {"Legs", "Legs", "Glutes", "Calves"},
                {"Push", "Chest", "Shoulders", "Triceps"},
                {"Pull", "Back", "Biceps"},
                {"Legs", "Legs", "Glutes", "Calves"},
                {"Full Body", "Chest", "Back", "Legs", "Shoulders", "Glutes"}
            };
        }

        if (isKvinde) {
            return new String[][]{
                {"Push", "Shoulders", "Chest", "Triceps"},
                {"Pull", "Back", "Biceps"},
                {"Legs & Glutes", "Glutes", "Legs", "Calves"},
                {"Push", "Shoulders", "Chest", "Triceps"},
                {"Pull", "Back", "Biceps"},
                {"Legs & Glutes", "Glutes", "Legs", "Calves"}
            };
        }
        return new String[][]{
            {"Push", "Chest", "Shoulders", "Triceps"},
            {"Pull", "Back", "Biceps"},
            {"Legs", "Legs", "Glutes", "Calves"},
            {"Push", "Chest", "Shoulders", "Triceps"},
            {"Pull", "Back", "Biceps"},
            {"Legs", "Legs", "Glutes", "Calves"}
        };
    }

    // ========== UTILITIES ==========

    private ProgramExercise buildExercise(String name, int sets, String reps, int rest, String tag) {
        if (sets <= 0) {
            return new ProgramExercise(name, sets, reps, rest, tag, -1f);
        }
        float weight = WeightSuggester.suggest(profile, goal, name, tag);
        return new ProgramExercise(name, sets, reps, rest, tag, weight);
    }

    private List<Exercise> filterAvailable(List<Exercise> exercises) {
        List<Exercise> filtered = ExerciseClassifier.filterByExperience(exercises, profile.getExperienceLevel());
        filtered = ExerciseClassifier.filterByAge(filtered, profile.getAge());
        if (profile.getAge() >= 50) {
            filtered = ExerciseClassifier.preferJointFriendly(filtered);
        }
        return filtered;
    }

    private String getAgeGroup() {
        int age = profile.getAge();
        if (age < 18) return "ung";
        if (age <= 35) return "voksen";
        if (age <= 50) return "senior";
        return "ældre";
    }

    private Exercise pickMainLift(List<Exercise> source) {
        if (wantsVariation) {
            return pickUniqueRandom(source, usedExercises);
        }
        List<Exercise> unused = source.stream()
            .filter(e -> !usedMainLifts.contains(e.getName()))
            .toList();
        if (unused.isEmpty()) {
            if (source.isEmpty()) return null;
            return source.get(random.nextInt(source.size()));
        }
        Exercise picked = unused.get(random.nextInt(unused.size()));
        usedMainLifts.add(picked.getName());
        return picked;
    }

    private Exercise pickNewRandom(List<Exercise> source) {
        return pickUniqueRandom(source, usedExercises);
    }

    private Exercise pickUniqueRandom(List<Exercise> source, Set<String> used) {
        List<Exercise> unused = source.stream()
            .filter(e -> !used.contains(e.getName()))
            .toList();

        if (unused.isEmpty()) {
            if (source.isEmpty()) return null;
            unused = source;
        }

        Exercise picked = unused.get(random.nextInt(unused.size()));
        used.add(picked.getName());
        return picked;
    }

    private List<Exercise> pickNewRandomMultiple(List<Exercise> source, int count) {
        List<Exercise> unused = new ArrayList<>(source.stream()
            .filter(e -> !usedExercises.contains(e.getName()))
            .toList());
        Collections.shuffle(unused, random);

        List<Exercise> result = new ArrayList<>();
        for (int i = 0; i < Math.min(count, unused.size()); i++) {
            Exercise ex = unused.get(i);
            usedExercises.add(ex.getName());
            result.add(ex);
        }
        return result;
    }

    private int exercisesForDuration(int minutes) {
        if (minutes <= 30) return 3;
        if (minutes <= 45) return 4;
        if (minutes <= 60) return 5;
        return 7;
    }
}
