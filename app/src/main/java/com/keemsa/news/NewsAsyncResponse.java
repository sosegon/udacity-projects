package com.keemsa.news;

import android.content.Context;
import android.content.res.Resources;

import java.util.List;

/**
 * Created by sebastian on 09/07/16.
 */
public interface NewsAsyncResponse {

    int SERVER_ERROR = 0;
    int NO_NEWS = 1;

    Resources getIResources();

    void toggleProgressBar(int value);

    void toggleMessage(int value);

    void setMessage(int message);

    void processNews(List<News> news);
}
