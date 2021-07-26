package com.example.orm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.orm.helper.MyDatabaseHelper;

public class MainActivity extends AppCompatActivity {
    private MyDatabaseHelper helper;
    private Button createDB;
    private Button deleteDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        createDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.getWritableDatabase();
            }
        });
        deleteDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDatabase("test.db");
            }
        });
    }

    private void init(){
        createDB = findViewById(R.id.create_database);
        deleteDB = findViewById(R.id.delete_database);
        helper = new MyDatabaseHelper(this,"test.db",null,1);
    }
}