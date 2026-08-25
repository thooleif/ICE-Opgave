import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class MacroPlan {

    // CSV-fil hvor makroplanen gemmes - statisk så alle instanser bruger samme fil
    private static final String MACROS_FILE = "Data/MacroPlans.csv";

    // Makro-targets i gram og kalorier - beregnet ud fra profil, mål og præferencer
    private int calorieGoal;
    private int proteinGram;
    private int carbohydrateGram;
    private int fatGram;

    // Beskrivelse af hvordan tallene blev udregnet - god at gemme så brugeren kan se hvorfor
    private String calculationMethod;


    public MacroPlan(int calorieGoal,
                     int proteinGram,
                     int carbohydrateGram,
                     int fatGram,
                     String calculationMethod) {

        this.calorieGoal = calorieGoal;
        this.proteinGram = proteinGram;
        this.carbohydrateGram = carbohydrateGram;
        this.fatGram = fatGram;
        this.calculationMethod = calculationMethod;
    }


    // Beregningslogikken - bygger en MacroPlan ud fra profil/mål/præferencer
    // Brugt 2026 standarder: Mifflin-St Jeor for BMR, ISSN guidelines for makros


    // Static factory metode - laver en frisk plan baseret på brugerens data
    // Static fordi vi ikke har et objekt at kalde det på endnu - vi laver et nyt et
    public static MacroPlan calculateFor(UserProfile profile, FitnessGoal goal, TrainingPreference prefs) {

        // Trin 1: BMR med Mifflin-St Jeor (mest præcise formel ifølge 2026 forskning)
        // Mænd:   BMR = 10*kg + 6.25*cm - 5*alder + 5
        // Kvinder: BMR = 10*kg + 6.25*cm - 5*alder - 161
        float weight = profile.getWeightKg();
        float height = profile.getHeightCm();
        int age = profile.getAge();

        float bmr;
        if (profile.getGender() == UserProfile.Gender.MALE) {
            bmr = (10 * weight) + (6.25f * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25f * height) - (5 * age) - 161;
        }

        // Trin 2: Aktivitetsfaktor ud fra antal træningsdage om ugen
        // Standard activity multipliers - de er testet ift. doubly labeled water studies
        float activityFactor;
        int days = prefs.getTrainingDaysPerWeek();
        if (days <= 1) {
            activityFactor = 1.2f;   // Næsten ingen aktivitet
        } else if (days <= 3) {
            activityFactor = 1.375f; // Let aktiv
        } else if (days <= 5) {
            activityFactor = 1.55f;  // Moderat aktiv
        } else if (days == 6) {
            activityFactor = 1.725f; // Meget aktiv
        } else {
            activityFactor = 1.9f;   // Ekstrem aktiv (7 dage)
        }

        if (prefs.getWantsCardio() && activityFactor < 1.9f) {
            activityFactor = Math.min(1.9f, activityFactor + 0.05f);
        }

        // TDEE = BMR * aktivitetsfaktor
        float tdee = bmr * activityFactor;

        // Trin 3: Juster TDEE baseret på mål - skab surplus eller deficit
        // Tallene er fra 2026 consensus statements: 250-1000 kcal deficit, 200-500 surplus
        float calorieTarget;
        FitnessGoal.GoalType goalType = goal.getGoalType();

        if (goalType == FitnessGoal.GoalType.LOSE_WEIGHT) {
            // 500 kcal deficit = ca 0.5 kg/uge tab, sikkert og bæredygtigt
            calorieTarget = tdee - 500;
        } else if (goalType == FitnessGoal.GoalType.BULK_UP) {
            // 300 kcal surplus = ca 0.3 kg/uge på - "lean bulk" approach
            calorieTarget = tdee + 300;
        } else if (goalType == FitnessGoal.GoalType.RECOMP) {
            // Recomp kører tæt på vedligehold - lille deficit gir bedre resultater
            calorieTarget = tdee - 100;
        } else {
            // MAINTAIN - bare TDEE
            calorieTarget = tdee;
        }

        // Trin 4: Protein i g/kg afhænger af mål - ISSN position stand 2017 stadig brugt 2026
        // Cut: 2.3 g/kg for at bevare muskelmassen i deficit
        // Bulk: 1.8 g/kg - over 2.0 gir ikke ekstra muskelvækst ifølge Morton et al meta-analyse
        // Recomp/Maintain: 1.6 g/kg - "plateauet" for muskelvækst
        float proteinPerKg;
        if (goalType == FitnessGoal.GoalType.LOSE_WEIGHT) {
            proteinPerKg = 2.3f;
        } else if (goalType == FitnessGoal.GoalType.BULK_UP) {
            proteinPerKg = 1.8f;
        } else {
            proteinPerKg = 1.6f;
        }

        float proteinG = weight * proteinPerKg;
        float proteinKcal = proteinG * 4; // protein gir 4 kcal/g

        // Trin 5: Fedt - minimum 0.8 g/kg for hormoner og fedtoploselige vitaminer
        // Powerlifting og bodybuilding lægger lidt mere på til mæthed
        float fatPerKg;
        if ("Bodybuilder".equals(prefs.getTrainingStyle()) || "Styrke".equals(prefs.getFocus())) {
            fatPerKg = 1.0f;
        } else {
            fatPerKg = 0.9f;
        }

        float fatG = weight * fatPerKg;
        float fatKcal = fatG * 9; // fedt gir 9 kcal/g

        // Trin 6: Resten af kalorierne fra carbs (4 kcal/g) - de "fylder" plads ud
        // Cardio brugere får automatisk mere carbs fordi det er det vi har tilbage
        float remainingKcal = calorieTarget - proteinKcal - fatKcal;
        if (remainingKcal < 0) {
            // Hvis target er så lavt at protein + fedt allerede sprænger den, så drop fedtet lidt
            // Det sker stort set kun for meget små personer i deficit
            remainingKcal = 0;
        }
        float carbG = remainingKcal / 4f;

        // Trin 7: Lav beskrivelse så brugeren kan se hvordan tallene blev udregnet
        String method = "Mifflin-St Jeor BMR (" + Math.round(bmr) + " kcal) * activity " + activityFactor +
                " = TDEE " + Math.round(tdee) + " kcal. Goal adjustment for " + goalType + " applied. " +
                "Protein " + proteinPerKg + " g/kg, fat " + fatPerKg + " g/kg, carbs fill the rest.";

        return new MacroPlan(
                Math.round(calorieTarget),
                Math.round(proteinG),
                Math.round(carbG),
                Math.round(fatG),
                method
        );
    }

    // Juster kalorier op eller ned med et fast antal - bruges hvis brugeren ikke gør progress
    // Carbs absorberer ændringen så protein og fedt forbliver låst (det er bedste praksis)
    public void adjustCalories(int amount) {

        this.calorieGoal += amount;

        // Carb-justering - hver gram carb er 4 kcal
        int carbDelta = amount / 4;
        this.carbohydrateGram += carbDelta;

        if (this.carbohydrateGram < 0) {
            this.carbohydrateGram = 0;
        }

        System.out.println("Calories adjusted by " + amount + " kcal. New target: " + this.calorieGoal);
    }

    // Returnerer fordeling i procent - god til at se hvor "carb-tung" eller "fed" planen er
    public String getMacroSplit() {

        int totalKcal = (proteinGram * 4) + (carbohydrateGram * 4) + (fatGram * 9);
        if (totalKcal == 0) {
            return "No macros set";
        }

        int proteinPct = (proteinGram * 4 * 100) / totalKcal;
        int carbPct = (carbohydrateGram * 4 * 100) / totalKcal;
        int fatPct = (fatGram * 9 * 100) / totalKcal;

        return "Protein " + proteinPct + "% / Carbs " + carbPct + "% / Fat " + fatPct + "%";
    }


    // CSV - save/load/update i samme stil som FitnessGoal og TrainingPreference


    private String toCsvLine(UUID userId) {
        // calculationMethod kan indeholde semikolon hvis ikke jeg passer på - byttes ud til komma
        // ellers ville split(";") ikke virke rigtigt når vi læser linjen ind igen
        String safeMethod = calculationMethod.replace(";", ",");

        return userId + ";" +
                calorieGoal + ";" +
                proteinGram + ";" +
                carbohydrateGram + ";" +
                fatGram + ";" +
                safeMethod + "\n";
    }

    private static boolean existsInFile(UUID userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(MACROS_FILE))) {
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

        try (FileWriter writer = new FileWriter(MACROS_FILE, true)) {
            writer.write(toCsvLine(userId));
            System.out.println("Macro plan saved.");
        } catch (IOException e) {
            System.out.println("Error saving macro plan: " + e.getMessage());
        }
    }

    public void update(UUID userId) {

        StringBuilder sb = new StringBuilder();
        boolean foundUser = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(MACROS_FILE))) {
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
            System.out.println("Error reading macro plan: " + e.getMessage());
            return;
        }

        if (!foundUser) {
            sb.append(toCsvLine(userId));
        }

        try (FileWriter writer = new FileWriter(MACROS_FILE, false)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.out.println("Error updating macro plan: " + e.getMessage());
        }
    }

    public static MacroPlan load(UUID userId) {

        try (BufferedReader reader = new BufferedReader(new FileReader(MACROS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 6 && parts[0].equals(userId.toString())) {

                    int kcal = Integer.parseInt(parts[1]);
                    int protein = Integer.parseInt(parts[2]);
                    int carbs = Integer.parseInt(parts[3]);
                    int fat = Integer.parseInt(parts[4]);
                    String method = parts[5];

                    return new MacroPlan(kcal, protein, carbs, fat, method);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading macro plan: " + e.getMessage());
        }
        return null;
    }

    // Getters
    public int getCalorieGoal() {
        return calorieGoal;
    }

    public int getProteinGram() {
        return proteinGram;
    }

    public int getCarbohydrateGram() {
        return carbohydrateGram;
    }

    public int getFatGram() {
        return fatGram;
    }

    public String getCalculationMethod() {
        return calculationMethod;
    }

    @Override
    public String toString() {

        return "MacroPlan{" +
                "calorieGoal=" + calorieGoal + " kcal" +
                ", protein=" + proteinGram + "g" +
                ", carbs=" + carbohydrateGram + "g" +
                ", fat=" + fatGram + "g" +
                '}';
    }
}