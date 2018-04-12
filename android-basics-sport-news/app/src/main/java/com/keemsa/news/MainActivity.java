package com.keemsa.news;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NewsAsyncResponse {

    List<News> news;
    RecyclerView rcv_news;
    NewsAdapter adapter;
    ProgressBar prg_load;
    ImageButton imb_search;
    EditText etx_book_name;
    TextView txt_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rcv_news = (RecyclerView) findViewById(R.id.rcv_news);
        rcv_news.setLayoutManager(new LinearLayoutManager(this));

        prg_load = (ProgressBar) findViewById(R.id.prg_load);
        prg_load.setVisibility(View.INVISIBLE);

        etx_book_name = (EditText) findViewById(R.id.etx_book_name);

        txt_message = (TextView) findViewById(R.id.txt_message);

        imb_search = (ImageButton) findViewById(R.id.imb_search);
        imb_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check network connection
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    String stringUrl = getResources().getString(R.string.urlHeader) + etx_book_name.getText();
                    DownloadNewsInfoTask task = new DownloadNewsInfoTask(MainActivity.this);
                    task.execute(stringUrl);
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_connection, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public Resources getIResources() {
        return this.getResources();
    }

    @Override
    public void setMessage(int message) {
        String sMessage = "";
        if (message == NewsAsyncResponse.SERVER_ERROR) {
            sMessage = getResources().getString(R.string.server_error);
        } else if (message == NewsAsyncResponse.NO_NEWS) {
            sMessage = getResources().getString(R.string.no_news);
        }

        txt_message.setText(sMessage);
    }

    @Override
    public void toggleMessage(int value) {
        txt_message.setVisibility(value);
    }

    @Override
    public void processNews(List<News> newss) {
        adapter = new NewsAdapter(this, newss);
        rcv_news.setAdapter(adapter);
    }

    @Override
    public void toggleProgressBar(int value) {
        prg_load.setVisibility(value);
    }
}
