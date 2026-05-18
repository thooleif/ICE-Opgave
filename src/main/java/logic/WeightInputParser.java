package logic;

/**
 * Fortolker vægt-input fra brugeren (tal, "body", ryd).
 */
public final class WeightInputParser {

    public static final class Result {
        public float kg = -1f;
        public boolean bodyweight;
        public boolean cleared;
        public boolean parsed;
    }

    private WeightInputParser() {}

    public static Result parse(String input, float profileWeightKg) {
        Result result = new Result();
        if (input == null || input.isBlank()) {
            return result;
        }

        String normalized = input.trim().toLowerCase();
        if (normalized.equals("body") || normalized.equals("bw") || normalized.equals("bodyweight")
                || normalized.equals("kropsvægt") || normalized.equals("kropsvaegt")) {
            result.bodyweight = true;
            result.kg = profileWeightKg > 0 ? profileWeightKg : -1f;
            result.parsed = true;
            return result;
        }

        if (normalized.equals("0") || normalized.equals("-") || normalized.equals("clear")) {
            result.cleared = true;
            result.kg = -1f;
            result.parsed = true;
            return result;
        }

        try {
            float w = Float.parseFloat(normalized.replace(",", "."));
            result.kg = w;
            result.bodyweight = false;
            result.parsed = true;
        } catch (NumberFormatException ignored) {
            result.parsed = false;
        }
        return result;
    }

    public static String formatDisplay(boolean bodyweight, float kg) {
        if (bodyweight && kg > 0) {
            return "bodyweight (" + formatKg(kg) + ")";
        }
        if (bodyweight) {
            return "bodyweight (your weight)";
        }
        if (kg > 0) {
            return formatKg(kg) + " kg";
        }
        return "—";
    }

    private static String formatKg(float kg) {
        if (kg == (int) kg) {
            return String.valueOf((int) kg);
        }
        return String.valueOf(kg);
    }
}
