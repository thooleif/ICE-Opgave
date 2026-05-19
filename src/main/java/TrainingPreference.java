import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class TrainingPreference {

    private static final String PREFS_FILE = "Data/TrainingPreferences.csv";

    private String focus;
    private String trainingStyle;
    private boolean wantsStrength;
    private boolean wantsCardio;
    private int trainingDaysPerWeek;
    private int sessionDurationMin;

    public TrainingPreference(String focus,
                              String trainingStyle,
                              boolean wantsStrength,
                              boolean wantsCardio,
                              int trainingDaysPerWeek,
                              int sessionDurationMin) {
        this.focus = focus;
        this.trainingStyle = trainingStyle;
        this.wantsStrength = wantsStrength;
        this.wantsCardio = wantsCardio;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.sessionDurationMin = sessionDurationMin;
    }

    public String getFocus() { return focus; }
    public String getTrainingStyle() { return trainingStyle; }
    public boolean getWantsStrength() { return wantsStrength; }
    public boolean getWantsCardio() { return wantsCardio; }
    public int getTrainingDaysPerWeek() { return trainingDaysPerWeek; }
    public int getSessionDurationMin() { return sessionDurationMin; }

    public void setFocus(String focus) { this.focus = focus; }
    public void setTrainingStyle(String trainingStyle) { this.trainingStyle = trainingStyle; }
    public void setWantsStrength(boolean wantsStrength) { this.wantsStrength = wantsStrength; }
    public void setWantsCardio(boolean wantsCardio) { this.wantsCardio = wantsCardio; }
    public void setTrainingDaysPerWeek(int trainingDaysPerWeek) { this.trainingDaysPerWeek = trainingDaysPerWeek; }
    public void setSessionDurationMin(int sessionDurationMin) { this.sessionDurationMin = sessionDurationMin; }

    /**
     * Fokus som programgeneratoren skal bruge — tager hænsyn til styrke/cardio-valg.
     */
    public String resolveGeneratorFocus() {
        if ("HIIT".equals(focus)) {
            return "HIIT";
        }
        if ("Blanding af alt".equals(focus)) {
            return "Blanding af alt";
        }
        if (wantsStrength && wantsCardio) {
            return "Blanding af alt";
        }
        if (wantsCardio && !wantsStrength) {
            return "Cardio";
        }
        if ("Cardio".equals(focus) && wantsStrength) {
            return "Blanding af alt";
        }
        return "Styrke";
    }

    public String getRecommendedSplit() {
        int days = trainingDaysPerWeek;

        if ("HIIT".equals(focus)) {
            return "HIIT-circuits " + days + " dage/uge (" + sessionDurationMin + " min/session)";
        }
        if ("Cardio".equals(focus) && !wantsStrength) {
            return "Cardio-sessioner (løb, cykel, intervaller) " + days + " gange/uge";
        }
        if ("Blanding af alt".equals(focus) || (wantsStrength && wantsCardio)) {
            return "Mix af styrke og cardio " + days + " dage/uge";
        }
        if ("Styrke".equals(focus) && "Atlet".equals(trainingStyle)) {
            if (days <= 3) {
                return "Full Body (squat / bench / deadlift-fokus)";
            }
            return "Upper / Lower med fokus på de store løft";
        }
        if ("Bodybuilder".equals(trainingStyle)) {
            if (days <= 3) {
                return "Full Body x" + days;
            } else if (days == 4) {
                return "Upper / Lower x2";
            }
            return "Push / Pull / Legs (PPL)";
        }
        if (days <= 3) {
            return "Full Body " + days + "x/uge";
        }
        return "Upper / Lower eller Push/Pull split";
    }

    private String toCsvLine(UUID userId) {
        return userId + ";" +
                focus + ";" +
                trainingStyle + ";" +
                wantsStrength + ";" +
                wantsCardio + ";" +
                trainingDaysPerWeek + ";" +
                sessionDurationMin + "\n";
    }

    private static boolean existsInFile(UUID userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PREFS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 0 && parts[0].equals(userId.toString())) {
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
        try (FileWriter writer = new FileWriter(PREFS_FILE, true)) {
            writer.write(toCsvLine(userId));
            System.out.println("Preferences saved.");
        } catch (IOException e) {
            System.out.println("Error saving preferences: " + e.getMessage());
        }
    }

    public void update(UUID userId) {
        StringBuilder sb = new StringBuilder();
        boolean foundUser = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(PREFS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length > 0 && parts[0].equals(userId.toString())) {
                    sb.append(toCsvLine(userId));
                    foundUser = true;
                } else {
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading preferences: " + e.getMessage());
            return;
        }

        if (!foundUser) {
            sb.append(toCsvLine(userId));
        }

        try (FileWriter writer = new FileWriter(PREFS_FILE, false)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Error updating preferences: " + e.getMessage());
        }
    }

    public static TrainingPreference load(UUID userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PREFS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 5 || !parts[0].equals(userId.toString())) {
                    continue;
                }

                if (isLegacyV1Format(parts)) {
                    return fromLegacyV1(parts);
                }
                if (parts.length == 5) {
                    return fromSimpleV2(parts);
                }
                if (parts.length >= 7) {
                    return new TrainingPreference(
                            parts[1],
                            parts[2],
                            Boolean.parseBoolean(parts[3]),
                            Boolean.parseBoolean(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6])
                    );
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading preferences: " + e.getMessage());
        }
        return null;
    }

    private static boolean isLegacyV1Format(String[] parts) {
        return parts.length >= 6
                && ("true".equalsIgnoreCase(parts[1]) || "false".equalsIgnoreCase(parts[1]));
    }

    private static TrainingPreference fromLegacyV1(String[] parts) {
        boolean strength = Boolean.parseBoolean(parts[1]);
        boolean cardio = Boolean.parseBoolean(parts[2]);
        ProgramFocus oldFocus = ProgramFocus.valueOf(parts[3]);
        int days = Integer.parseInt(parts[4]);
        int duration = Integer.parseInt(parts[5]);

        String focus;
        String style;

        if (oldFocus == ProgramFocus.CARDIO && !strength) {
            focus = duration <= 35 ? "HIIT" : "Cardio";
            style = "Atlet";
        } else if (oldFocus == ProgramFocus.POWERLIFTING) {
            focus = "Styrke";
            style = "Atlet";
        } else if (oldFocus == ProgramFocus.BODYBUILDING) {
            focus = "Styrke";
            style = "Bodybuilder";
        } else if (strength && cardio) {
            focus = "Blanding af alt";
            style = "Atlet";
        } else if (cardio) {
            focus = "Cardio";
            style = "Atlet";
        } else {
            focus = "Styrke";
            style = strength ? "Bodybuilder" : "Atlet";
        }

        return new TrainingPreference(focus, style, strength, cardio, days, duration);
    }

    private static TrainingPreference fromSimpleV2(String[] parts) {
        String focus = parts[1];
        String style = parts[2];
        int days = Integer.parseInt(parts[3]);
        int duration = Integer.parseInt(parts[4]);
        boolean strength = inferWantsStrength(focus, style);
        boolean cardio = inferWantsCardio(focus);
        return new TrainingPreference(focus, style, strength, cardio, days, duration);
    }

    private static boolean inferWantsStrength(String focus, String style) {
        return !"Cardio".equals(focus) && !"HIIT".equals(focus);
    }

    private static boolean inferWantsCardio(String focus) {
        return "Cardio".equals(focus) || "HIIT".equals(focus) || "Blanding af alt".equals(focus);
    }

    private enum ProgramFocus {
        CARDIO, POWERLIFTING, BODYBUILDING, GENERAL_LIFESTYLE
    }

    @Override
    public String toString() {
        return "TrainingPreference{" +
                "focus='" + focus + '\'' +
                ", trainingStyle='" + trainingStyle + '\'' +
                ", wantsStrength=" + wantsStrength +
                ", wantsCardio=" + wantsCardio +
                ", trainingDaysPerWeek=" + trainingDaysPerWeek +
                ", sessionDurationMin=" + sessionDurationMin +
                ", generatorFocus='" + resolveGeneratorFocus() + '\'' +
                '}';
    }
}
