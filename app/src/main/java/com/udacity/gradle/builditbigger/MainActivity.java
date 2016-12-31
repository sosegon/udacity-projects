package com.udacity.gradle.builditbigger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.keemsa.android.jokes.JokeActivity;


public class MainActivity extends ActionBarActivity implements RetrieveJokeAsyncTask.RetrievesJokesAsyncTaskReceiver {

    public interface ProgressBarBin {
        void setProgressBarVisibility(int value);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void useJoke(String joke) {
        ProgressBarBin frg = (ProgressBarBin) getSupportFragmentManager().findFragmentById(R.id.fragment);
        frg.setProgressBarVisibility(ProgressBar.GONE);
        if(joke.equals(RetrieveJokeAsyncTask.DEFAULT_JOKE)){
            joke = getString(R.string.default_joke);
        }
        Intent intent = new Intent(this, JokeActivity.class);
        intent.putExtra(JokeActivity.JOKE_TAG, joke);
        startActivity(intent);
    }

    public void tellJoke(View view) {
        ProgressBarBin frg = (ProgressBarBin) getSupportFragmentManager().findFragmentById(R.id.fragment);
        frg.setProgressBarVisibility(ProgressBar.VISIBLE);

        RetrieveJokeAsyncTask task = new RetrieveJokeAsyncTask(this);
        task.execute();
    }

}
