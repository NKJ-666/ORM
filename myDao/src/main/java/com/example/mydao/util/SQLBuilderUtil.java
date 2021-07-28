package com.example.mydao.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.mydao.annotation.Table;
import com.example.mydao.bean.Result;
import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.EmptyFieldException;
import com.example.mydao.exception.GetMethodException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NoPointValueException;
import com.example.mydao.exception.NotEveryHavePointer;
import com.example.mydao.exception.NotMainKeyException;
import com.example.mydao.myenum.SelectFlag;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SQLBuilderUtil {
    public synchronized static String createTable(Class<?> clazz) throws EmptyFieldException, NotMainKeyException , NoDefaultException , CanOnlyOneKeyException {
        StringBuilder builder = new StringBuilder();
        Field [] fields = clazz.getDeclaredFields();
        if(fields == null || fields.length == 0){
            throw new EmptyFieldException("class fields can't be empty!");
        }

        builder.append("create table ")
                .append(TableUtil.getTableName(clazz))
                .append("(");
        Field IDField = TableUtil.getIDField(fields);
        if(IDField != null){
            boolean isAuto = TableUtil.isAutoIncrement(IDField);
            builder.append(IDField.getName())
                    .append(" ")
                    .append(TableUtil.getDataType(IDField));
            if(isAuto){
                builder.append(" auto_increment");
            }
            builder.append(" primary key not null,");
        }
        for(Field field : fields){
            if(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()))
                continue;
            if(!field.equals(IDField)){
                builder.append(field.getName())
                        .append(" ")
                        .append(TableUtil.getDataType(field));
                if(!TableUtil.nullable(field)){
                    builder.append(" not null");
                }
                builder.append(" default ")
                        .append(TableUtil.getDefaultValue(field).toString())
                        .append(",");
            }
        }
        if(builder.charAt(builder.length()-1) == ','){
            builder.deleteCharAt(builder.length()-1);
        }
        builder.append(")");
        return builder.toString();
    }

    public synchronized static String deleteTable(Class<?> clazz){
        StringBuilder builder = new StringBuilder();
        builder.append("drop table ")
                .append(TableUtil.getTableName(clazz));
        return builder.toString();
    }

    public synchronized static String deleteTableFromName(String tableName){
        StringBuilder builder = new StringBuilder();
        builder.append("drop table ")
                .append(tableName);
        return builder.toString();
    }

    public synchronized static String changeTableName(String from, String to){
        StringBuilder builder = new StringBuilder();
        builder.append("alter table ")
                .append(from)
                .append(" rename to ")
                .append(to);
        return builder.toString();
    }

    public synchronized static String addNewColumn(Class<?> clazz, String column, String dataType, boolean nullable, String defaultValue)
        throws NoDefaultException{
        StringBuilder builder = new StringBuilder();
        builder.append("alter table ")
                .append(TableUtil.getTableName(clazz))
                .append(" add ")
                .append(column)
                .append(" ")
                .append(dataType);
        if(defaultValue == null)
            throw new NoDefaultException("default value can not be null");
        if(defaultValue.trim().isEmpty())
            throw new NoDefaultException("the field must have default value, because it can't be null");
        builder.append(" default ")
                .append(defaultValue);
        if(!nullable){
            builder.append(" not null");
        }
        return builder.toString();
    }

    public synchronized static String addNewColumn(Class<?> clazz, String column, String dataType)
            throws NoDefaultException{
        StringBuilder builder = new StringBuilder();
        builder.append("alter table ")
                .append(TableUtil.getTableName(clazz))
                .append(" add ")
                .append(column)
                .append(" ")
                .append(dataType);
        return builder.toString();
    }

    public synchronized static <T> void insertFromJson(String json,Class<T> clazz, SQLiteDatabase sd) throws IllegalAccessException, InvocationTargetException, GetMethodException {
        Gson gson = new Gson();
        T t = gson.fromJson(json,clazz);
        insertFromInstance(t,sd);
    }

    public synchronized static <T> void insertFromInstance(T instance, SQLiteDatabase sd) throws GetMethodException, InvocationTargetException, IllegalAccessException {
        Method[] methods = instance.getClass().getDeclaredMethods();
        ContentValues values = new ContentValues();
        if(methods.length == 0)
            throw new GetMethodException("the table's origin class no get method,you need to create that for every field");
        for(Method method : methods){
            if(method.getName().startsWith("get")){
                String[] totalType = method.getReturnType().toString().split("\\.");
                String type = totalType[totalType.length-1];
                String[] totalName = method.getName().substring(3).toLowerCase().split("\\.");
                String name = totalName[totalName.length-1];
                Object obj = method.invoke(instance);
                if(obj != null){
                    if(type.equalsIgnoreCase("int") || type.equalsIgnoreCase("integer")){
                        values.put(name,(int) obj);
                    }else if(type.equalsIgnoreCase("String")){
                        values.put(name,(String) obj);
                    }else if(type.equalsIgnoreCase("boolean")){
                        values.put(name, (boolean) obj);
                    }
                }
            }
        }
        sd.insert(TableUtil.getTableName(instance.getClass()),"null",values);
    }

    public synchronized static <K> void deleteByKey(K key, Class<?> table, SQLiteDatabase sd) throws CanOnlyOneKeyException {
        Field field = TableUtil.getIDField(table.getDeclaredFields());
        String [] totalName = field.getName().split("\\.");
        String name = totalName[totalName.length-1];
        sd.delete(TableUtil.getTableName(table), name + " = ?", new String[]{key+""});
    }

    public synchronized static <T> void deleteFromInstance(T instance, SQLiteDatabase sd) throws IllegalAccessException, InvocationTargetException {
        StringBuilder builder = new StringBuilder();
        Field [] fields = instance.getClass().getDeclaredFields();
        String [] names = getAllFieldName(fields);
        String [] compareValues = new String[names.length];
        Method [] methods = instance.getClass().getDeclaredMethods();
        for(int i = 0; i < names.length; i++){
            builder.append(names[i] + " = ? and ");
            for(Method method : methods){
                if(method.getName().startsWith("get") && method.getName().endsWith(names[i].substring(1))){
                    compareValues[i] = Objects.requireNonNull(method.invoke(instance)).toString();
                    break;
                }
            }
        }
        builder.delete(builder.length()-4,builder.length()-1);
        sd.delete(TableUtil.getTableName(instance.getClass()),builder.toString(),compareValues);
    }

    public synchronized static void deleteAll(Class<?> table, SQLiteDatabase sd){
        sd.delete(TableUtil.getTableName(table),null,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static <T,K> List<T> selectWhere(Class<T> clazz, String[] columnNames, SelectFlag[] flag, K[] compareValues, SQLiteDatabase sd) throws NotEveryHavePointer, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<T> list = new ArrayList<>();
        if(columnNames.length != compareValues.length || columnNames.length != flag.length)
            throw new NotEveryHavePointer("every column must have a pointer");
        StringBuilder builder = new StringBuilder();
        String [] selectionArgs = new String[columnNames.length];
        for(int i = 0; i < columnNames.length; i++){
            builder.append(columnNames[i] + " " + getFlag(flag[i]) + " ? ,");
            selectionArgs[i] = compareValues[i].toString();
        }
        if(builder.charAt(builder.length()-1) == ','){
            builder.deleteCharAt(builder.length()-1);
        }
        return query(clazz,builder.toString(),selectionArgs,sd);
    }

    private static String [] getAllFieldName(Field [] fields){
        String [] names = new String[fields.length];
        int flag = 0;
        for(Field field : fields){
            names[flag] = field.getName();
            flag++;
        }
        return names;
    }

    private static <T> List<T> query(Class<T> clazz, String selection, String [] selectionArgs, SQLiteDatabase sd) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        List<T> list = new ArrayList<>();
        Cursor cursor = sd.query(TableUtil.getTableName(clazz),null,selection, selectionArgs,null,null,null);
        if(cursor.moveToFirst()){
            do{
                T t = clazz.newInstance();
                Method[] methods = clazz.getDeclaredMethods();
                for(Method method : methods){
                    if(method.getName().startsWith("set")){
                        Class[] columnTypes = method.getParameterTypes();
                        String name = method.getName().toLowerCase().substring(3);
                        if(columnTypes.length > 1)
                            continue;
                        String [] totalType = columnTypes[0].toString().split("\\.");
                        String type = totalType[totalType.length-1];
                        if(type.equalsIgnoreCase("Integer") || type.equalsIgnoreCase("int")){
                            method.invoke(t, cursor.getInt(cursor.getColumnIndex(name)));
                        }else if(type.equalsIgnoreCase("String")){
                            method.invoke(t, cursor.getString(cursor.getColumnIndex(name)));
                        }else if(type.equalsIgnoreCase("boolean")){
                            method.invoke(t, cursor.getInt(cursor.getColumnIndex(name)) == 1);
                        }
                    }
                }
                list.add(t);
            }while (cursor.moveToNext());
        }
        return list;
    }

    private static String getFlag(SelectFlag flag){
        switch (flag){
            case EQUAL:
                return "=";
            case GREATER:
                return ">";
            case LESS:
                return "<";
            case GREATER_OR_EQUAL:
                return ">=";
            case LESS_OR_EQUAL:
                return "<=";
            default:
                return null;
        }
    }
}
