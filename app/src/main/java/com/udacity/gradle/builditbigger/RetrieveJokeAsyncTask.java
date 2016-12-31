package com.udacity.gradle.builditbigger;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.keemsa.jokes.backend.myApi.MyApi;

import java.io.IOException;

/**
 * Created by sebastian on 12/31/16.
 */

public class RetrieveJokeAsyncTask extends AsyncTask<Void, Void, String> {

  public interface RetrievesJokesAsyncTaskReceiver {
    void useJoke(String joke);
  }

  private static MyApi myApiService = null;
  private RetrievesJokesAsyncTaskReceiver mReceiver;

  public RetrieveJokeAsyncTask(RetrievesJokesAsyncTaskReceiver mReceiver) {
    this.mReceiver = mReceiver;
  }

  // Base code from https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints#2-connecting-your-android-app-to-the-backend
  @Override
  protected String doInBackground(Void... voids) {
    if(myApiService == null) {  // Only do this once
      MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
              new AndroidJsonFactory(), null)
              // options for running against local devappserver
              // - 10.0.2.2 is localhost's IP address in Android emulator
              // - turn off compression when running against local devappserver
              .setRootUrl("http://10.0.2.2:8080/_ah/api/") // TODO update this url when deploying to GAE
              .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                @Override
                public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) {
                  abstractGoogleClientRequest.setDisableGZipContent(true);
                }
              });
      // end options for devappserver

      myApiService = builder.build();
    }

    try {
      return myApiService.throwJoke().execute().getContent();
    } catch (IOException e) {
      return e.getMessage(); // TODO handle this to avoid the error message being displayed as a joke
    }
  }

  @Override
  protected void onPostExecute(String s) {
    mReceiver.useJoke(s);
  }
}
