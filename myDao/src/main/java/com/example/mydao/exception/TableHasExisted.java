package com.example.mydao.exception;

public class TableHasExisted extends Exception{
    private String message;

    public TableHasExisted(String message){
        super(message);
        this.message = message;
    }
}
