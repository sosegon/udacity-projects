package com.keemsa.android.jokes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by sebastian on 12/31/16.
 */

public class JokeActivity extends AppCompatActivity {

  private static String LOG_TAG = JokeActivity.class.getSimpleName();
  public static final String JOKE_TAG = "joke";
  private TextView txt_joke;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_joke);

    txt_joke = (TextView) findViewById(R.id.txt_joke);

    Intent intent = getIntent();
    if(intent != null){
      String joke = intent.getStringExtra(JOKE_TAG);
      txt_joke.setText(joke);
    }
  }
}
