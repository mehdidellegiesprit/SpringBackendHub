package com.supportportalMehdi.demo.listener;

import com.supportportalMehdi.demo.domain.User;
import com.supportportalMehdi.demo.domain.UserPrincipal;
import com.supportportalMehdi.demo.service.impl.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class AuthenticationSuccessListener {
    private LoginAttemptService loginAttemptService ;

    @Autowired
    public AuthenticationSuccessListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) throws ExecutionException {
        /// if the authentication is success i have as an object a User
        Object principal1 = event.getAuthentication().getPrincipal();
        if (principal1 instanceof UserPrincipal){
            UserPrincipal userPrincipal = (UserPrincipal) event.getAuthentication().getPrincipal();
            loginAttemptService.evictUserFromLoginAttemptCache(userPrincipal.getUsername());
        }
    }
}
