package com.keemsa.news;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian on 09/07/16.
 */
public class DownloadNewsInfoTask extends AsyncTask<String, Void, String> {

    private NewsAsyncResponse newsAsyncResponse;
    private int response;

    public DownloadNewsInfoTask(NewsAsyncResponse newsAsyncResponse) {
        this.newsAsyncResponse = newsAsyncResponse;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            String downloadedString = downloadUrl(urls[0]);
            return downloadedString;
        } catch (IOException e) {
            return newsAsyncResponse.getIResources().getString(R.string.cant_fetch_data);
        }
    }

    private String downloadUrl(String myUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            response = connection.getResponseCode();

            is = connection.getInputStream();

            String contentAsString = readIt(is);

            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String readIt(InputStream is) throws IOException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(is, "UTF-8");
        BufferedReader bReader = new BufferedReader(reader);
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = bReader.readLine()) != null) {
            output.append(line);
        }

        return output.toString();
    }

    @Override
    protected void onPreExecute() {
        newsAsyncResponse.toggleProgressBar(View.VISIBLE);
        newsAsyncResponse.toggleMessage(View.GONE);
    }

    @Override
    protected void onPostExecute(String s) {
        if (response != 200) {
            newsAsyncResponse.setMessage(newsAsyncResponse.SERVER_ERROR);
            newsAsyncResponse.toggleMessage(View.VISIBLE);
            return;
        }

        List<News> news = processJSON(s);

        if (news != null && news.size() > 0) {
            newsAsyncResponse.processNews(news);
        } else {
            newsAsyncResponse.setMessage(newsAsyncResponse.NO_NEWS);
            newsAsyncResponse.toggleMessage(View.VISIBLE);
        }
        newsAsyncResponse.toggleProgressBar(View.GONE);
    }

    public List<News> processJSON(String jsonString) {
        try {
            List<News> newss = new ArrayList<News>();
            JSONObject reader = new JSONObject(jsonString);
            JSONObject mainResponse = reader.getJSONObject("response");
            JSONArray newsArray = mainResponse.getJSONArray("results");

            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject jsonNews = newsArray.getJSONObject(i);
                String news_headline = jsonNews.optString("webTitle");
                String news_url = jsonNews.optString("webUrl");
                String news_type = jsonNews.optString("type");
                //News newNews = new News(news_type, news_url, news_headline);
                newss.add(new News(news_type, news_url, news_headline, ""));

                if (jsonNews.has("fields")) {
                    JSONObject jsonFields = jsonNews.getJSONObject("fields");
                    if (jsonFields.has("thumbnail")) {
                        newss.get(newss.size() - 1).setThumbnailUrl(jsonFields.optString("thumbnail"));
                    }
                }
            }

            return newss;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
