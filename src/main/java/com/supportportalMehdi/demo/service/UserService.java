package com.supportportalMehdi.demo.service;

import com.supportportalMehdi.demo.domain.User;
import com.supportportalMehdi.demo.exception.domain.EmailExistException;
import com.supportportalMehdi.demo.exception.domain.EmailNotFoundException;
import com.supportportalMehdi.demo.exception.domain.NotAnImageFileException;
import com.supportportalMehdi.demo.exception.domain.UsernameExistException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface UserService {
    User register(String firstName, String lastName, String username, String email, String phoneNumber, String gender) throws EmailExistException, UsernameExistException, MessagingException;
    List<User> getUsers() ;
    User findUserByUsername(String username) ;
    User findUserByEmail(String email) ;
    ///TODO case if i want to add a user when im inside the applications !
    User addNewUser(String firstName , String lasName, String username, String email ,String phoneNumber,String gender, String role , boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException, NotAnImageFileException;
    User updateUser(String currentUsername,String newFirstName , String newLasName, String newUsername, String newEmail ,String phoneNumber,String gender, String role , boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException, NotAnImageFileException;
    void deleteUser(String username) throws IOException;
    void resetPassword(String email) throws EmailNotFoundException, MessagingException;
    User updateProfileImage(String username,MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException, NotAnImageFileException;
}
