package com.udacity.gradle.builditbigger.testing;

import android.support.test.runner.AndroidJUnit4;

import com.udacity.gradle.builditbigger.RetrieveJokeAsyncTask;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by sebastian on 12/31/16.
 */

@RunWith(AndroidJUnit4.class)
public class RetrieveJokeAsyncTaskTest {

  @Test
  public void test() throws Exception {
    RetrieveJokeAsyncTask task =  new RetrieveJokeAsyncTask(null);
    String joke = task.execute().get();

    Assert.assertNotNull(joke);
  }
}
