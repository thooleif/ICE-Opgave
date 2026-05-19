package foodRecommender;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FoodRecommender {

    static String[] keywords = {
            "low calorie", "high protein", "snack", "protein", "fruit",
            "beverage", "drink", "candy", "fish", "low kcal",
            "no fish", "no candy"
    };

    // -------------------------------------------------------------------------
    // promptFood — unchanged from original
    // -------------------------------------------------------------------------
    public static ArrayList<String> promptFood() {
        ArrayList<String> resultKeywords = new ArrayList<>();
        boolean inputting = true;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the diet recommender");

        while (inputting) {
            System.out.println("Please input what you are looking for");
            String userAnswer = scanner.nextLine();

            ArrayList<String> tempkeywords = new ArrayList<>();
            for (String keyword : keywords) {
                if (userAnswer.contains(keyword)) {
                    tempkeywords.add(keyword);
                }
            }

            if (!tempkeywords.isEmpty()) {
                System.out.println("DETECTED THESE KEYWORDS:  " + tempkeywords);
                System.out.println("\nIs that correct? y/n");
                String yesNo = scanner.nextLine();
                if (yesNo.equals("yes") || yesNo.equals("y")) {
                    resultKeywords.addAll(tempkeywords);
                    inputting = false;
                } else if (yesNo.equals("no") || yesNo.equals("n")) {
                    System.out.println("Try again\n");
                }
            } else {
                System.out.println("DETECTED NO KEYWORDS, TRY AGAIN\n");
            }
        }
        return resultKeywords;
    }

    // -------------------------------------------------------------------------
    // grabLowKcalFood
    //
    // Keyword behaviour
    // -----------------
    // "low calorie" / "low kcal" = only food with kcal ≤ 150 per 100 g
    //                               (without this tag ALL kcal levels are shown)
    // "high protein" / "protein" = only food with protein ≥ 10 g per 100 g
    // "fish" = only food from fish/seafood food-groups
    // "fruit"  = only food from fruit food-groups
    // "snack"  = only food from snack food-group
    // "candy" = only food from candy food-group
    // "drink" / "beverage" = include drinks (excluded by default)
    // "no fish" = exclude fish food-groups (overrides "fish")
    // "no candy" = exclude candy food-group  (overrides "candy")
    //
    // Calories AND protein are always shown.
    // -------------------------------------------------------------------------



    public static void grabFood(ArrayList<String> searchWords) {
        ArrayList<String> results = new ArrayList<>();

        // --- Interpret tags ------------------------------------------------
        boolean lowCalorie   = searchWords.contains("low calorie") || searchWords.contains("low kcal");
        boolean highProtein  = searchWords.contains("high protein") || searchWords.contains("protein");
        boolean wantFish     = searchWords.contains("fish")    && !searchWords.contains("no fish");
        boolean wantFruit    = searchWords.contains("fruit");
        boolean wantSnack    = searchWords.contains("snack");
        boolean wantCandy    = searchWords.contains("candy")   && !searchWords.contains("no candy");
        boolean withDrinks   = searchWords.contains("drink")   || searchWords.contains("beverage");
        boolean noFish       = searchWords.contains("no fish");
        boolean noCandy      = searchWords.contains("no candy");

        // Are we restricting to specific positive food-group tags?
        boolean hasPositiveGroupTag = wantFish || wantFruit || wantSnack || wantCandy;

        // --- Food-group filter sets ----------------------------------------
        // Fish / seafood groups in FRIDA
        String[] fishGroups  = {"Somewhat oily fish", "Oily fish", "Lean fish",
                "Fish products", "Offal and fish eggs",
                "Shellfish and their products", "Mollusks and their products"};

        // Fruit groups in FRIDA
        String[] fruitGroups = {"Soft fruit", "Pome fruit", "Stone fruit",
                "Tropical or subtropical fruit", "Fruit -Vegetables",
                "Dried fruit and berry products", "Fruit juice and smoothies",
                "Canned fruit products", "Frozen fruits and berries"};

        // Snack groups
        String[] snackGroups = {"Potato chip and snacks"};

        // Candy groups
        String[] candyGroups = {"Candy"};

        // Drink-like keywords used to exclude beverages when withDrinks=false
        String[] drinkKeywords = {"drink", "tea", "wine", "liquor", "beverage",
                "cider", "infant", "coffee", "water", "juice",
                "smoothie", "beer", "spirit", "soda"};

        // --- Build SQL -------------------------------------------------------
        // We always join on both Energy and Protein so both values are available.
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "    SUBSTR(f.foodname, 1, INSTR(f.foodname || ',', ',') - 1) AS baseName, " +
                        "    f.foodgroup, " +
                        "    CAST(kcal.resval AS REAL) AS kcal, " +
                        "    CAST(prot.resval AS REAL) AS protein_g " +
                        "FROM food f " +
                        "JOIN data_normalised kcal ON kcal.foodid = f.foodid " +
                        "    AND kcal.parametername = 'Energy (kcal)' " +
                        "JOIN data_normalised prot ON prot.foodid = f.foodid " +
                        "    AND prot.parametername = 'Protein' " +
                        "WHERE 1=1 "
        );

        if (lowCalorie) {
            sql.append("AND CAST(kcal.resval AS REAL) <= 150 ");
        }
        if (highProtein) {
            sql.append("AND CAST(prot.resval AS REAL) >= 10 ");
        }

        // Positive food-group filter: if one or more positive tags are set,
        // restrict to those groups using IN (built dynamically below).
        if (hasPositiveGroupTag) {
            ArrayList<String> allowedGroups = new ArrayList<>();
            if (wantFish)  for (String g : fishGroups)  allowedGroups.add(g);
            if (wantFruit) for (String g : fruitGroups) allowedGroups.add(g);
            if (wantSnack) for (String g : snackGroups) allowedGroups.add(g);
            if (wantCandy) for (String g : candyGroups) allowedGroups.add(g);

            sql.append("AND f.foodgroup IN (");
            for (int i = 0; i < allowedGroups.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(") ");
        }

        sql.append("GROUP BY SUBSTR(f.foodname, 1, INSTR(f.foodname || ',', ',') - 1) ");
        sql.append("ORDER BY kcal ASC");

        // --- Execute ---------------------------------------------------------
        final String DATABASE_URL = "jdbc:sqlite:data/frida.db";

        try {
            Connection con = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement stmt = con.prepareStatement(sql.toString());

            // Bind the food-group IN list if needed
            if (hasPositiveGroupTag) {
                ArrayList<String> allowedGroups = new ArrayList<>();
                if (wantFish)  for (String g : fishGroups)  allowedGroups.add(g);
                if (wantFruit) for (String g : fruitGroups) allowedGroups.add(g);
                if (wantSnack) for (String g : snackGroups) allowedGroups.add(g);
                if (wantCandy) for (String g : candyGroups) allowedGroups.add(g);

                for (int i = 0; i < allowedGroups.size(); i++) {
                    stmt.setString(i + 1, allowedGroups.get(i));
                }
            }

            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("Query returned 0 rows — no matches for your filters.");
            }

            while (rs.next()) {
                String foodgroup = rs.getString("foodgroup");
                String groupLower = foodgroup.toLowerCase();

                // --- Negative exclusions applied in Java (no-fish, no-candy, no-drinks) ---

                // Exclude fish if "no fish" was requested
                if (noFish) {
                    boolean isFish = false;
                    for (String fg : fishGroups) {
                        if (foodgroup.equalsIgnoreCase(fg)) { isFish = true; break; }
                    }
                    if (isFish) continue;
                }

                // Exclude candy if "no candy" was requested
                if (noCandy) {
                    boolean isCandy = false;
                    for (String cg : candyGroups) {
                        if (foodgroup.equalsIgnoreCase(cg)) { isCandy = true; break; }
                    }
                    if (isCandy) continue;
                }

                // Exclude drinks unless withDrinks is true
                // (only applied when there is no positive group tag restricting to drinks)
                if (!withDrinks && !hasPositiveGroupTag) {
                    boolean isDrink = false;
                    for (String dk : drinkKeywords) {
                        if (groupLower.contains(dk)) { isDrink = true; break; }
                    }
                    if (isDrink) continue;
                }

                String baseName = rs.getString("baseName");
                int    kcal     = rs.getInt("kcal");
                double protein  = rs.getDouble("protein_g");

                results.add(String.format("%-45s | %4d kcal | %5.1f g protein | %s",
                        baseName, kcal, protein, foodgroup));
            }

            rs.close();
            stmt.close();
            con.close();

        } catch (SQLException e) {
            throw new RuntimeException("Database error in grabLowKcalFood: " + e.getMessage(), e);
        }

        // --- Print results ---------------------------------------------------
        if (results.isEmpty()) {
            System.out.println("No food found matching your filters.");
        } else {
            System.out.printf("%-45s | %s | %s | %s%n",
                    "Food", "Kcal/100g", "Protein/100g", "Group");
            System.out.println("-".repeat(90));
            for (String food : results) {
                System.out.println(food);
            }
            System.out.println("\n" + results.size() + " result(s) found.");
        }
    }

   public static void launchFoodRecommender(){
        grabFood(promptFood());
   }
}