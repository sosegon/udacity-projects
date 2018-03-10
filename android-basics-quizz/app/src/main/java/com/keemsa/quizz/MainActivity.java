package com.keemsa.quizz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<String, String> answers = new HashMap<String, String>();
    int question_counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        next(findViewById(R.id.btn_next));
    }

    public void next(View view) {
        if (question_counter > 0 && question_counter < 5) {
            storePreviousAnswer(question_counter);
        } else if (question_counter == 5) {
            storePreviousAnswer(question_counter);
            setButtonText(getResources().getString(R.string.btn_finish));
        } else if (question_counter == 6) {
            storePreviousAnswer(question_counter);
            gradeAnswers();
            setButtonText(getResources().getString(R.string.btn_try_again));
            question_counter++;
            return;
        } else if (question_counter == 7) {
            reset();
            next(findViewById(R.id.btn_next));
            return;
        }

        question_counter++;

        emptyOptionsContainer();
        LinearLayout ll_options_container = (LinearLayout) findViewById(R.id.ll_question_options);

        // Replace question
        String question_name = "question_" + question_counter;
        String question_value = getStringValueByName(question_name);
        TextView txt_question = (TextView) findViewById(R.id.txt_question);
        txt_question.setText(question_value);

        // Add new options
        String type_name = question_name + "_type";
        String type_value = getStringValueByName(type_name);
        if (type_value.equals("radio")) {
            // attach view
            ViewGroup rbt_options = (ViewGroup) getLayoutInflater().inflate(R.layout.options_radio_button, null);
            ll_options_container.addView(rbt_options);

            // get options
            String options_name = question_name + "_options";
            int options_id = getResources().getIdentifier(options_name, "array", getPackageName());
            String[] options_values = getResources().getStringArray(options_id);

            // set options values in view
            for (int i = 0; i < options_values.length; i++) {
                ViewGroup sub_container = (ViewGroup) rbt_options.getChildAt(0);
                RadioButton rbt_option = (RadioButton) sub_container.getChildAt(i);
                rbt_option.setText(options_values[i]);
            }
        } else if (type_value.equals("check")) {
            // attach view
            ViewGroup chk_options = (ViewGroup) getLayoutInflater().inflate(R.layout.options_check_box, null);
            ll_options_container.addView(chk_options);

            // get options
            String options_name = question_name + "_options";
            int options_id = getResources().getIdentifier(options_name, "array", getPackageName());
            String[] options_values = getResources().getStringArray(options_id);

            // set options values in view
            for (int i = 0; i < options_values.length / 2; i++) {
                ViewGroup sub_container_1 = (ViewGroup) chk_options.getChildAt(0);
                CheckBox chk_option_1 = (CheckBox) sub_container_1.getChildAt(i);
                chk_option_1.setText(options_values[i]);

                ViewGroup sub_container_2 = (ViewGroup) chk_options.getChildAt(1);
                CheckBox chk_option_2 = (CheckBox) sub_container_2.getChildAt(i);
                chk_option_2.setText(options_values[i + 2]);
            }
        } else {
            // attach view
            ViewGroup edt_options = (ViewGroup) getLayoutInflater().inflate(R.layout.options_edit_text, null);
            ll_options_container.addView(edt_options);
        }
    }

    private void reset() {
        answers = new HashMap<String, String>();
        question_counter = 0;

        setButtonText(getResources().getString(R.string.btn_next));
        emptyOptionsContainer();
    }

    private void gradeAnswers() {
        int score = 0;
        for (Map.Entry<String, String> elem : answers.entrySet()) {
            String question_name = elem.getKey();
            String question_answer = getStringValueByName(question_name + "_answer").toUpperCase();
            String user_response = elem.getValue().toUpperCase();
            if (question_answer.equals(user_response)) {
                score++;
            }
        }

        String message = "You got: " + score + " right answers\n";

        if (score <= 2) {
            message += getResources().getString(R.string.upto_2_correct_answers);
        } else if (score > 2 && score <= 4) {
            message += getResources().getString(R.string.upto_4_correct_answers);
        } else if (score > 4 && score <= 5) {
            message += getResources().getString(R.string.upto_5_correct_answers);
        } else if (score > 5 && score <= 6) {
            message += getResources().getString(R.string.upto_6_correct_answers);
        }

        Toast user_results = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        user_results.show();
    }

    private void storePreviousAnswer(int question_counter) {
        String answer_name = "question_" + question_counter;
        String answer = retrievePreviousAnswer();
        answers.put(answer_name, answer);
    }

    private String retrievePreviousAnswer() {
        String answer = "";
        // get view with options
        ViewGroup options_container = (ViewGroup) findViewById(R.id.ll_question_options);
        ViewGroup sub_container = (ViewGroup) options_container.getChildAt(0);

        if (sub_container.getId() == R.id.ll_options_radio_button) {
            RadioGroup group = (RadioGroup) sub_container.getChildAt(0);
            int selected_id = group.getCheckedRadioButtonId();
            if (selected_id >= 0) {
                RadioButton selected = (RadioButton) group.findViewById(selected_id);
                answer += selected.getText();
            }
        } else if (sub_container.getId() == R.id.ll_options_check_box) {
            ViewGroup horizontal_container_1 = (ViewGroup) sub_container.getChildAt(0);
            CheckBox option_1 = (CheckBox) horizontal_container_1.getChildAt(0);
            CheckBox option_2 = (CheckBox) horizontal_container_1.getChildAt(1);

            ViewGroup horizontal_container_2 = (ViewGroup) sub_container.getChildAt(1);
            CheckBox option_3 = (CheckBox) horizontal_container_2.getChildAt(0);
            CheckBox option_4 = (CheckBox) horizontal_container_2.getChildAt(1);

            CheckBox[] options = {option_1, option_2, option_3, option_4};

            for (int i = 0; i < options.length; i++) {
                CheckBox current_option = options[i];
                if (current_option.isChecked()) {
                    if (answer.length() > 0) {
                        answer += ",";
                    }
                    answer += current_option.getText();

                }
            }
        } else if (sub_container.getId() == R.id.ll_options_edit_text) {
            EditText option = (EditText) sub_container.getChildAt(0);
            answer += option.getText();
        }

        return answer;
    }

    private String getStringValueByName(String name) {
        int id = getResources().getIdentifier(name, "string", getPackageName());
        return getResources().getText(id).toString();
    }

    private void setButtonText(String text){
        Button btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setText(text);
    }

    private void emptyOptionsContainer(){
        LinearLayout container = (LinearLayout) findViewById(R.id.ll_question_options);
        container.removeAllViews();
    }
}
