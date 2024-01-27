package com.supportportalMehdi.demo.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = 432_000_000; // 5 days expressed in millisecond
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token" ;
    public static final String TOKEN_CANNOT_BE_VERIFIEED = "Token cannot be verified" ;
    public static final String GET_ARRAYS_LLC = "Get Arrays, LLC" ;
    public static final String GET_ARRAYS_ADMINISTATION = "User Managmenet Portal" ;
    public static final String AUTHORITIES = "AUTHORITIES" ;
    public static final String FORBIDDEN_MESSAGE = "You need to log in to access this page" ;
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page" ;
    public static final String OPTIONS_HTTP_METHODE = "OPTIONS" ;
    //public static final String[] PUBLIC_URLS = {"/user/login","/user/register","/user/image/**","**/upload","/**/upload","**/**/upload"} ;
    public static final String[] PUBLIC_URLS = {"/","/user/login","/user/register","/user/image/**","/**/upload","/**/all"} ;
    //public static final String[] PUBLIC_URLS = {"**"} ;

}
