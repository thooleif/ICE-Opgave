import java.time.LocalDate;
import java.util.UUID;

public class WeeklyWeighIn {
    private UUID weighInId;
    private LocalDate date;
    private float weightKg;

    //Constructor for Weekly Weigh-In
    public WeeklyWeighIn(LocalDate date, float weightKg){
        this.weighInId = UUID.randomUUID();
        this.date = date;
        this.weightKg = weightKg;
       }

    public LocalDate getDate(){
        return date;
    }

    public float getWeightKg(){
        return weightKg;
    }

    @Override
    public String toString(){
        return date + ";" + weightKg;
    }
}
