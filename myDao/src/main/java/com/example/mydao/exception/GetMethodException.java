package com.example.mydao.exception;

public class GetMethodException extends Exception{
    private String message;

    public GetMethodException(String message){
        super(message);
        this.message = message;
    }
}
