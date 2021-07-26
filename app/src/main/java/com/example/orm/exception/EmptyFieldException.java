package com.example.orm.exception;

public class EmptyFieldException extends Exception{
    private String message;

    public EmptyFieldException(String message){
        super(message);
        this.message = message;
    }
}
