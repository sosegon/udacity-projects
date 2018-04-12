package com.keemsa.booklisting;

import android.opengl.Visibility;
import android.os.AsyncTask;
import android.util.Log;
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
 * Created by sebastian on 07/07/16.
 */
public class DownloadBookInfoTask extends AsyncTask<String, Void, String> {

    private BookAsyncResponse bookAsyncResponse;
    private int response;

    public DownloadBookInfoTask(BookAsyncResponse bookAsyncResponse) {
        this.bookAsyncResponse = bookAsyncResponse;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            String downloadedString = downloadUrl(urls[0]);
            return downloadedString;
        } catch (IOException e) {
            return "Unable to fetch data";
        }
    }

    @Override
    protected void onPreExecute() {
        bookAsyncResponse.toggleProgressBar(View.VISIBLE);
        bookAsyncResponse.toggleMessage(View.GONE);
    }

    @Override
    protected void onPostExecute(String s) {
        if(response != 200){
            bookAsyncResponse.setMessage(BookAsyncResponse.SERVER_ERROR);
            bookAsyncResponse.toggleMessage(View.VISIBLE);
            return;
        }

        List<Book> books = processJSON(s);

        if (books != null) {
            bookAsyncResponse.processBooks(books);
        } else {
            bookAsyncResponse.setMessage(BookAsyncResponse.NO_BOOKS);
            bookAsyncResponse.toggleMessage(View.VISIBLE);
        }
        bookAsyncResponse.toggleProgressBar(View.GONE);
    }

    private String downloadUrl(String myUrl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            response = connection.getResponseCode();

            Log.d("DEBUG_TAG", "The response is: " + response);
            is = connection.getInputStream();

            String contentAsString = readIt(is);

            return contentAsString;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream is) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(is, "UTF-8");
        BufferedReader bReader = new BufferedReader(reader);
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = bReader.readLine()) != null) {
            output.append(line);
        }

        return output.toString();
    }

    public List<Book> processJSON(String jsonString) {
        try {
            List<Book> books = new ArrayList<Book>();
            JSONObject reader = new JSONObject(jsonString);
            JSONArray booksArray = reader.getJSONArray("items");

            for (int i = 0; i < booksArray.length(); i++) {
                JSONObject jsonBook = booksArray.getJSONObject(i);
                JSONObject jsonInfo = jsonBook.getJSONObject("volumeInfo");
                String book_title = jsonInfo.optString("title");
                String book_author = jsonInfo.optString("authors");
                String book_type = jsonInfo.optString("printType");

                if (book_type.toUpperCase().equals("BOOK")) {
                    if (book_author.length() >= 2) {
                        book_author = book_author.substring(1, book_author.length() - 1);
                    }
                    else{
                        book_author = bookAsyncResponse.noAuthor();
                    }

                    books.add(new Book(book_title, book_author));
                }
            }

            return books;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
