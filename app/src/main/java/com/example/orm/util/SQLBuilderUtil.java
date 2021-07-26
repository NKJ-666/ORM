package com.example.orm.util;

import com.example.orm.exception.EmptyFieldException;
import com.example.orm.exception.NotMainKeyException;

import java.lang.reflect.Field;

public class SQLBuilderUtil {
    public static String createTable(Class<?> clazz) throws EmptyFieldException, NotMainKeyException {
        StringBuilder builder = new StringBuilder();
        Field [] fields = clazz.getDeclaredFields();
        if(fields == null || fields.length == 0){
            throw new EmptyFieldException("class fields can't be empty!");
        }

        builder.append("create table ")
                .append(TableUtil.getTableName(clazz))
                .append("(");
        Field field = TableUtil.getIDField(fields);
        if(field != null){
            boolean isAuto = TableUtil.isAutoIncrement(field);
            builder.append(field.getName())
                    .append(" ")
                    .append(TableUtil.getDataType(field));
            if(isAuto){
                builder.append(" "+"auto in");
            }
        }
        return null;
    }
}
