import java.util.List;

public class WeightProgress {
    private double totalChangeKg;
    private double averageWeeklyChange;

    //Metode for at calculate progress taget fra WeeklyWeighIn History
    public void calculateProgress(List<WeeklyWeighIn> history){

        if(history.size() < 2){
            System.out.println("Not enough data.");
            return;
        }

        double startWeight = history.get(0).getWeightKG();
        double endWeight = history.get(history.size() - 1).getWeightKG();

        totalChangeKg = endWeight - startWeight;

        averageWeeklyChange = totalChangeKg / (history.size() - 1);
    }

    //Metode til at få en Progress Report
    public String getProgressReport(){
        return """
                ===== Progress =====
                Total Change: %.2f kg
                Average Weekly Change: %.2f kg
                """.formatted(totalChangeKg, averageWeeklyChange);
    }
}
