import model.ProgramExercise;
import model.TrainingDay;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SavedTrainingProgram {

    private static final String PROGRAMS_FILE = "Data/TrainingPrograms.csv";
    private static final String DAY_SEP = "||";
    private static final String EX_SEP = "|";
    private static final String FIELD_SEP = "~";

    private final List<TrainingDay> days;
    private final String generatedAt;
    private final String programInfo;

    public SavedTrainingProgram(List<TrainingDay> days, String generatedAt, String programInfo) {
        this.days = days != null ? new ArrayList<>(days) : new ArrayList<>();
        this.generatedAt = generatedAt;
        this.programInfo = programInfo != null ? programInfo : "";
    }

    public List<TrainingDay> getDays() {
        return days;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public String getProgramInfo() {
        return programInfo;
    }

    public int getDayCount() {
        return days.size();
    }

    public String getOverview() {
        if (days.isEmpty()) {
            return "No exercises in program.";
        }
        return days.size() + " training days | generated " + generatedAt;
    }

    private String encodeProgram() {
        StringBuilder sb = new StringBuilder();
        for (TrainingDay day : days) {
            sb.append("D").append(FIELD_SEP)
                    .append(safe(day.getDayName())).append(FIELD_SEP)
                    .append(safe(day.getFocus())).append(FIELD_SEP);
            for (ProgramExercise ex : day.getExercises()) {
                sb.append(safe(ex.getExerciseName())).append(FIELD_SEP)
                        .append(ex.getSets()).append(FIELD_SEP)
                        .append(safe(ex.getReps())).append(FIELD_SEP)
                        .append(ex.getRestTimeSec()).append(FIELD_SEP)
                        .append(safe(ex.getTag())).append(FIELD_SEP)
                        .append(ex.getWeightKg()).append(FIELD_SEP)
                        .append(ex.isBodyweight() ? "bw" : "").append(EX_SEP);
            }
            sb.append(DAY_SEP);
        }
        return sb.toString();
    }

    private static List<TrainingDay> decodeProgram(String encoded) {
        List<TrainingDay> result = new ArrayList<>();
        if (encoded == null || encoded.isBlank()) {
            return result;
        }

        for (String block : encoded.split("\\|\\|")) {
            if (block.isBlank()) {
                continue;
            }

            String[] header = block.split(FIELD_SEP, 4);
            if (header.length < 4 || !"D".equals(header[0])) {
                continue;
            }

            String dayName = unescape(header[1]);
            String focus = unescape(header[2]);
            String exerciseBlob = header[3];
            List<ProgramExercise> exercises = new ArrayList<>();

            if (!exerciseBlob.isBlank()) {
                for (String exChunk : exerciseBlob.split("\\|")) {
                    if (exChunk.isBlank()) {
                        continue;
                    }
                    String[] exParts = exChunk.split(FIELD_SEP);
                    if (exParts.length < 5) {
                        continue;
                    }
                    float weight = exParts.length >= 6 ? parseWeight(exParts[5]) : -1f;
                    boolean bw = exParts.length >= 7 && "bw".equalsIgnoreCase(exParts[6]);
                    ProgramExercise ex = new ProgramExercise(
                            unescape(exParts[0]),
                            Integer.parseInt(exParts[1]),
                            unescape(exParts[2]),
                            Integer.parseInt(exParts[3]),
                            unescape(exParts[4]),
                            weight
                    );
                    ex.setBodyweight(bw);
                    exercises.add(ex);
                }
            }

            result.add(new TrainingDay(dayName, focus, exercises));
        }
        return result;
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(FIELD_SEP, "-").replace("|", "/").replace(";", ",");
    }

    private static String unescape(String value) {
        return value == null ? "" : value;
    }

    private static float parseWeight(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return -1f;
        }
    }

    private String toCsvLine(UUID userId) {
        String safeInfo = programInfo.replace(";", ",").replace("\n", " ");
        return userId + ";" +
                generatedAt + ";" +
                safeInfo + ";" +
                encodeProgram() + "\n";
    }

    private static boolean existsInFile(UUID userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PROGRAMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(userId.toString() + ";")) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public void save(UUID userId) {
        if (existsInFile(userId)) {
            update(userId);
            return;
        }
        try (FileWriter writer = new FileWriter(PROGRAMS_FILE, true)) {
            writer.write(toCsvLine(userId));
            System.out.println("Training program saved.");
        } catch (IOException e) {
            System.out.println("Error saving training program: " + e.getMessage());
        }
    }

    public void update(UUID userId) {
        StringBuilder sb = new StringBuilder();
        boolean foundUser = false;
        String prefix = userId + ";";

        try (BufferedReader reader = new BufferedReader(new FileReader(PROGRAMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(prefix)) {
                    sb.append(toCsvLine(userId));
                    foundUser = true;
                } else {
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading training program: " + e.getMessage());
            return;
        }

        if (!foundUser) {
            sb.append(toCsvLine(userId));
        }

        try (FileWriter writer = new FileWriter(PROGRAMS_FILE, false)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Error updating training program: " + e.getMessage());
        }
    }

    public static SavedTrainingProgram load(UUID userId) {
        String prefix = userId + ";";
        try (BufferedReader reader = new BufferedReader(new FileReader(PROGRAMS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(prefix)) {
                    continue;
                }
                int first = line.indexOf(';');
                int second = line.indexOf(';', first + 1);
                int third = line.indexOf(';', second + 1);
                if (second < 0 || third < 0) {
                    return null;
                }

                String date = line.substring(first + 1, second);
                String info = line.substring(second + 1, third);
                String encoded = line.substring(third + 1);

                return new SavedTrainingProgram(decodeProgram(encoded), date, info);
            }
        } catch (IOException e) {
            System.out.println("Error loading training program: " + e.getMessage());
        }
        return null;
    }

    public static SavedTrainingProgram create(List<TrainingDay> days, String programInfo) {
        return new SavedTrainingProgram(days, LocalDate.now().toString(), programInfo);
    }

    @Override
    public String toString() {
        return "SavedTrainingProgram{" + getOverview() + "}";
    }
}
