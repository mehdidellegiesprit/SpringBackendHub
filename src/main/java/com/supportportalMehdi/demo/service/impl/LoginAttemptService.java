package com.supportportalMehdi.demo.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Service
public class LoginAttemptService {
    private static final int MAXIMUM_NUMBER_OF_ATTEMPT = 5;
    private static final int ATTEMPT_INCREMENT = 1;
    private LoadingCache<String,Integer> loginAttemptCache ;

    public LoginAttemptService (){
        super();
        loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100).build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }
    public void evictUserFromLoginAttemptCache(String username){
        /// elle va supprimer l user by username from the cache
        loginAttemptCache.invalidate(username);
    }
    public void addUserToLoginAttemptCache(String username) {
        System.out.println("addUserToLoginAttemptCache");
//                -------------cache----------
//                User                Attempts
//                user1               2
//                user2               3
//                user3               1
//        this is how it works : a chaque fois il me provide un faux mot de passe je vais incrementeé
        int attempts = 0 ;
        try {
            attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username) ;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        loginAttemptCache.put(username,attempts);
    }
    public boolean hasExceedMaxAttempts(String username) {
        System.out.println("hasExceedMaxAttempts");
        try {
            return loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPT ;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
