package com.example.orm.bean;

import com.example.orm.annotation.IntegerDefault;
import com.example.orm.annotation.Key;
import com.example.orm.annotation.Nullable;
import com.example.orm.annotation.StringDefault;
import com.example.orm.annotation.Table;

@Table(tableName = "test")
public class Result {
    @Key
    @Nullable(nullable = false)
    @IntegerDefault(def = 0)
    private Integer code;

    @Nullable(nullable = false)
    @StringDefault(def = "")
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
