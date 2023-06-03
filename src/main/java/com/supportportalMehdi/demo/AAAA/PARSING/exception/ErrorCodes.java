package com.supportportalMehdi.demo.AAAA.PARSING.exception;

public enum ErrorCodes {

    BAD_CREDENTIALS(400),
    ARTICLE_NOT_FOUND(1000),
    ARTICLE_NOT_VALID(1001),
    FILE_ALREADY_IN_USE(1002),

    ROLE_NOT_FOUND(14000),
    ROLE_NOT_VALID(14001),

    // Liste des exceptions techniques
    UPDATE_PHOTO_EXCEPTION(15000),
    UNKNOWN_CONTEXT(15001),

    ;
    private int code ;

    ErrorCodes(int code ) {
        this.code = code ;
    }
    public int getCode(){
        return code ;
    }
}
