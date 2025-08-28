package com.biobac.warehouse.exception;

public class NotEnoughException extends RuntimeException{
    public NotEnoughException(String message) {
        super(message);
    }

}
