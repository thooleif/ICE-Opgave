

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;


public class FoodRecommender {
    static String[] keywords = {"low calorie", "high protein", "snack", "protein", "vegetarian" , "fruit", "beverage", "drink", "candy", "fish", "low kcal", "no fish", "no candy"};





//keywords skal komme fra en anden funktion, brugeren skal indputte hvad de leder efter, fx: lav kalorie mad høj protein
    //Keywords her ville være: (lav kalorie) (mad) (høj protein). Altså ingen drinks eller mad med kcal over fx 200kcal
    //Keyword ku også være noget som "frugt"


    public static ArrayList<String> promptFood(){

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
                System.out.println("\n Is that correct? y/n");
                String yesNo = scanner.nextLine();
                if (yesNo.equals("yes") || yesNo.equals("y")) {
                    resultKeywords.addAll(tempkeywords);
                    inputting = false;
                } else if (yesNo.equals("no") || yesNo.equals("n")) {
                    System.out.println("Try again");
                    System.out.println();
                }
            }else{
                System.out.println("DETECTED NO KEYWORDS, TRY AGAIN");
                System.out.println();
            }
        }
        return resultKeywords;
            }





   // En funktion som tar en array eller en arraylist med keywords, og fodre dem videre til forskellige funktioner.
    private static void chooseFood(ArrayList<String> searchWords){
    }



    public static ArrayList<String> grabLowKcalFood(ArrayList<String> searchWords){
        ArrayList<String> results = new ArrayList<>();

//Tjekker om drinks ska listes
        boolean withDrinks = (searchWords.contains("drink") || searchWords.contains("beverage"));
        boolean fish = (!searchWords.contains("no fish"));
        boolean vegetarian = (searchWords.contains("vegetarian"));
        boolean snacks = (searchWords.contains("snacks"));
        snacks = (!searchWords.contains("no snacks"));



        //Burde ku joine url'en til databasen fra FridaImporter
        final String database_url = "jdbc:sqlite:C:/Users/max88/OneDrive/Documents/Datmatiker opgaver/Første semester/Java-Opgaver - IntelliJ'/SP - opgaver/ICE-Opgave/data/frida.db";
        final String QUERY = "SELECT data_normalised.foodname, resval AS kalorier, foodgroup " +
        "FROM data_normalised JOIN food ON data_normalised.foodid = food.foodid " +
                "WHERE parametername = 'Energy (kcal)' AND resval <= ?" +
                "GROUP BY SUBSTR(data_normalised.foodname, 1, INSTR(data_normalised.foodname || ',', ',') - 1)\n";


        // Laver en forbindelse til databasen
        // Det er her der er en fejl hvis databasen ikke kan findes. Databasen skal skabes første gang man åbner programmet
        try {

            Connection con = DriverManager.getConnection(database_url);
            PreparedStatement statement = con.prepareStatement(QUERY);
            statement.setInt(1, 150);
            ResultSet rs = statement.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("Query returned 0 rows — problem is in SQL or DB connection");
            }


            while (rs.next()) {

                if (withDrinks) {

                    String baseName = rs.getString("foodname").split(",")[0].trim();

                    results.add(baseName + " - " + rs.getInt("kalorier") + " kcal");

                } else {

                    //Tjekker om foodgroupen er en drink af enhver art, hvis den er bliver den ikke tilføjet til results
                    String foodgroup = rs.getString("foodgroup").toLowerCase();
                    if (!foodgroup.contains("drink") && !foodgroup.contains("tea") && !foodgroup.contains("wine") && !foodgroup.contains("liquor") && !foodgroup.contains("beverage") && !foodgroup.contains("cider") && !foodgroup.contains("infant")) {

                        String baseName = rs.getString("foodname").split(",")[0].trim();

                        results.add(baseName + " - " + rs.getInt("kalorier") + " kcal");
                    }
                }
            }

        }catch (SQLException e) {
            throw new RuntimeException("Error in foodRecommender");
        }


        for (String food : results){
            System.out.println(food);
        }
       if (results.isEmpty()){
           System.out.println("no food");
       }

        return results;
    }



    public static void grabLowKcalHighProteinFood(){

    }




    public static void main(String[] args) {

    grabLowKcalFood(promptFood());
    }


}
