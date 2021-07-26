package com.example.orm.exception;

public class NotMainKeyException extends Exception{
    private String message;

    public NotMainKeyException(String message){
        super(message);
        this.message = message;
    }
}
