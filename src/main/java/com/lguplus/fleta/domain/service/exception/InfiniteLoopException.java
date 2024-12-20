package com.lguplus.fleta.domain.service.exception;

public class InfiniteLoopException extends Exception{
    public InfiniteLoopException(){
        this("This Synchronizer have potential Infinite Loop. Please Check ");
    }

    public InfiniteLoopException(String message) {
        super(message);
    }

    public InfiniteLoopException(String message, Throwable cause) {
        super(message, cause);
    }
}
