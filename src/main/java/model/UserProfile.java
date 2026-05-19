package model;

public class UserProfile {
    private String gender;
    private int age;
    private float weightKg;
    private float heightCm;
    private String experienceLevel;

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public float getWeightKg() { return weightKg; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }

    public float getHeightCm() { return heightCm; }
    public void setHeightCm(float heightCm) { this.heightCm = heightCm; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    @Override
    public String toString() {
        return "Profil: " + gender + ", " + age + " år, " + weightKg + " kg, " +
               heightCm + " cm, " + experienceLevel;
    }
}
