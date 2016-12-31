package com.udacity.gradle.builditbigger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.keemsa.android.jokes.JokeActivity;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements RetrieveJokeAsyncTask.RetrievesJokesAsyncTaskReceiver{

    private static String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private Button btn_tellJoke;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        Button btn_tellJoke = (Button) root.findViewById(R.id.btn_tellJoke);
        btn_tellJoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RetrieveJokeAsyncTask task = new RetrieveJokeAsyncTask(MainActivityFragment.this);
                task.execute();
            }
        });

        AdView mAdView = (AdView) root.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        return root;
    }

    @Override
    public void useJoke(String joke) {
        Intent intent = new Intent(getContext(), JokeActivity.class);
        intent.putExtra("joke", joke); // TODO use a key value from strings.xml
        startActivity(intent);
    }
}
