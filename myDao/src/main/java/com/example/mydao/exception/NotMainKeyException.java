package com.example.mydao.exception;

public class NotMainKeyException extends Exception{
    private String message;

    public NotMainKeyException(String message){
        super(message);
        this.message = message;
    }
}
