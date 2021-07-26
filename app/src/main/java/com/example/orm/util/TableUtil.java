package com.example.orm.util;

import android.renderscript.Element;

import com.example.orm.annotation.Key;
import com.example.orm.annotation.Table;
import com.example.orm.exception.NotMainKeyException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class TableUtil {
    /**
     * 获取主键
     * @param fields bean类中所有声明的字段
     * @return 主键字段
     */
    public static Field getIDField(Field [] fields){
        Field IDField = null;
        for(Field field : fields){
            if(field.isAnnotationPresent(Key.class)){
                IDField = field;
                break;
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
        if(type.equals("int") || type.equals("Integer") || type.equalsIgnoreCase("byte") ||
            type.equalsIgnoreCase("short") || type.equalsIgnoreCase("long")||
            type.equalsIgnoreCase("boolean"))
        {
            return "INTEGER";
        }else if(type.equals("float") || type.equals("Float") || type.equals("Double") || type.equals("double")){
            return "REAL";
        }else if (type.equals("Date")){
            return "DATE";
        }else{
            return "TEXT";
        }
    }
}
