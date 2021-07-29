package com.example.mydao.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.EmptyFieldException;
import com.example.mydao.exception.GetMethodException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NotEveryHavePointer;
import com.example.mydao.exception.NotMainKeyException;
import com.example.mydao.exception.TableHasExisted;
import com.example.mydao.myenum.SelectFlag;
import com.example.mydao.util.SQLBuilderUtil;
import com.example.mydao.util.TableUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MyDaoHelper {
    private final SQLiteDatabase db;

    public MyDaoHelper(Context context, String databaseName, int version){
        db = new MyDatabaseHelper(context,databaseName,null,version).getWritableDatabase();
    }

    public void createTable(Class<?> clazz) throws CanOnlyOneKeyException, NotMainKeyException, EmptyFieldException, NoDefaultException, TableHasExisted {
        String name = TableUtil.getTableName(clazz);
        if(TableUtil.isTableHasExisted(name,db)){
            throw new TableHasExisted("the table " + name + " has existed");
        }else
            db.execSQL(SQLBuilderUtil.createTable(clazz));
    }

    public void deleteTable(Class<?> clazz){
        db.execSQL(SQLBuilderUtil.deleteTable(clazz));
    }

    public void deleteTableFromName(String tableName){
        db.execSQL(SQLBuilderUtil.deleteTableFromName(tableName));
    }

    public void changeTableName(String from, String to){
        db.execSQL(SQLBuilderUtil.changeTableName(from,to));
    }

    public void addNewColumn(Class<?> clazz, String column, String dataType, boolean nullable, String defaultValue) throws NoDefaultException {
        db.execSQL(SQLBuilderUtil.addNewColumn(clazz, column, dataType, nullable, defaultValue));
    }

    public void addNewColumn(Class<?> clazz, String column, String dataType) throws NoDefaultException {
        db.execSQL(SQLBuilderUtil.addNewColumn(clazz, column, dataType));
    }

    public <T> void insertFromJson(String json,Class<T> clazz) throws IllegalAccessException, GetMethodException, InvocationTargetException {
        SQLBuilderUtil.insertFromJson(json,clazz,this.db);
    }

    public <T> void insertFromInstance(T instance) throws IllegalAccessException, GetMethodException, InvocationTargetException {
        SQLBuilderUtil.insertFromInstance(instance,db);
    }

    public <K> void deleteByKey(K key, Class<?> table) throws CanOnlyOneKeyException {
        SQLBuilderUtil.deleteByKey(key,table,db);
    }

    public <T> void deleteFromInstance(T instance) throws InvocationTargetException, IllegalAccessException {
        SQLBuilderUtil.deleteFromInstance(instance,db);
    }

    public void deleteAll(Class<?> table){
        SQLBuilderUtil.deleteAll(table,db);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public <T,K> List<T> selectWhere(Class<T> clazz, String[] columnNames, SelectFlag[] flag, K[] compareValues) throws InvocationTargetException, InstantiationException, NotEveryHavePointer, IllegalAccessException {
        return SQLBuilderUtil.selectWhere(clazz,columnNames,flag,compareValues,db);
    }

    public <T,K> void updateFromInstance(T instance, String[] columnNames, SelectFlag[] flag, K[] compareValues) throws IllegalAccessException, GetMethodException, InvocationTargetException {
        SQLBuilderUtil.updateFromInstance(instance,columnNames,flag,compareValues,db);
    }

    public String getTableName(Class<?> clazz){
        return TableUtil.getTableName(clazz);
    }
}
