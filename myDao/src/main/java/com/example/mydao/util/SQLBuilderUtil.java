package com.example.mydao.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.mydao.annotation.Table;
import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.EmptyFieldException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NoPointValueException;
import com.example.mydao.exception.NotMainKeyException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
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

    public synchronized static String insert(Class<?> clazz, String [] columnName, String [] value) throws NoPointValueException {
        if (columnName.length != value.length)
            throw new NoPointValueException("Field name and value are inconsistent");
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ")
                .append(TableUtil.getTableName(clazz))
                .append("(");
        for(int i = 0; i < columnName.length; i++){
            builder.append(columnName[i])
                    .append(",");
        }
        if (builder.charAt(builder.length()-1) == ','){
            builder.deleteCharAt(builder.length()-1);
        }
        builder.append(")")
                .append(" values(");
        for(int i = 0; i < value.length; i++){
            builder.append(value[i])
                    .append(",");
        }
        if (builder.charAt(builder.length()-1) == ','){
            builder.deleteCharAt(builder.length()-1);
        }
        builder.append(")");
        return builder.toString();
    }

    public synchronized static void insert(Class<?> clazz, SQLiteDatabase db, HashMap values){
        ContentValues contentValues = new ContentValues();
        Iterator<Map.Entry> entries = values.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry entry = entries.next();
        }
    }
}
