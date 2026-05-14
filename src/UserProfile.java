public class UserProfile {

    // Enum for gender
    public enum Gender {
        MALE,
        FEMALE
    }

    // Enum for experience level
    public enum ExperienceLevel {
        BEGINNER,
        NOVICE,
        INTERMEDIATE,
        ADVANCED
    }

    // Fields
    private int age;
    private Gender gender;
    private float heightCm;
    private float weightKg;
    private ExperienceLevel experienceLevel;
    private String injuryNotes;

    // Constructor
    public UserProfile(int age,
                       Gender gender,
                       float heightCm,
                       float weightKg,
                       ExperienceLevel experienceLevel,
                       String injuryNotes) {

        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.experienceLevel = experienceLevel;
        this.injuryNotes = injuryNotes;
    }

    // Update stats
    public void updateStats(int age, float heightCm, float weightKg) {

        this.age = age;
        this.heightCm = heightCm;
        this.weightKg = weightKg;

        System.out.println("Stats updated successfully.");
    }

    // Calculate BMI
    public float calculateBMI() {

        float heightMeters = heightCm / 100;

        return weightKg / (heightMeters * heightMeters);
    }

    // Set experience level
    public void setExperienceLevel(ExperienceLevel experienceLevel) {

        this.experienceLevel = experienceLevel;

        System.out.println("Experience level updated.");
    }

    // Add injury note
    public void addInjuryNote(String note) {

        if (injuryNotes == null || injuryNotes.isEmpty()) {

            injuryNotes = note;

        } else {

            injuryNotes += ", " + note;
        }

        System.out.println("Injury note added.");
    }

    // Getters
    public int getAge() {
        return age;
    }

    public Gender getGender() {
        return gender;
    }

    public float getHeightCm() {
        return heightCm;
    }

    public float getWeightKg() {
        return weightKg;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public String getInjuryNotes() {
        return injuryNotes;
    }

    // toString
    @Override
    public String toString() {

        return "UserProfile{" +
                "age=" + age +
                ", gender=" + gender +
                ", heightCm=" + heightCm +
                ", weightKg=" + weightKg +
                ", experienceLevel=" + experienceLevel +
                ", injuryNotes='" + injuryNotes + '\'' +
                '}';
    }
}