package db;

import model.Exercise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExerciseLoader {

    public static List<Exercise> getAllExercises() {
        List<Exercise> results = new ArrayList<>();
        String query = """
            SELECT e.id, e.name, mg.name AS muscle_group
            FROM exercises e
            JOIN muscle_groups mg ON e.muscle_group_id = mg.id
            ORDER BY mg.name, e.name
        """;

        try (Connection con = DatabaseManager.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                results.add(new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("muscle_group")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load exercises", e);
        }
        return results;
    }

    public static List<Exercise> getByMuscleGroup(String muscleGroup) {
        List<Exercise> results = new ArrayList<>();
        String query = """
            SELECT e.id, e.name, mg.name AS muscle_group
            FROM exercises e
            JOIN muscle_groups mg ON e.muscle_group_id = mg.id
            WHERE mg.name = ?
            ORDER BY e.name
        """;

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, muscleGroup);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(new Exercise(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("muscle_group")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load exercises for " + muscleGroup, e);
        }
        return results;
    }

    public static List<String> getAllMuscleGroups() {
        List<String> groups = new ArrayList<>();
        String query = "SELECT name FROM muscle_groups ORDER BY name";

        try (Connection con = DatabaseManager.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                groups.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load muscle groups", e);
        }
        return groups;
    }
}
