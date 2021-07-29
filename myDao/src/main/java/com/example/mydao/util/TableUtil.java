package com.example.mydao.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.Element;

import com.example.mydao.annotation.Default;
import com.example.mydao.annotation.Key;
import com.example.mydao.annotation.Nullable;
import com.example.mydao.annotation.Table;
import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.GetMethodException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NotMainKeyException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TableUtil {
    /**
     * 获取主键
     * @param fields bean类中所有声明的字段
     * @return 主键字段
     */
    public static Field getIDField(Field [] fields) throws CanOnlyOneKeyException {
        Field IDField = null;
        int flag = 0;
        for(Field field : fields){
            if(field.isAnnotationPresent(Key.class)){
                IDField = field;
                flag++;
                if (flag > 1){
                    throw new CanOnlyOneKeyException("a table can only have a main key");
                }
            }
        }
        return IDField;
    }

    /**
     * 获取表名
     * @param clazz bean类
     * @return 表名
     */
    public static String getTableName(Class<?> clazz){
        String tableName = null;
        if(clazz.isAnnotationPresent(Table.class)){
            Table table = clazz.getAnnotation(Table.class);
            tableName = table.tableName();
        }
        return tableName;
    }

    /**
     * 判断主键是否自增
     * @param field 主键字段
     * @return true or false
     */
    public static boolean isAutoIncrement(Field field) throws NotMainKeyException {
        boolean is = true;
        if(!field.isAnnotationPresent(Key.class)){
            throw new NotMainKeyException("the field must be main key!");
        }else{
            is = field.getAnnotation(Key.class).auto();
        }
        return is;
    }

    /**
     * java中数据类型和数据库中数据类型的映射
     * @param field 字段
     * @return 数据库中数据类型
     */
    public static String getDataType(Field field){
        String type = field.getType().getSimpleName();
        if(type.equals("int") || type.equals("Integer") || type.equalsIgnoreCase("boolean"))
        {
            return "INTEGER";
        }else if(type.equalsIgnoreCase("String")){
            return "TEXT";
        }else{
            return null;
        }
    }

    /**
     * 判断字段是否能为空
     * @param field 字段
     * @return 是否
     */
    public static boolean nullable(Field field){
        boolean isNullable = false;
        if(field.isAnnotationPresent(Nullable.class)){
            isNullable = true;
        }
        return isNullable;
    }

    /**
     * 获取字段的默认值
     * @param field 字段
     * @return 默认值
     */
    public static Object getDefaultValue(Field field) throws NoDefaultException {
        Object value = null;
        String type;
        if(!field.isAnnotationPresent(Nullable.class) && !field.isAnnotationPresent(Default.class))
            throw new NoDefaultException("not null field must have a default value");
        if(field.isAnnotationPresent(Default.class)){
            type = field.getType().getSimpleName();
            if(type.equalsIgnoreCase("String")){
                value = field.getAnnotation(Default.class).stringDef();
            }else if (type.equalsIgnoreCase("int")){
                value = field.getAnnotation(Default.class).IntegerDef();
            }else if(type.equalsIgnoreCase("boolean")){
                value = field.getAnnotation(Default.class).booleanDef();
            }
        }
        return value;

    }

    /**
     * 查询所找到的数据
     * @param clazz 表的映射类
     * @param selection 查询的语句
     * @param selectionArgs 字段所要比较的值
     * @param sd
     * @param <T>
     * @return 查询结果的列表
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static <T> List<T> query(Class<T> clazz, String selection, String [] selectionArgs, SQLiteDatabase sd) throws InstantiationException, IllegalAccessException, InvocationTargetException {
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

    public static String [] getAllFieldName(Field [] fields){
        String [] names = new String[fields.length];
        int flag = 0;
        for(Field field : fields){
            names[flag] = field.getName();
            flag++;
        }
        return names;
    }

    public static <T> ContentValues getValuesFromInstance(T instance) throws GetMethodException, InvocationTargetException, IllegalAccessException {
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
        return values;
    }

    public static boolean isTableHasExisted(String name, SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while(cursor.moveToNext()){
            if(name.equalsIgnoreCase(cursor.getString(0))){
                return true;
            }
        }
        return false;
    }
}
