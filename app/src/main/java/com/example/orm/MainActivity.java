package com.example.orm;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mydao.bean.Result;
import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.EmptyFieldException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NoPointValueException;
import com.example.mydao.exception.NotMainKeyException;
import com.example.mydao.helper.MyDatabaseHelper;
import com.example.mydao.util.SQLBuilderUtil;
import com.example.mydao.util.TableUtil;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class MainActivity extends AppCompatActivity {
    private MyDatabaseHelper helper;
    private Button createDB;
    private Button deleteDB;
    private Button createTable;
    private Button deleteTable;
    private Button changeTableName;
    private Button addNewColumn;
    private Button insertValue;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState){
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
        createTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    db.execSQL(SQLBuilderUtil.createTable(Result.class));
                } catch (EmptyFieldException e) {
                    e.printStackTrace();
                } catch (NotMainKeyException e) {
                    e.printStackTrace();
                } catch (NoDefaultException e){
                    e.printStackTrace();
                } catch (CanOnlyOneKeyException e){
                    e.printStackTrace();
                }
            }
        });
        deleteTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.execSQL(SQLBuilderUtil.deleteTableFromName("newName"));
            }
        });
        changeTableName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.execSQL(SQLBuilderUtil.changeTableName(TableUtil.getTableName(Result.class),"newName"));
            }
        });
        addNewColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    db.execSQL(SQLBuilderUtil.addNewColumn(Result.class,"id","integer", false,"0"));
                } catch (NoDefaultException e) {
                    e.printStackTrace();
                }
            }
        });
        insertValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.execSQL("insert into test(id,message) values(1,test)");
                //try {
                //    db.execSQL(SQLBuilderUtil.insert(Result.class,new String[]{"id","message"},new String[]{"1","test"}));
                //} catch (NoPointValueException e) {
                //    e.printStackTrace();
                //}
            }
        });
    }

    private void init(){
        createDB = findViewById(R.id.create_database);
        deleteDB = findViewById(R.id.delete_database);
        createTable = findViewById(R.id.create_table);
        deleteTable = findViewById(R.id.delete_table);
        changeTableName = findViewById(R.id.change_table_name);
        addNewColumn = findViewById(R.id.add_new_column);
        insertValue = findViewById(R.id.insert_value);
        helper = new MyDatabaseHelper(this,"test.db",null,1);
        db = helper.getWritableDatabase();
        SQLiteStudioService.instance().start(this);
    }
}