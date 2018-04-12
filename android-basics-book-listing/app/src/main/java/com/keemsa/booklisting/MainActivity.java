package com.keemsa.booklisting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements BookAsyncResponse {

    ArrayList<Book> books = new ArrayList<Book>();
    BookAdapter adapter;
    ListView lsv_books;
    ProgressBar pgr_load;
    TextView txt_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pgr_load = (ProgressBar) findViewById(R.id.prg_load);
        toggleProgressBar(View.GONE);

        txt_message = (TextView) findViewById(R.id.txt_message);
        toggleMessage(View.GONE);

        adapter = new BookAdapter(MainActivity.this, books);
        lsv_books = (ListView) findViewById(R.id.lsv_books);
        lsv_books.setAdapter(adapter);

        final EditText etx_book_name = (EditText) findViewById(R.id.etx_book_name);
        ImageButton imb_search = (ImageButton) findViewById(R.id.imb_search);

        imb_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check network connection
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    // empty list
                    flushBooks();
                    // fetch data
                    String stringUrl = getResources().getString(R.string.base_url) + etx_book_name.getText();
                    DownloadBookInfoTask task = new DownloadBookInfoTask(MainActivity.this);
                    task.execute(stringUrl);
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    public void flushBooks() {
        int nBooks = adapter.getCount();
        if (nBooks > 0) {
            int index = adapter.getCount() - 1;
            while (books.size() > 0) {
                adapter.remove(adapter.getItem(index));
                index--;
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void processBooks(List<Book> books) {
        for (Book book : books) {
            adapter.add(book);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void toggleProgressBar(int value) {
        pgr_load.setVisibility(value);
    }

    @Override
    public String noAuthor(){
        return getResources().getString(R.string.unknown_author);
    }

    @Override
    public void toggleMessage(int value){
        txt_message.setVisibility(value);
    }

    @Override
    public void setMessage(int message){
        String sMessage = "";
        if(message == BookAsyncResponse.SERVER_ERROR){
            sMessage = getResources().getString(R.string.server_error);
        }
        else if(message == BookAsyncResponse.NO_BOOKS){
            sMessage = getResources().getString(R.string.no_books);
        }

        txt_message.setText(sMessage);
    }
}


