package com.example.mydao.exception;

public class NotEveryHavePointer extends Exception{
    private String message;

    public NotEveryHavePointer(String message){
        super(message);
        this.message = message;
    }
}
