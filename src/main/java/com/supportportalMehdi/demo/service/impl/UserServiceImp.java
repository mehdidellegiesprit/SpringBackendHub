package com.supportportalMehdi.demo.service.impl;

import com.supportportalMehdi.demo.domain.User;
import com.supportportalMehdi.demo.domain.UserPrincipal;
import com.supportportalMehdi.demo.enumeration.Role;
import com.supportportalMehdi.demo.exception.domain.EmailExistException;
import com.supportportalMehdi.demo.exception.domain.EmailNotFoundException;
import com.supportportalMehdi.demo.exception.domain.NotAnImageFileException;
import com.supportportalMehdi.demo.exception.domain.UsernameExistException;
import com.supportportalMehdi.demo.repository.UserRepository;
import com.supportportalMehdi.demo.service.EmailService;
import com.supportportalMehdi.demo.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.persistence.AssociationOverride;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.supportportalMehdi.demo.constant.FileConstant.*;
import static com.supportportalMehdi.demo.constant.UserImplConstant.*;
import static com.supportportalMehdi.demo.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.MediaType.*;

@Service
@Transactional
@Qualifier("UserDetailsService")
public class UserServiceImp implements UserService, UserDetailsService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass()) ;
    private UserRepository userRepository ;
    private BCryptPasswordEncoder passwordEncoder ;

    private LoginAttemptService loginAttemptService ;
    private EmailService emailService ;

    @Autowired
    public UserServiceImp(UserRepository userRepository,BCryptPasswordEncoder passwordEncoder,LoginAttemptService loginAttemptService,EmailService emailService ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder ;
        this.loginAttemptService = loginAttemptService ;
        this.emailService = emailService ;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException  {
        User user = userRepository.findUserByUsername(username) ;
        if (user == null){
            LOGGER.error(NO_USER_FOUND_BY_USERNAME+username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME+username) ;
        }else{
            validateLoginAttempt(user) ;
            user.setLastLoginDateDisplay(user.getLastLoginDate()); //TODO e5er mara deja d5al feha
            user.setLastLoginDate(new Date()); // TODO l marra hethi eli d5al feha bech nejem nafishiha l marra jeya
            userRepository.save(user) ;
            UserPrincipal userPrincipal = new UserPrincipal(user) ;
            LOGGER.info(FOUND_USER_BY_USERNAME +username);
            return userPrincipal;
        }
    }

    private void validateLoginAttempt(User user)  {
        if (user.isNotLocked()){
            if (loginAttemptService.hasExceedMaxAttempts(user.getUsername())){
                user.setNotLocked(false);///it's locked now !  TRUE
            }else{
                user.setNotLocked(true);///it's NOT locked now
            }
        }else{
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    @Override
    public User register(String firstName, String lastName, String username, String email,String phoneNumber, String gender) throws EmailExistException, UsernameExistException, MessagingException {
        validateNewUsernameAndEmail(StringUtils.EMPTY,username,email) ;
        User user = new User() ;
        user.setUserId(generateUserId());
        String password = generatePassword();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodePassword(password));
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        user.setPhoneNumber(phoneNumber);
        user.setGender(gender);
        userRepository.save(user);
        LOGGER.info("New user password "+password);
        emailService.sendNewPasswordEmail(firstName,password,email);
        return user;
    }

    @Override
    public User addNewUser(String firstName, String lasName, String username, String email,String phoneNumber,String gender, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException, NotAnImageFileException {
        validateNewUsernameAndEmail(StringUtils.EMPTY,username,email) ;
        User user = new User() ;
        String password = generatePassword();
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lasName);
        user.setJoinDate(new Date());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNonLocked);
        user.setRole(getRoleEnum(role).name());
        user.setAuthorities(getRoleEnum(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        user.setPhoneNumber(phoneNumber);
        user.setGender(gender);
        userRepository.save(user) ;
        saveProfileImage(user,profileImage) ;
        LOGGER.info("New user password "+password);
        return user;
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLasName, String newUsername, String newEmail,String phoneNumber,String gender, String role, boolean isNonLocked, boolean isActive, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException, NotAnImageFileException {
        System.out.println("updateUser!!!");
        System.out.println("phone number = "+phoneNumber);
        System.out.println("gender = "+gender);
        System.out.println("role = "+role);
        User currentUser = validateNewUsernameAndEmail(currentUsername,newUsername,newEmail) ;
        currentUser.setFirstName(newFirstName);
        currentUser.setLastName(newLasName);
        currentUser.setJoinDate(new Date());
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setGender(gender);
        currentUser.setActive(isActive);
        currentUser.setNotLocked(isNonLocked);
        currentUser.setRole(getRoleEnum(role).name());
        currentUser.setAuthorities(getRoleEnum(role).getAuthorities());
        userRepository.save(currentUser) ;
        saveProfileImage(currentUser,profileImage) ;
        return currentUser;
    }

    @Override
    public void deleteUser(String username) throws IOException {
        User user = userRepository.findUserByUsername(username) ;
        Path userFolder = Paths.get(USER_FOLDER+user.getUsername()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(new File(userFolder.toString()));
        userRepository.deleteById(user.getId());
    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException, MessagingException {
        User user = userRepository.findUserByEmail(email);
        if (user ==null){
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email) ;
        }
        String password = generatePassword() ;
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(user.getFirstName(),password,user.getEmail());
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) throws EmailExistException, UsernameExistException, IOException, NotAnImageFileException {
        User user = validateNewUsernameAndEmail(username,null,null) ;
        saveProfileImage(user,profileImage);
        return user;
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException, NotAnImageFileException {
        if (profileImage!=null){
            ///we must test first if the useer has an existing image profile if yes we must delete the old one and replace
            /// it by the new one . in this case the save of the images is in the System folder NOT IN DB!
            /// userFolder = user/home/supportportal/user/rick
            if (!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains((profileImage.getContentType()))){
                throw new NotAnImageFileException(profileImage.getOriginalFilename()+"is not an image file. Please upload an image!") ;
            }
            Path userFolder = Paths.get(USER_FOLDER+user.getUsername()).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)){
                Files.createDirectories(userFolder);
                LOGGER.info(DIRECTORY_CREATED+userFolder);
            }
            String fileName = profileImage.getOriginalFilename();
            // Get the last index of the dot character to find the extension
            int dotIndex = fileName.lastIndexOf('.');
            // Extract the extension from the file name
            String extension = fileName.substring(dotIndex + 1);
            if (extension.equals("")){
                extension= "jpg";
            }
            System.out.println(" !!saveProfileImage extension = "+extension);
            Files.deleteIfExists(Paths.get(userFolder+user.getUsername()+DOT+extension));
            try (InputStream inputStream = profileImage.getInputStream()) {
                Files.copy(inputStream, userFolder.resolve(user.getUsername() + DOT + extension), REPLACE_EXISTING);
            }
           // Files.copy(profileImage.getInputStream(),userFolder.resolve(user.getUsername()+DOT+extension),REPLACE_EXISTING); ligne hethi ta3mel kenet fel mochkel houwa 5dem haka !!
            if (extension.equals("")){
                extension= "jpg";
            }
            user.setProfileImageUrl(setPtofileImageUrl(user.getUsername(),extension));
            userRepository.save(user);
            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM+profileImage.getOriginalFilename());
        }
    }

    private String setPtofileImageUrl(String username,String extension) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH+username+FORWARD_SLASH
                +username+DOT+extension).toUriString();
    }

    private Role getRoleEnum(String role) {
        ///TODO if i give her as parametre role : "user" she will return ROLE_USER !!! a tester bro
        System.out.println("Role given as parametre:***"+role);
        System.out.println("Role:***"+Role.valueOf(role.toUpperCase()));
        return Role.valueOf(role.toUpperCase()) ;
    }


    private String getTemporaryProfileImageUrl(String username) {
        System.out.println("Servlet contextBuilder************* = "+ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH+username).toUriString());
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH+username).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password) ; 
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        //generate a random pass²word of 10 caracterez mais avoir une facon apres de mettre le random unique !
        return RandomStringUtils.randomNumeric(10) ;
    }

    private User validateNewUsernameAndEmail(String currentUsername , String newUsername , String newEmail) throws UsernameExistException, EmailExistException {
        User userByNewUsername = findUserByUsername(newUsername) ;
        User userByNewEmail = findUserByEmail(newEmail) ;
        if (StringUtils.isNotBlank(currentUsername)){
            User currentUser = findUserByUsername(currentUsername) ;
            if (currentUser == null){
                throw  new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME +currentUsername) ;
            }
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())){
                throw new UsernameExistException(USERNAME_ALREADY_EXISTE) ;
            }
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())){
                throw new EmailExistException(EMAIL_ALREADY_EXISTE) ;
            }
            return currentUser;
        }else {
            if (userByNewUsername!=null){
                throw new UsernameExistException(USERNAME_ALREADY_EXISTE) ;
            }
            if (userByNewEmail != null ){
                throw new EmailExistException(EMAIL_ALREADY_EXISTE) ;
            }
            return null ;
        }
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
}
