package cs160.brandon.calorieconverter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private HashMap<String, ExerciseInfo> exercises;
    private List<String> exerciseOptions;
    private String selectedExercise;
    private boolean outputCalories = true; // true if outputting calories to burn
    private boolean isTimerLoaded = false; // true if timer is loaded on screen
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // load exercise options
        exercises = getExerciseTypes();
        exerciseOptions = new ArrayList<String>(exercises.keySet());
        Collections.sort(exerciseOptions);
        selectedExercise = exerciseOptions.get(0);

        // exercise type spinner
        final Spinner exerciseType = (Spinner) findViewById(R.id.exercise_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.exercise_type_spinner,
                exerciseOptions
        );
        exerciseType.setAdapter(adapter);
        exerciseType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedExercise = exerciseOptions.get((int) id);
                ExerciseInfo exercise = exercises.get(selectedExercise);
                if (outputCalories) {
                    EditText input = (EditText) findViewById(R.id.input);
                    // reset values and UI
                    if (isTimerLoaded) {
                        timer.reset();
                        Button inputButton = (Button) findViewById(R.id.input_button);
                        if (exercise.isTimed()) {
                            inputButton.setText(R.string.timer_start_label);
                        } else {
                            inputButton.setText(R.string.input_button_label);
                            input.setText("0");
                            isTimerLoaded = false;
                        }
                    } else {
                        input.setText("0");
                    }
                    // add/remove timed button
                    if (exercise.isTimed()) {
                        if (!hasTimedButton()) {
                            addTimedButton();
                        }
                    } else {
                       removeTimedButton();
                    }
                }
                setInputLabel();
                setOutputLabel();
                // resetting values
                TextView output = (TextView) findViewById(R.id.output);
                output.setText("0");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // input button
        final Button inputButton = (Button) findViewById(R.id.input_button);
        inputButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ExerciseInfo exercise = exercises.get(selectedExercise);
                if (!isTimerLoaded) {
                    EditText input = (EditText) findViewById(R.id.input);
                    TextView output = (TextView) findViewById(R.id.output);
                    double amount;
                    try {
                        amount = Double.parseDouble(input.getText().toString());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return;
                    }
                    double converted;
                    if (outputCalories) {
                        converted = exercise.getCaloriesBurnt(amount);
                    } else {
                        converted = exercise.getAmountNeeded(amount);
                    }
                    output.setText(String.format("%.1f", converted));
                    // hide keyboard
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputButton.getWindowToken(), 0);
                } else if (timer.isOn) {
                    timer.stop();
                    inputButton.setText(R.string.timer_start_label);
                } else {
                    timer.start(exercise);
                    inputButton.setText(R.string.timer_stop_label);
                }
            }
        });

        // switch direction button
        final ImageButton switchDirection = (ImageButton) findViewById(R.id.switch_direction);
        switchDirection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                outputCalories = !outputCalories;
                ExerciseInfo exercise = exercises.get(selectedExercise);
                if (exercise.isTimed()) {
                    if (outputCalories) {
                        addTimedButton();
                    } else {
                        timer.stop();
                        removeTimedButton();
                        if (isTimerLoaded) {
                            inputButton.setText(R.string.input_button_label);
                        }
                    }
                }
                setInputLabel();
                setOutputLabel();
                // flipping values
                EditText input = (EditText) findViewById(R.id.input);
                TextView output = (TextView) findViewById(R.id.output);
                String inputValue = input.getText().toString();
                String outputValue = output.getText().toString();
                if (isTimerLoaded) {
                    inputValue = String.format("%.1f", timer.getMinutes());
                    isTimerLoaded = false;
                }
                input.setText(outputValue);
                output.setText(inputValue);
            }
        });

        timer = new Timer(this);
    }

    /**
     * Add the button that switches between inputting amount of minutes
     * and timing the exercise
     */
    private void addTimedButton() {
        final LinearLayout container = (LinearLayout) findViewById(R.id.buttons_container);
        final ImageButton switchTimer = new ImageButton(this);
        switchTimer.setId(R.id.timed_button);
        switchTimer.setPadding(15, 15, 15, 15);
        switchTimer.setBackgroundResource(R.color.colorAccent);
        switchTimer.setImageResource(R.drawable.timer);
        switchTimer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isTimerLoaded = !isTimerLoaded;
                Button inputButton = (Button) findViewById(R.id.input_button);
                EditText input = (EditText) findViewById(R.id.input);
                TextView output = (TextView) findViewById(R.id.output);
                if (isTimerLoaded) {
                    switchTimer.setImageResource(R.drawable.edit);
                    inputButton.setText(R.string.timer_start_label);
                    container.requestFocus();
                    double minutes = Double.parseDouble(input.getText().toString());
                    timer.setMinutes(minutes);
                } else {
                    switchTimer.setImageResource(R.drawable.timer);
                    inputButton.setText(R.string.input_button_label);
                    input.setText(String.format("%.1f", timer.getMinutes()));
                }
            }
        });
        container.addView(switchTimer);
        setMargins(switchTimer, 40, 0, 0, 0);
    }

    private void removeTimedButton() {
        LinearLayout container = (LinearLayout) findViewById(R.id.buttons_container);
        ImageButton timedButton = (ImageButton) findViewById(R.id.timed_button);
        container.removeView(timedButton);
    }

    /**
     * @return true if the timed button (see addTimedButton()) already
     * exists on the screen
     */
    private boolean hasTimedButton() {
        return findViewById(R.id.timed_button) != null;
    }

    /**
     * Set the label denoting the units of the input, e.g. "reps", "minutes", "calories"
     */
    private void setInputLabel() {
        ExerciseInfo exercise = exercises.get(selectedExercise);
        TextView inputLabel = (TextView) findViewById(R.id.input_label);
        String text;
        if (outputCalories) {
            text = exercise.getUnits();
        } else {
            text = "calories";
        }
        inputLabel.setText(text);
    }

    private void setOutputLabel() {
        ExerciseInfo exercise = exercises.get(selectedExercise);
        TextView outputLabel = (TextView) findViewById(R.id.output_label);
        int text;
        if (outputCalories) {
            text = R.string.output_label_calories;
        } else if (exercise.isTimed()) {
            text = R.string.output_label_minutes;
        } else {
            text = R.string.output_label_reps;
        }
        outputLabel.setText(text);
    }

    /**
     * Helper function to set margins of a view. The view must be contained
     * within a LinearLayout view
     * @param v the View to set margins for
     * @param left the left margin
     * @param top the top margin
     * @param right the right margin
     * @param bottom the bottom margin
     */
    private void setMargins(View v, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        v.setLayoutParams(params);
    }

    /**
     * Load exercise types from the exercise_types.json file in the assets directory
     * @return a hashmap mapping the name of the exercise to an ExerciseInfo object
     * containing data about the exercise
     */
    private HashMap<String, ExerciseInfo> getExerciseTypes() {
        try {
            String json = null;
            InputStream is = getAssets().open("exercise_types.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            HashMap<String, ExerciseInfo> exercises = new HashMap<>();
            JSONObject object = new JSONObject(json);
            JSONArray exerciseNames = object.names();
            for (int i = 0; i < exerciseNames.length(); i++) {
                String name = exerciseNames.getString(i);
                JSONObject info = object.getJSONObject(name);
                String units = info.getString("units");
                int amount = info.getInt("amount");
                ExerciseInfo exerciseInfo = new ExerciseInfo(units, amount);
                exercises.put(name, exerciseInfo);
            }
            return exercises;
        } catch (IOException|JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
