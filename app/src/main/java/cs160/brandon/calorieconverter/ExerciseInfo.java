package cs160.brandon.calorieconverter;

/**
 * Created by Brandon on 1/21/16.
 */
public class ExerciseInfo {
    private ExerciseUnit units;
    // conversion factor representing amount to burn 100 calories
    public int factor;

    public ExerciseInfo(String unitName, int amount) {
        this.factor = amount;

        switch (unitName) {
            case "reps":
                this.units = ExerciseUnit.REPS;
                break;
            case "mins":
                this.units = ExerciseUnit.MINUTES;
                break;
            default:
                this.units = null;
        }
    }

    public String getUnits() {
        switch (this.units) {
            case REPS:
                return "reps";
            case MINUTES:
                return "minutes";
            default:
                return null;
        }
    }

    public boolean isTimed() {
        switch (this.units) {
            case MINUTES:
                return true;
            default:
                return false;
        }
    }

    public double getCaloriesBurnt(double amount) {
        return amount / (factor / 100.0);
    }

    public double getAmountNeeded(double calories) {
        return calories * (factor / 100.0);
    }
}
