package com.example.orm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mydao.bean.Result;
import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.EmptyFieldException;
import com.example.mydao.exception.GetMethodException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NoPointValueException;
import com.example.mydao.exception.NotEveryHavePointer;
import com.example.mydao.exception.NotMainKeyException;
import com.example.mydao.helper.MyDatabaseHelper;
import com.example.mydao.myenum.SelectFlag;
import com.example.mydao.util.SQLBuilderUtil;
import com.example.mydao.util.TableUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

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
    private Button deleteValue;
    private Button selectValue;
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
                Result result = new Result();
                result.setCode(100);
                result.setMessage("test");
                try {
                    SQLBuilderUtil.insertFromInstance(result,db);
                } catch (GetMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        deleteValue.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                Result result = new Result();
                result.setMessage("test");
                result.setCode(100);
                try {
                    SQLBuilderUtil.deleteFromInstance(result, db);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
        selectValue.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String [] columnNames = new String[]{"code"};
                SelectFlag [] flags = new SelectFlag[]{SelectFlag.EQUAL};
                String [] compareValue = new String[]{"400"};
                try {
                    List<Result> results = SQLBuilderUtil.selectWhere(Result.class,columnNames,flags,compareValue,db);
                    for(Result result : results){
                        Log.d("MainActivity", "onClick: " + result.getCode() + result.getMessage());
                    }
                } catch (NotEveryHavePointer notEveryHavePointer) {
                    notEveryHavePointer.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
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
        deleteValue = findViewById(R.id.delete_value);
        selectValue = findViewById(R.id.select_value);
        helper = new MyDatabaseHelper(this,"test.db",null,1);
        db = helper.getWritableDatabase();
        SQLiteStudioService.instance().start(this);
    }
}