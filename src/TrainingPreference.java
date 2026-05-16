import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class TrainingPreference {

    // ProgramFocus dækker de fire stilarter brugeren kan vælge i menuen
    // GENERAL_LIFESTYLE er bare "normalt aktiv" - ikke nogen specifik sport
    public enum ProgramFocus {
        CARDIO,
        POWERLIFTING,
        BODYBUILDING,
        GENERAL_LIFESTYLE
    }

    // CSV-filen hvor præferencer gemmes - statisk så alle instanser bruger samme fil
    private static final String PREFS_FILE = "Data/TrainingPreferences.csv";

    // Booleans bruges så man kan trænene styrke OG cardio samtidig
    // F.eks. powerlifter der også løber lidt for konditionen
    private boolean wantsStrength;
    private boolean wantsCardio;
    private ProgramFocus programFocus;
    private int trainingDaysPerWeek;
    private int sessionDurationMin;

    // Constructor
    public TrainingPreference(boolean wantsStrength,
                              boolean wantsCardio,
                              ProgramFocus programFocus,
                              int trainingDaysPerWeek,
                              int sessionDurationMin) {

        this.wantsStrength = wantsStrength;
        this.wantsCardio = wantsCardio;
        this.programFocus = programFocus;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.sessionDurationMin = sessionDurationMin;
    }

    // Sætter alle præferencer på én gang - bruges når brugeren laver sin profil første gang
    public void setPreferences(boolean wantsStrength,
                               boolean wantsCardio,
                               ProgramFocus programFocus,
                               int trainingDaysPerWeek,
                               int sessionDurationMin) {

        this.wantsStrength = wantsStrength;
        this.wantsCardio = wantsCardio;
        this.programFocus = programFocus;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.sessionDurationMin = sessionDurationMin;

        System.out.println("Preferences saved.");
    }

    // Samme som setPreferences men med en anden besked - bruges fra update-menuen
    public void updatePreferences(boolean wantsStrength,
                                  boolean wantsCardio,
                                  ProgramFocus programFocus,
                                  int trainingDaysPerWeek,
                                  int sessionDurationMin) {

        this.wantsStrength = wantsStrength;
        this.wantsCardio = wantsCardio;
        this.programFocus = programFocus;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.sessionDurationMin = sessionDurationMin;

        System.out.println("Preferences updated.");
    }

    // Giver et forslag til hvordan ugen skal struktureres baseret på fokus og antal dage
    // Det er bare en simpel tekst-anbefaling, ikke et rigtig program endnu
    public String getRecommendedSplit() {

        if (programFocus == ProgramFocus.POWERLIFTING) {

            if (trainingDaysPerWeek <= 3) {
                return "Full Body (Squat / Bench / Deadlift focus)";
            } else {
                return "Upper / Lower split med focus on the big 3";
            }

        } else if (programFocus == ProgramFocus.BODYBUILDING) {

            if (trainingDaysPerWeek <= 3) {
                return "Full Body x3";
            } else if (trainingDaysPerWeek == 4) {
                return "Upper / Lower x2";
            } else {
                return "Push / Pull / Legs (PPL)";
            }

        } else if (programFocus == ProgramFocus.CARDIO) {

            return "Cardio sessions (run, biking, intervals) " + trainingDaysPerWeek + " times a week";

        } else {

            // General lifestyle - bare en blanding så man holder sig sund
            return "mix of strength and cardio " + trainingDaysPerWeek + " days";
        }
    }


    // CSV - gemmer og indlæser præferencer fra fil
    // Lagt i klassen selv så Menu ikke skal kende til filformatet


    // Lille helper der laver én CSV-linje for denne præference
    // private fordi det er en intern detalje - kun klassen selv skal bruge den
    private String toCsvLine(UUID userId) {
        return userId + ";" +
                wantsStrength + ";" +
                wantsCardio + ";" +
                programFocus + ";" +
                trainingDaysPerWeek + ";" +
                sessionDurationMin + "\n";
    }

    // Tjekker om der allerede ligger en linje for brugeren - bruges for at undgå duplikater
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

    // Gemmer denne præference for en given bruger - hvis der allerede er en linje
    // så kalder vi update i stedet for at appende en ny linje
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

    // Overskriver brugerens linje - læser hele filen ind først, skriver bagefter
    // for at undgå reader og writer åbne samtidig (samme grund som i Menu's profil-kode)
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

    // Indlæser præferencen for en bruger - returnerer null hvis ikke fundet
    // Static fordi vi ikke har et objekt at indlæse på endnu når vi kalder den
    public static TrainingPreference load(UUID userId) {

        try (BufferedReader reader = new BufferedReader(new FileReader(PREFS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 6 && parts[0].equals(userId.toString())) {

                    boolean strength = Boolean.parseBoolean(parts[1]);
                    boolean cardio = Boolean.parseBoolean(parts[2]);
                    ProgramFocus focus = ProgramFocus.valueOf(parts[3]);
                    int days = Integer.parseInt(parts[4]);
                    int duration = Integer.parseInt(parts[5]);

                    return new TrainingPreference(strength, cardio, focus, days, duration);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading preferences: " + e.getMessage());
        }
        return null;
    }

    // Getters
    public boolean getWantsStrength() {
        return wantsStrength;
    }

    public boolean getWantsCardio() {
        return wantsCardio;
    }

    public ProgramFocus getProgramFocus() {
        return programFocus;
    }

    public int getTrainingDaysPerWeek() {
        return trainingDaysPerWeek;
    }

    public int getSessionDurationMin() {
        return sessionDurationMin;
    }

    // Setters - bruges enkeltvis fra update-menuen
    public void setWantsStrength(boolean b) {
        this.wantsStrength = b;
    }

    public void setWantsCardio(boolean b) {
        this.wantsCardio = b;
    }

    public void setProgramFocus(ProgramFocus p) {
        this.programFocus = p;
    }

    public void setTrainingDaysPerWeek(int d) {
        this.trainingDaysPerWeek = d;
    }

    public void setSessionDurationMin(int m) {
        this.sessionDurationMin = m;
    }

    @Override
    public String toString() {

        return "TrainingPreference{" +
                "wantsStrength=" + wantsStrength +
                ", wantsCardio=" + wantsCardio +
                ", programFocus=" + programFocus +
                ", trainingDaysPerWeek=" + trainingDaysPerWeek +
                ", sessionDurationMin=" + sessionDurationMin +
                '}';
    }
}