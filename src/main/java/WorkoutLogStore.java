import model.WorkoutExerciseLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gemmer sidste træning per dag og personlige rekorder (CSV).
 */
public final class WorkoutLogStore {

    private static final String LAST_FILE = "Data/LastWorkouts.csv";
    private static final String PB_FILE = "Data/PersonalBests.csv";
    private static final String FIELD = "~";

    private WorkoutLogStore() {}

    public static void applyHistory(UUID userId, String dayName, List<WorkoutExerciseLog> logs) {
        Map<String, float[]> pbs = loadPersonalBests(userId);
        Map<String, String[]> last = loadLastForDay(userId, dayName);

        for (WorkoutExerciseLog log : logs) {
            if (!log.isLoggable()) {
                continue;
            }
            String key = log.getExerciseName();
            if (pbs.containsKey(key)) {
                log.setPb(pbs.get(key)[0], formatReps(pbs.get(key)[1]));
            }
            if (last.containsKey(key)) {
                String[] l = last.get(key);
                boolean bw = l.length >= 4 && "bw".equalsIgnoreCase(l[3]);
                log.setLast(parseIntSafe(l[0]), l[1], parseFloatSafe(l[2]), bw);
            }
        }
    }

    public static void saveCompletedWorkout(UUID userId, String dayName, List<WorkoutExerciseLog> logs) {
        String date = LocalDate.now().toString();
        saveLastWorkout(userId, dayName, logs, date);
        updatePersonalBests(userId, logs, date);
    }

    private static void saveLastWorkout(UUID userId, String dayName, List<WorkoutExerciseLog> logs, String date) {
        String prefix = userId + FIELD + safe(dayName) + FIELD;
        StringBuilder kept = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(LAST_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(prefix)) {
                    kept.append(line).append("\n");
                }
            }
        } catch (IOException ignored) {}

        for (WorkoutExerciseLog log : logs) {
            if (!log.isLoggable()) {
                continue;
            }
            kept.append(prefix)
                    .append(safe(log.getExerciseName())).append(FIELD)
                    .append(log.getCurrentSets()).append(FIELD)
                    .append(safe(log.getCurrentReps())).append(FIELD)
                    .append(log.getCurrentWeightKg() > 0 ? log.getCurrentWeightKg() : -1).append(FIELD)
                    .append(log.isCurrentBodyweight() ? "bw" : "").append(FIELD)
                    .append(date).append("\n");
        }

        try (FileWriter writer = new FileWriter(LAST_FILE, false)) {
            writer.write(kept.toString());
        } catch (IOException e) {
            System.out.println("Error saving last workout: " + e.getMessage());
        }
    }

    private static void updatePersonalBests(UUID userId, List<WorkoutExerciseLog> logs, String date) {
        Map<String, float[]> bests = loadPersonalBests(userId);

        for (WorkoutExerciseLog log : logs) {
            if (!log.isLoggable() || log.getCurrentWeightKg() <= 0) {
                continue;
            }
            String name = log.getExerciseName();
            float weight = log.getCurrentWeightKg();
            float repsNum = parseRepsNumber(log.getCurrentReps());
            if (!bests.containsKey(name) || weight > bests.get(name)[0]) {
                bests.put(name, new float[]{weight, repsNum, dateToFloat(date)});
            }
        }

        List<String> otherUsers = new ArrayList<>();
        String userPrefix = userId.toString() + FIELD;
        try (BufferedReader reader = new BufferedReader(new FileReader(PB_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(userPrefix)) {
                    otherUsers.add(line);
                }
            }
        } catch (IOException ignored) {}

        try (FileWriter writer = new FileWriter(PB_FILE, false)) {
            for (String line : otherUsers) {
                writer.write(line);
                if (!line.endsWith("\n")) {
                    writer.write("\n");
                }
            }
            for (Map.Entry<String, float[]> e : bests.entrySet()) {
                float[] v = e.getValue();
                writer.write(userPrefix + safe(e.getKey()) + FIELD + v[0] + FIELD + v[1] + FIELD
                        + formatDate(v[2]) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving personal bests: " + e.getMessage());
        }
    }

    private static Map<String, float[]> loadPersonalBests(UUID userId) {
        Map<String, float[]> map = new HashMap<>();
        String prefix = userId.toString() + FIELD;
        try (BufferedReader reader = new BufferedReader(new FileReader(PB_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(prefix)) {
                    continue;
                }
                String[] p = line.split(FIELD);
                if (p.length >= 5) {
                    map.put(unescape(p[1]), new float[]{
                            parseFloatSafe(p[2]),
                            parseFloatSafe(p[3]),
                            parseDateSafe(p[4])
                    });
                }
            }
        } catch (IOException ignored) {}
        return map;
    }

    private static Map<String, String[]> loadLastForDay(UUID userId, String dayName) {
        Map<String, String[]> map = new HashMap<>();
        String prefix = userId + FIELD + safe(dayName) + FIELD;
        try (BufferedReader reader = new BufferedReader(new FileReader(LAST_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(prefix)) {
                    continue;
                }
                String[] p = line.split(FIELD);
                if (p.length >= 6) {
                    String bwFlag = p.length >= 7 ? p[6] : "";
                    map.put(unescape(p[2]), new String[]{p[3], p[4], p[5], bwFlag});
                }
            }
        } catch (IOException ignored) {}
        return map;
    }

    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace(FIELD, "-").replace(";", ",");
    }

    private static String unescape(String s) {
        return s == null ? "" : s;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static float parseFloatSafe(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return -1f;
        }
    }

    private static float parseRepsNumber(String reps) {
        if (reps == null || reps.isBlank()) {
            return 0;
        }
        String digits = reps.replaceAll("[^0-9]", " ");
        String[] parts = digits.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return 0;
        }
        try {
            return Float.parseFloat(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String formatReps(float repsNum) {
        if (repsNum <= 0) {
            return "";
        }
        if (repsNum == (int) repsNum) {
            return String.valueOf((int) repsNum);
        }
        return String.valueOf(repsNum);
    }

    private static float parseDateSafe(String date) {
        try {
            return LocalDate.parse(date).toEpochDay();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String formatDate(float epochDay) {
        if (epochDay <= 0) {
            return LocalDate.now().toString();
        }
        return LocalDate.ofEpochDay((long) epochDay).toString();
    }

    private static float dateToFloat(String date) {
        try {
            return LocalDate.parse(date).toEpochDay();
        } catch (Exception e) {
            return 0;
        }
    }
}
