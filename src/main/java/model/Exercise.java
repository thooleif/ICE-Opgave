package model;

public class Exercise {
    private int id;
    private String name;
    private String muscleGroup;

    public Exercise(int id, String name, String muscleGroup) {
        this.id = id;
        this.name = name;
        this.muscleGroup = muscleGroup;
    }

    public Exercise(String name, String muscleGroup) {
        this.name = name;
        this.muscleGroup = muscleGroup;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }

    @Override
    public String toString() {
        return muscleGroup + " — " + name;
    }
}
