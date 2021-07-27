package com.example.mydao.util;

import android.renderscript.Element;

import com.example.mydao.annotation.Default;
import com.example.mydao.annotation.Key;
import com.example.mydao.annotation.Nullable;
import com.example.mydao.annotation.Table;
import com.example.mydao.exception.CanOnlyOneKeyException;
import com.example.mydao.exception.NoDefaultException;
import com.example.mydao.exception.NotMainKeyException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

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
}
