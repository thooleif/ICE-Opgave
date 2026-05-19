package db;

import java.sql.*;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:fitness.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection con = getConnection(); Statement stmt = con.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS muscle_groups (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS exercises (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    muscle_group_id INTEGER NOT NULL,
                    FOREIGN KEY (muscle_group_id) REFERENCES muscle_groups(id)
                )
            """);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exercises");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Database already seeded (" + rs.getInt(1) + " exercises).");
                return;
            }

            seed(con);
            System.out.println("Database seeded successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void seed(Connection con) throws SQLException {
        con.setAutoCommit(false);

        insertGroup(con, "Chest", new String[]{
            "Assisted Dip", "Band-Assisted Bench Press", "Bar Dip", "Bench Press",
            "Bench Press Against Band", "Board Press", "Cable Chest Press", "Clap Push-Up",
            "Close-Grip Bench Press", "Close-Grip Feet-Up Bench Press", "Cobra Push-Up",
            "Decline Bench Press", "Decline Push-Up", "Dumbbell Chest Fly", "Dumbbell Chest Press",
            "Dumbbell Decline Chest Press", "Dumbbell Floor Press", "Dumbbell Pullover",
            "Feet-Up Bench Press", "Floor Press", "Incline Bench Press", "Incline Dumbbell Press",
            "Incline Push-Up", "Kettlebell Floor Press", "Kneeling Incline Push-Up",
            "Kneeling Push-Up", "Machine Chest Fly", "Machine Chest Press",
            "Medicine Ball Chest Pass", "Pec Deck", "Pin Bench Press", "Plank to Push-Up",
            "Push-Up", "Push-Up Against Wall", "Push-Ups With Feet in Rings",
            "Resistance Band Chest Fly", "Ring Dip", "Seated Cable Chest Fly",
            "Smith Machine Bench Press", "Smith Machine Incline Bench Press",
            "Smith Machine Reverse Grip Bench Press", "Standing Cable Chest Fly",
            "Standing Resistance Band Chest Fly"
        });

        insertGroup(con, "Shoulders", new String[]{
            "Arnold Press", "Band External Shoulder Rotation", "Band Internal Shoulder Rotation",
            "Band Pull-Apart", "Banded Face Pull", "Barbell Front Raise", "Barbell Rear Delt Row",
            "Barbell Upright Row", "Behind the Neck Press", "Cable Internal Shoulder Rotation",
            "Cable External Shoulder Rotation", "Cable Front Raise", "Cable Lateral Raise",
            "Cable Rear Delt Row", "Cuban Press", "Devils Press", "Dumbbell Front Raise",
            "Dumbbell Horizontal Internal Shoulder Rotation",
            "Dumbbell Horizontal External Shoulder Rotation", "Dumbbell Lateral Raise",
            "Dumbbell Rear Delt Row", "Dumbbell Shoulder Press", "Face Pull", "Front Hold",
            "Handstand Push-Up", "Jerk", "Kettlebell Halo", "Kettlebell Press",
            "Kettlebell Push Press", "Landmine Press", "Lying Dumbbell External Shoulder Rotation",
            "Lying Dumbbell Internal Shoulder Rotation", "Machine Lateral Raise",
            "Machine Shoulder Press", "Monkey Row", "One-Arm Landmine Press", "Overhead Press",
            "Plate Front Raise", "Poliquin Raise", "Power Jerk", "Push Press",
            "Resistance Band Lateral Raise", "Reverse Cable Flyes", "Reverse Dumbbell Flyes",
            "Reverse Dumbbell Flyes on Incline Bench", "Reverse Machine Fly",
            "Seated Dumbbell Shoulder Press", "Seated Barbell Overhead Press",
            "Seated Kettlebell Press", "Seated Smith Machine Shoulder Press",
            "Smith Machine Landmine Press", "Snatch Grip Behind the Neck Press",
            "Squat Jerk", "Split Jerk", "Turkish Get-Up", "Wall Walk", "Z Press"
        });

        insertGroup(con, "Biceps", new String[]{
            "Barbell Curl", "Barbell Preacher Curl", "Bayesian Curl", "Bodyweight Curl",
            "Cable Crossover Bicep Curl", "Cable Curl With Bar", "Cable Curl With Rope",
            "Concentration Curl", "Drag Curl", "Dumbbell Curl", "Dumbbell Preacher Curl",
            "EZ Curl", "Hammer Curl", "Incline Dumbbell Curl", "Kettlebell Curl",
            "Lying Bicep Cable Curl on Bench", "Lying Bicep Cable Curl on Floor",
            "Machine Bicep Curl", "Overhead Cable Curl", "Reverse Barbell Curl",
            "Reverse Dumbbell Curl", "Resistance Band Curl", "Spider Curl", "Zottman Curl"
        });

        insertGroup(con, "Triceps", new String[]{
            "Barbell Standing Triceps Extension", "Barbell Incline Triceps Extension",
            "Barbell Lying Triceps Extension", "Bench Dip",
            "Crossbody Cable Triceps Extension", "Close-Grip Push-Up",
            "Dumbbell Lying Triceps Extension", "Dumbbell Standing Triceps Extension",
            "EZ Bar Lying Triceps Extension", "Machine Overhead Triceps Extension",
            "Overhead Cable Triceps Extension (Lower Position)",
            "Overhead Cable Triceps Extension (Upper Position)",
            "Smith Machine Skull Crushers", "Tate Press", "Tricep Bodyweight Extension",
            "Tricep Pushdown With Bar", "Tricep Pushdown With Rope"
        });

        insertGroup(con, "Legs", new String[]{
            "Air Squat", "Banded Hip March", "Barbell Hack Squat", "Barbell Lunge",
            "Barbell Walking Lunge", "Belt Squat", "Body Weight Lunge", "Bodyweight Leg Curl",
            "Box Jump", "Box Squat", "Bulgarian Split Squat", "Cable Machine Hip Adduction",
            "Chair Squat", "Curtsy Lunge", "Depth Jump", "Dumbbell Lunge",
            "Dumbbell Walking Lunge", "Dumbbell Squat", "Front Squat", "Glute Ham Raise",
            "Goblet Squat", "Ground to Overhead", "Hack Squat Machine", "Half Air Squat",
            "Heel Walk", "Hip Adduction Against Band", "Hip Adduction Machine", "Jump Squat",
            "Jumping Lunge", "Kettlebell Front Squat", "Kettlebell Thrusters",
            "Kettlebell Tibialis Raise", "Landmine Hack Squat", "Landmine Squat",
            "Lateral Bound", "Leg Curl On Ball", "Leg Extension", "Leg Press",
            "Lying Leg Curl", "Nordic Hamstring Eccentric", "One-Legged Leg Extension",
            "One-Legged Lying Leg Curl", "One-Legged Seated Leg Curl", "Pause Squat",
            "Pendulum Squat", "Pin Squat", "Pistol Squat", "Poliquin Step-Up",
            "Prisoner Get Up", "Reverse Barbell Lunge", "Reverse Body Weight Lunge",
            "Reverse Dumbbell Lunge", "Reverse Nordic", "Romanian Deadlift",
            "Safety Bar Squat", "Seated Leg Curl", "Shallow Body Weight Lunge",
            "Side Lunges (Bodyweight)", "Smith Machine Bulgarian Split Squat",
            "Smith Machine Front Squat", "Smith Machine Lunge",
            "Smith Machine Romanian Deadlift", "Smith Machine Squat", "Sumo Squat", "Squat",
            "Standing Cable Leg Extension", "Standing Hip Flexor Raise", "Standing Leg Curl",
            "Step Up", "Tibialis Band Pull", "Tibialis Raise", "Vertical Leg Press",
            "Zercher Squat", "Zombie Squat"
        });

        insertGroup(con, "Back", new String[]{
            "Assisted Chin-Up", "Assisted Pull-Up", "Back Extension", "Banded Muscle-Up",
            "Barbell Row", "Barbell Shrug", "Block Clean", "Block Snatch",
            "Cable Close Grip Seated Row", "Cable Wide Grip Seated Row",
            "Chest-Supported Dumbbell Row", "Chest to Bar", "Chin-Up", "Clean",
            "Clean and Jerk", "Close-Grip Chin-Up", "Close-Grip Lat Pulldown", "Deadlift",
            "Deficit Deadlift", "Dumbbell Deadlift", "Dumbbell Row", "Dumbbell Shrug",
            "Floor Back Extension", "Good Morning", "Gorilla Row", "Hang Clean",
            "Hang Power Clean", "Hang Power Snatch", "Hang Snatch", "Inverted Row",
            "Inverted Row with Underhand Grip", "Jefferson Curl", "Jumping Muscle-Up",
            "Kettlebell Clean", "Kettlebell Clean & Jerk", "Kettlebell Clean & Press",
            "Kettlebell Row", "Kettlebell Snatch", "Kettlebell Swing", "Kroc Row",
            "Lat Pulldown With Neutral Grip", "Lat Pulldown With Pronated Grip",
            "Lat Pulldown With Supinated Grip", "Machine Lat Pulldown", "Muscle-Up (Bar)",
            "Muscle-Up (Rings)", "Neutral Close-Grip Lat Pulldown", "One-Handed Cable Row",
            "One-Handed Kettlebell Swing", "One-Handed Lat Pulldown", "Pause Deadlift",
            "Pendlay Row", "Power Clean", "Power Snatch", "Pull-Up",
            "Pull-Up With a Neutral Grip", "Rack Pull", "Renegade Row", "Rope Pulldown",
            "Ring Pull-Up", "Ring Row", "Scap Pull-Up", "Seal Row", "Seated Machine Row",
            "Single Leg Deadlift with Kettlebell", "Smith Machine Deadlift",
            "Smith Machine One-Handed Row", "Snatch", "Snatch Grip Deadlift",
            "Stiff-Legged Deadlift", "Straight Arm Lat Pulldown", "Sumo Deadlift",
            "Superman Raise", "T-Bar Row", "Towel Row",
            "Trap Bar Deadlift With High Handles", "Trap Bar Deadlift With Low Handles"
        });

        insertGroup(con, "Glutes", new String[]{
            "Banded Side Kicks", "Cable Glute Kickback", "Cable Pull Through",
            "Cable Machine Hip Abduction", "Clamshells", "Cossack Squat",
            "Death March with Dumbbells", "Donkey Kicks", "Dumbbell Romanian Deadlift",
            "Dumbbell Frog Pumps", "Fire Hydrants", "Frog Pumps", "Glute Bridge",
            "Hip Abduction Against Band", "Hip Abduction Machine", "Hip Thrust",
            "Hip Thrust Machine", "Hip Thrust With Band Around Knees", "Kettlebell Windmill",
            "Lateral Walk With Band", "Machine Glute Kickbacks", "One-Legged Glute Bridge",
            "One-Legged Hip Thrust", "Reverse Hyperextension", "Romanian Deadlift",
            "Smith Machine Hip Thrust", "Single Leg Romanian Deadlift",
            "Standing Hip Abduction Against Band", "Standing Glute Kickback in Machine",
            "Standing Glute Push Down", "Step Up"
        });

        insertGroup(con, "Abs", new String[]{
            "Ball Slams", "Bicycle Crunch", "Cable Crunch", "Captain's Chair Knee Raise",
            "Captain's Chair Leg Raise", "Copenhagen Plank", "Core Twist", "Crunch",
            "Dead Bug", "Dead Bug With Dumbbells", "Dragon Flag", "Dumbbell Side Bend",
            "Dynamic Side Plank", "Hanging Knee Raise", "Hanging Leg Raise", "Hanging Sit-Up",
            "Hanging Windshield Wiper", "High to Low Wood Chop with Band",
            "High to Low Wood Chop with Cable", "Hollow Body Crunch", "Hollow Hold",
            "Horizontal Wood Chop with Band", "Horizontal Wood Chop with Cable",
            "Jackknife Sit-Up", "Kettlebell Plank Pull Through",
            "Kneeling Ab Wheel Roll-Out", "Kneeling Plank", "Kneeling Side Plank",
            "Landmine Rotation", "L-Sit", "Low to High Wood Chop with Band",
            "Low to High Wood Chop with Cable", "Lying Leg Raise", "Lying Windshield Wiper",
            "Lying Windshield Wiper with Bent Knees", "Machine Crunch", "Mountain Climbers",
            "Oblique Crunch", "Oblique Sit-Up", "Pallof Press", "Plank",
            "Plank with Leg Lifts", "Plank with Shoulder Taps", "Side Plank", "Sit-Up",
            "Weighted Plank"
        });

        insertGroup(con, "Calves", new String[]{
            "Barbell Standing Calf Raise", "Barbell Seated Calf Raise",
            "Calf Raise in Leg Press", "Donkey Calf Raise", "Eccentric Heel Drop",
            "Heel Raise", "Seated Calf Raise", "Standing Calf Raise"
        });

        insertGroup(con, "Forearm Flexors & Grip", new String[]{
            "Barbell Wrist Curl", "Barbell Wrist Curl Behind the Back", "Bar Hang",
            "Dumbbell Wrist Curl", "Farmers Walk", "Fat Bar Deadlift", "Gripper",
            "One-Handed Bar Hang", "Plate Pinch", "Plate Wrist Curl", "Towel Pull-Up",
            "Wrist Roller"
        });

        insertGroup(con, "Forearm Extensors", new String[]{
            "Barbell Wrist Extension", "Dumbbell Wrist Extension"
        });

        insertGroup(con, "Neck", new String[]{
            "Lying Neck Curl", "Lying Neck Extension", "Prone Neck Bridge",
            "Supine Neck Bridge"
        });

        insertGroup(con, "Cardio", new String[]{
            "Rowing Machine", "Stationary Bike", "Treadmill Running",
            "Assault Bike", "Jump Rope", "Stair Climber",
            "Elliptical", "Ski Erg", "Fan Bike Sprints",
            "Battle Ropes", "Sled Push", "Sled Pull"
        });

        con.commit();
        con.setAutoCommit(true);
    }

    private static void insertGroup(Connection con, String groupName, String[] exercises) throws SQLException {
        try (PreparedStatement groupStmt = con.prepareStatement(
                "INSERT INTO muscle_groups (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            groupStmt.setString(1, groupName);
            groupStmt.executeUpdate();

            ResultSet keys = groupStmt.getGeneratedKeys();
            keys.next();
            int groupId = keys.getInt(1);

            try (PreparedStatement exStmt = con.prepareStatement(
                    "INSERT INTO exercises (name, muscle_group_id) VALUES (?, ?)")) {
                for (String name : exercises) {
                    exStmt.setString(1, name);
                    exStmt.setInt(2, groupId);
                    exStmt.addBatch();
                }
                exStmt.executeBatch();
            }
        }
    }
}
