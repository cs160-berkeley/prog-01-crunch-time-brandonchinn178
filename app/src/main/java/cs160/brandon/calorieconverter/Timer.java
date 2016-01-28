package cs160.brandon.calorieconverter;

import android.app.Activity;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Brandon on 1/23/16.
 */
public class Timer {
    private Handler handler;
    private Runnable runnable;
    private EditText clock;
    private TextView output;
    private Time currentTime;
    public boolean isOn;

    private class Time {
        private final String timeFormat = "%02d:%02d";
        public int minutes;
        public int seconds;

        public Time() {
            setZero();
        }

        public void setZero() {
            this.minutes = 0;
            this.seconds = 0;
        }

        public String toString() {
            return String.format(timeFormat, this.minutes, this.seconds);
        }
    }

    public Timer(Activity a) {
        this.handler = new Handler();
        this.clock = (EditText) a.findViewById(R.id.input);
        this.output = (TextView) a.findViewById(R.id.output);
        this.currentTime = new Time();
        this.isOn = false;
        updateClock();
    }

    public void start(final ExerciseInfo exercise) {
        runnable = new Runnable() {
            @Override
            public void run() {
                // update clock
                if (currentTime.seconds == 59) {
                    currentTime.seconds = 0;
                    currentTime.minutes++;
                } else {
                    currentTime.seconds++;
                }
                updateClock();

                // update output
                double calories = exercise.getCaloriesBurnt(getMinutes());
                output.setText(String.format("%.1f", calories));

                // set timer again
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
        isOn = true;
    }

    public void stop() {
        handler.removeCallbacks(runnable);
        runnable = null;
        isOn = false;
    }

    public void reset() {
        stop();
        currentTime.setZero();
        updateClock();
    }

    public void updateClock() {
        clock.setText(currentTime.toString());
    }

    public double getMinutes() {
        return currentTime.minutes + currentTime.seconds / 60.0;
    }

    public void setMinutes(double minutes) {
        int mins = (int) minutes;
        double seconds = (minutes - mins) * 60;
        currentTime.seconds = (int) seconds;
        currentTime.minutes = mins;
        updateClock();
    }
}
