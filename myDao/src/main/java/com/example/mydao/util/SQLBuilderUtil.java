package com.example.mydao.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.mydao.annotation.Default;
import com.example.mydao.annotation.Nullable;
import com.example.mydao.annotation.Table;
import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.EmptyFieldException;
import com.example.mydao.exception.GetMethodException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NotEveryHavePointer;
import com.example.mydao.exception.NotMainKeyException;
import com.example.mydao.exception.newColumnMustCanBeNull;
import com.example.mydao.myenum.SelectFlag;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                if(field.isAnnotationPresent(Default.class))
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
        String builder = "drop table " +
                TableUtil.getTableName(clazz);
        return builder;
    }

    public synchronized static String deleteTableFromName(String tableName){
        String builder = "drop table " +
                tableName;
        return builder;
    }

    public synchronized static String changeTableName(Class<?> clazz, String to) throws NoSuchFieldException, IllegalAccessException {
        Table table = clazz.getAnnotation(Table.class);
        InvocationHandler handler = Proxy.getInvocationHandler(table);
        Field declaredField = handler.getClass().getDeclaredField("memberValues");
        declaredField.setAccessible(true);
        Map memberValues = (Map) declaredField.get(handler);
        memberValues.put("tableName",to);
        String builder = "alter table " +
                TableUtil.getTableName(clazz) +
                " rename to " +
                to;
        return builder;
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

    public synchronized static String addNewColumn(Class<?> clazz, String column, String dataType) {
        String builder = "alter table " +
                TableUtil.getTableName(clazz) +
                " add " +
                column +
                " " +
                dataType;
        return builder;
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
        String [] names = TableUtil.getAllFieldName(fields);
        String [] compareValues = new String[names.length];
        Method [] methods = instance.getClass().getDeclaredMethods();
        for(int i = 0; i < names.length; i++){
            builder.append(names[i] + " = ? and ");
            for(Method method : methods){
                if(method.getName().startsWith("get") && method.getName().endsWith(names[i].substring(1))){
                    Object obj = method.invoke(instance);
                    if(obj != null){
                        compareValues[i] = obj.toString();
                    }
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
        return TableUtil.query(clazz,builder.toString(),selectionArgs,sd);
    }

    public static <T> List<T> selectWhere(List<T> list){
        return list;
    }

    public synchronized static <T,K> void updateFromInstance(T instance, String[] columnNames, SelectFlag[] flag, K[] compareValues, SQLiteDatabase sd) throws IllegalAccessException, GetMethodException, InvocationTargetException {
        StringBuilder builder = new StringBuilder();
        String [] selectionArgs = new String[columnNames.length];
        for(int i = 0; i < columnNames.length; i++){
            builder.append(columnNames[i] + " " + getFlag(flag[i]) + " ? ,");
            selectionArgs[i] = compareValues[i].toString();
        }
        if(builder.charAt(builder.length()-1) == ','){
            builder.deleteCharAt(builder.length()-1);
        }
        ContentValues values = TableUtil.getValuesFromInstance(instance);
        sd.update(TableUtil.getTableName(instance.getClass()),values, builder.toString(),selectionArgs);
    }

    public synchronized static <T> void updateFromInstance(T instance, List<T> list, SQLiteDatabase sd) throws IllegalAccessException, GetMethodException, InvocationTargetException {
        String tableName = TableUtil.getTableName(instance.getClass());
        ContentValues values = TableUtil.getValuesFromInstance(instance);
        Method[] methods = instance.getClass().getDeclaredMethods();
        List<Object> list1 = new ArrayList<>();
        if(methods.length == 0)
            throw new GetMethodException("the table's origin class no get method,you need to create that for every field");
        StringBuilder builder = new StringBuilder();
        for(T temp : list){
            for(Method method : methods){
                if(method.getName().startsWith("get")){
                    String[] totalName = method.getName().substring(3).toLowerCase().split("\\.");
                    String name = totalName[totalName.length-1];
                    Object obj = method.invoke(temp);
                    if (obj != null){
                        builder.append(name + " = " + "? and ");
                        list1.add(obj);
                    }
                }
            }
            builder.delete(builder.length()-4,builder.length()-1);
            String[] selectionArgs = new String[list1.size()];
            for(int i = 0; i < list1.size(); i++){
                selectionArgs[i] = list1.get(i).toString();
            }
            sd.update(tableName,values,builder.toString(),selectionArgs);
        }
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

    public synchronized static void updateTable(Class<?> clazz, SQLiteDatabase sd) throws newColumnMustCanBeNull, NoDefaultException, EmptyFieldException, CanOnlyOneKeyException, NotMainKeyException {
        int flag = 0;
        String tableName = TableUtil.getTableName(clazz);
        Field [] fields = clazz.getDeclaredFields();
        Cursor cursor = sd.rawQuery("pragma table_info(" + tableName + ")", null);
        while (cursor.moveToNext()){
            flag++;
        }
        cursor.close();
        if(flag == fields.length){
        }else if (flag < fields.length){
            for (Field field : fields) {
                Cursor cursor1 = sd.rawQuery("select * from " + tableName + " limit 0", null);
                String name = field.getName();
                if (cursor1.getColumnIndex(name) == -1) {
                    if (!field.isAnnotationPresent(Nullable.class)) {
                        throw new newColumnMustCanBeNull("new column must can be null");
                    } else {
                        StringBuilder builder = new StringBuilder();
                        builder.append("alter table ")
                                .append(tableName)
                                .append(" add column ")
                                .append(field.getName())
                                .append(" ")
                                .append(TableUtil.getDataType(field));
                        if (field.isAnnotationPresent(Default.class)) {
                            builder.append(" default ")
                                    .append(TableUtil.getDefaultValue(field));
                        } else {
                            builder.append(" default ")
                                    .append("null");
                        }
                        sd.execSQL(builder.toString());
                    }
                }
            }
        }else{
            TableUtil.change(tableName,"temporaryTable");
            sd.execSQL(createTable(clazz));
            StringBuilder builder = new StringBuilder();
            builder.append("insert into ")
                    .append(tableName)
                    .append(" (");
            for (Field value : fields) {
                builder.append(value.getName())
                        .append(",");
            }
            if(builder.charAt(builder.length()-1) == ','){
                builder.deleteCharAt(builder.length()-1);
            }
            builder.append(") select ");
            for (Field field : fields) {
                builder.append(field.getName())
                        .append(",");
            }
            if(builder.charAt(builder.length()-1) == ','){
                builder.deleteCharAt(builder.length()-1);
            }
            builder.append(" from temporaryTable");
            sd.execSQL(builder.toString());
            deleteTableFromName("temporaryTable");
        }
    }

    public static <T,K> List<T> and(Class<T> clazz, String[] columnNames, SelectFlag[] flag, K[] compareValues, SQLiteDatabase sd) throws NotEveryHavePointer, IllegalAccessException, InvocationTargetException, InstantiationException {
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
        return TableUtil.query(clazz,builder.toString(),selectionArgs,sd);
    }

    public static <T,K> List<T> or(Class<T> clazz, String[] columnNames, SelectFlag[] flags, K[] compareValues, SQLiteDatabase sd) throws NotEveryHavePointer, InstantiationException, IllegalAccessException, InvocationTargetException {
        if(columnNames.length != compareValues.length || columnNames.length != flags.length)
            throw new NotEveryHavePointer("every column must have a pointer");
        List<T> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("select * from ")
                .append(TableUtil.getTableName(clazz))
                .append(" where ");
        String [] selectionArgs = new String[columnNames.length];
        for(int i = 0; i < columnNames.length; i++){
            builder.append(columnNames[i] + " " + getFlag(flags[i]) + " ? or ");
            selectionArgs[i] = compareValues[i].toString();
        }
        builder.delete(builder.length()-3,builder.length()-1);
        Cursor cursor = sd.rawQuery(builder.toString(),selectionArgs);
        return TableUtil.query(cursor,clazz);
    }
}
