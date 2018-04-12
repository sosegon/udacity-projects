package com.keemsa.basketballscores;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    int score_team_a = 0;
    int score_team_b = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_one_a = (Button) findViewById(R.id.btn_add_one_team_a);
        btn_one_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseScore(1, "a");
            }
        });
        Button btn_one_b = (Button) findViewById(R.id.btn_add_one_team_b);
        btn_one_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseScore(1, "b");
            }
        });
        Button btn_two_a = (Button) findViewById(R.id.btn_add_two_team_a);
        btn_two_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseScore(2, "a");
            }
        });
        Button btn_two_b = (Button) findViewById(R.id.btn_add_two_team_b);
        btn_two_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseScore(2, "b");
            }
        });
        Button btn_three_a = (Button) findViewById(R.id.btn_add_three_team_a);
        btn_three_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseScore(3, "a");
            }
        });
        Button btn_three_b = (Button) findViewById(R.id.btn_add_three_team_b);
        btn_three_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseScore(3, "b");
            }
        });
    }

    public void resetScores(View view) {
        TextView txt_score_a = (TextView) findViewById(R.id.txt_score_a);
        txt_score_a.setText("0");
        TextView txt_score_b = (TextView) findViewById(R.id.txt_score_b);
        txt_score_b.setText("0");
        score_team_a = 0;
        score_team_b = 0;
    }

    public void increaseScore(int value, String team) {
        if (team == "a") {
            score_team_a += value;
            TextView txt_score_a = (TextView) findViewById(R.id.txt_score_a);
            txt_score_a.setText("" + score_team_a);
        } else {
            score_team_b += value;
            TextView txt_score_b = (TextView) findViewById(R.id.txt_score_b);
            txt_score_b.setText("" + score_team_b);
        }
    }
}
