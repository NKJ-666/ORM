package com.example.mydao.exception;

public class newColumnMustCanBeNull extends Exception{
    private String message;

    public newColumnMustCanBeNull(String message){
        super(message);
        this.message = message;
    }
}
