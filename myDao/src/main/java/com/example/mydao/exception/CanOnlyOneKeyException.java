package com.example.mydao.exception;

public class CanOnlyOneKeyException extends Exception{
    private String message;

    public CanOnlyOneKeyException(String message){
        super(message);
        this.message = message;
    }
}
