package com.example.mydao.bean;


import com.example.mydao.annotation.Key;
import com.example.mydao.annotation.Nullable;
import com.example.mydao.annotation.Default;
import com.example.mydao.annotation.Table;

@Table(tableName = "test")
public class Result {
    @Key
    @Default(IntegerDef = 0)
    private Integer code;

    @Nullable
    @Default(stringDef = "test")
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
