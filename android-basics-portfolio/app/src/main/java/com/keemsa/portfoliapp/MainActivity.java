package com.keemsa.portfoliapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView txt_movies, txt_stock, txt_bigger, txt_material, txt_ubi, txt_capstone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_movies = (TextView) findViewById(R.id.txt_movies);
        txt_stock = (TextView) findViewById(R.id.txt_stock);
        txt_bigger = (TextView) findViewById(R.id.txt_bigger);
        txt_material = (TextView) findViewById(R.id.txt_material);
        txt_ubi = (TextView) findViewById(R.id.txt_ubi);
        txt_capstone = (TextView) findViewById(R.id.txt_capstone);

        txt_movies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = MainActivity.this.getResources().getString(R.string.msg_app) + " " +
                             MainActivity.this.getResources().getString(R.string.lbl_movies);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });

        txt_stock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = MainActivity.this.getResources().getString(R.string.msg_app) + " " +
                             MainActivity.this.getResources().getString(R.string.lbl_stock);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });

        txt_bigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = MainActivity.this.getResources().getString(R.string.msg_app) + " " +
                             MainActivity.this.getResources().getString(R.string.lbl_bigger);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });

        txt_material.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = MainActivity.this.getResources().getString(R.string.msg_app) + " " +
                             MainActivity.this.getResources().getString(R.string.lbl_material);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });

        txt_ubi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = MainActivity.this.getResources().getString(R.string.msg_app) + " " +
                             MainActivity.this.getResources().getString(R.string.lbl_ubi);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });

        txt_capstone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = MainActivity.this.getResources().getString(R.string.msg_app) + " " +
                        MainActivity.this.getResources().getString(R.string.lbl_capstone);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
