public class UserProfile {

    // Enums
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum ExperienceLevel {
        BEGINNER,
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
    public UserProfile(int age, Gender gender, float heightCm, float weightKg, ExperienceLevel experienceLevel, String injuryNotes) {

        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.experienceLevel = experienceLevel;
        this.injuryNotes = injuryNotes;
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

    // Setters
    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setHeightCm(float heightCm) {
        this.heightCm = heightCm;
    }

    public void setWeightKg(float weightKg) {
        this.weightKg = weightKg;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public void setInjuryNotes(String injuryNotes) {
        this.injuryNotes = injuryNotes;
    }

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