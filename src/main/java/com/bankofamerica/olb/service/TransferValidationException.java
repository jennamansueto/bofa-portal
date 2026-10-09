package com.bankofamerica.olb.service;

/** Carries a message-resource key plus optional arguments back to the Action. */
public class TransferValidationException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String key;
    private final Object[] args;
    public TransferValidationException(String key) { this(key, null); }
    public TransferValidationException(String key, Object[] args) {
        super(key);
        this.key = key;
        this.args = args;
    }
    public String getKey() { return key; }
    public Object[] getArgs() { return args; }
}
