package com.example.orm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.EmptyFieldException;
import com.example.mydao.exception.GetMethodException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NotEveryHavePointer;
import com.example.mydao.exception.NotMainKeyException;
import com.example.mydao.exception.TableHasExisted;
import com.example.mydao.helper.MyDaoHelper;
import com.example.mydao.myenum.SelectFlag;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class MainActivity extends AppCompatActivity {
    private MyDaoHelper helper;
    private Button deleteDB;
    private Button createTable;
    private Button deleteTable;
    private Button insertValue;
    private Button deleteValue;
    private Button selectValue;
    private Button updateValue;
    private Button updateTable;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
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
                    helper.createTable(Result.class);
                } catch (CanOnlyOneKeyException e) {
                    e.printStackTrace();
                } catch (NotMainKeyException e) {
                    e.printStackTrace();
                } catch (EmptyFieldException e) {
                    e.printStackTrace();
                } catch (NoDefaultException e) {
                    e.printStackTrace();
                } catch (TableHasExisted tableHasExisted) {
                    tableHasExisted.printStackTrace();
                }
            }
        });
        deleteTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.deleteTable(Result.class);
            }
        });
        insertValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Result result = new Result();
                result.setCode(100);
                result.setMessage("test");
                try {
                    helper.insertFromInstance(result);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (GetMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
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
                    helper.deleteFromInstance(result);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        selectValue.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String [] columnNames = new String[]{"code","code"};
                SelectFlag [] flags = new SelectFlag[]{SelectFlag.EQUAL,SelectFlag.EQUAL};
                String [] compareValue = new String[]{"400","200"};
                List<Result> results = null;
                try {
                    results = helper.selectWhere(helper.or(Result.class,columnNames,flags,compareValue));
                    for(Result result : results){
                        Log.d("MainActivity", "onClick: " + result.getCode() + result.getMessage());
                    }
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (NotEveryHavePointer notEveryHavePointer) {
                    notEveryHavePointer.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        updateValue.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Result result = new Result();
                result.setCode(200);
                result.setMessage("new");
                try {
                    helper.updateFromInstance(result);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (GetMethodException e) {
                    e.printStackTrace();
                } catch (NotEveryHavePointer notEveryHavePointer) {
                    notEveryHavePointer.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (CanOnlyOneKeyException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        });
        updateTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    helper.updateTable(Result.class);
                } catch (NoDefaultException e) {
                    e.printStackTrace();
                } catch (com.example.mydao.exception.newColumnMustCanBeNull newColumnMustCanBeNull) {
                    newColumnMustCanBeNull.printStackTrace();
                } catch (EmptyFieldException e) {
                    e.printStackTrace();
                } catch (NotMainKeyException e) {
                    e.printStackTrace();
                } catch (CanOnlyOneKeyException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void init(){
        deleteDB = findViewById(R.id.delete_database);
        createTable = findViewById(R.id.create_table);
        deleteTable = findViewById(R.id.delete_table);
        insertValue = findViewById(R.id.insert_value);
        deleteValue = findViewById(R.id.delete_value);
        selectValue = findViewById(R.id.select_value);
        updateValue = findViewById(R.id.update_value);
        updateTable = findViewById(R.id.update_table);
        helper = new MyDaoHelper(this,"test.db",1);
        SQLiteStudioService.instance().start(this);
    }
}