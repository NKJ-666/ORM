package com.example.mydao.exception;

public class NoDefaultException extends Exception{
    private String message;

    public NoDefaultException(String message){
        super(message);
        this.message = message;
    }
}
