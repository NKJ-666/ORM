package com.example.mydao.exception;

public class NoPointValueException extends Exception{
    private String message;

    public NoPointValueException(String message){
        super(message);
        this.message = message;
    }
}
