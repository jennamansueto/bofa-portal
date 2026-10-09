package com.bankofamerica.olb.dao;

public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public DataAccessException(String msg, Throwable cause) { super(msg, cause); }
}
