import java.util.List;

public class WeightProgress {
    private float totalChangeKg;
    private float averageWeeklyChange;

    public void calculateProgress(List<WeeklyWeighIn> history) {
        if (history.size() < 2) {
            System.out.println("Not enough data.");
            return;
        }

        float startWeight = history.get(0).getWeightKg();
        float endWeight = history.get(history.size() - 1).getWeightKg();

        totalChangeKg = endWeight - startWeight;
        averageWeeklyChange = totalChangeKg / (history.size() - 1);
    }

    public String getProgressReport() {
        return """
                ===== Progress =====
                Total Change: %.2f kg
                Average Weekly Change: %.2f kg
                """.formatted(totalChangeKg, averageWeeklyChange);
    }
}
