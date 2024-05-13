package com.supportportalMehdi.demo.resource;


import com.supportportalMehdi.demo.domain.HttpResponse;
import com.supportportalMehdi.demo.domain.User;
import com.supportportalMehdi.demo.domain.UserPrincipal;
import com.supportportalMehdi.demo.exception.domain.*;
import com.supportportalMehdi.demo.repository.UserRepository;
import com.supportportalMehdi.demo.service.UserService;
import com.supportportalMehdi.demo.utility.JWTTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.supportportalMehdi.demo.constant.FileConstant.*;
import static com.supportportalMehdi.demo.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@RestController
@RequestMapping(value = {"/","/user"})
public class UserResource extends ExceptionHandling {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);
    public static final String EMAIL_SENT = "An email with a new password was sent to: ";
    public static final String USER_DELETED_SECCESSFULY = "User Deleted Seccessfuly";
    private UserService userService ;
    private AuthenticationManager authenticationManager ;
    private JWTTokenProvider jwtTokenProvider ;
    private UserRepository userRepository ;

    @Autowired
    public UserResource(UserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider,UserRepository userRepository ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {
        // if there is a probeleme im gonna throw an exception inside authenticate he will never pass to the next level
        LOGGER.info("Login attempt for user: {}", user.getUsername());
        authenticate(user.getUsername(),user.getPassword());
        User loginUser = userService.findUserByUsername(user.getUsername()) ;
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return new ResponseEntity<>(loginUser,jwtHeader, OK) ;
    }
    @CrossOrigin
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException {
        System.out.println("register path me!!!! "+user.getPassword());
        User newUser = userService.register(user.getFirstName(),user.getLastName(), user.getUsername(), user.getEmail(),user.getPhoneNumber(),user.getGender(),user.getPassword());
        System.out.println(user.getPhoneNumber());
        System.out.println(user.getGender());

        return new ResponseEntity(newUser, OK) ;
    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser (@RequestParam("firstName") String firstName,
                                            @RequestParam("lastName") String lastName,
                                            @RequestParam("username") String username,
                                            @RequestParam("email") String email,
                                            @RequestParam("phoneNumber") String phoneNumber,
                                            @RequestParam("gender") String gender,
                                            @RequestParam("role") String role,
                                            @RequestParam("isActive") String isActive,
                                            @RequestParam("isNonLocked") String isNonLocked,
                                            @RequestParam(value = "profileImage",required = false) MultipartFile profileImage) throws EmailExistException, IOException, UsernameExistException, NotAnImageFileException {
        User newUser = userService.addNewUser(firstName,lastName,username,email,phoneNumber,gender,role,Boolean.parseBoolean(isNonLocked),Boolean.parseBoolean(isActive),profileImage);
        return new ResponseEntity<>(newUser,OK);
    }
    @PostMapping("/update")
    public ResponseEntity<User> updateNewUser (@RequestParam("currentUsername") String currentUsername,
                                            @RequestParam("firstName") String firstName,
                                            @RequestParam("lastName") String lastName,
                                            @RequestParam("username") String username,
                                            @RequestParam("email") String email,
                                            @RequestParam("phoneNumber") String phoneNumber,
                                            @RequestParam("gender") String gender,
                                            @RequestParam("role") String role,
                                            @RequestParam("isActive") String isActive,
                                            @RequestParam("isNonLocked") String isNonLocked,
                                            @RequestParam(value = "profileImage",required = false) MultipartFile profileImage) throws EmailExistException, IOException, UsernameExistException, NotAnImageFileException {
        User updateUser = userService.updateUser(currentUsername,firstName,lastName,username,email,phoneNumber,gender,role,Boolean.parseBoolean(isNonLocked),Boolean.parseBoolean(isActive),profileImage);
        return new ResponseEntity<>(updateUser,OK);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getuser(@PathVariable("username")String username){
        User user = userService.findUserByUsername(username);
        return new ResponseEntity<>(user,OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getUsers() ;
        return new ResponseEntity<>(users,OK);
    }

    //TODO WE ARE HERE !!!!!!!!
    @GetMapping("/page/list")
    public Page<User> showPagesUsers(@RequestParam(defaultValue = "0") int page){
        return userRepository.findAll(PageRequest.of(page, 10));
    }

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws EmailNotFoundException, MessagingException {
        userService.resetPassword(email);
        return response(OK, EMAIL_SENT +email);
    }

    @DeleteMapping("/delete/{username}")
    //@PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity <HttpResponse> deleteUser(@PathVariable("username")String username) throws IOException {
        userService.deleteUser(username) ;
        return response(OK , USER_DELETED_SECCESSFULY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage (@RequestParam("username") String username,@RequestParam(value = "profileImage") MultipartFile profileImage) throws EmailExistException, IOException, UsernameExistException, NotAnImageFileException {
        User user = userService.updateProfileImage(username,profileImage) ;
        return new ResponseEntity<>(user,OK);
    }

    @GetMapping(path = "/image/{username}/{fileName}",produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username")String username,@PathVariable("fileName")String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER+username+FORWARD_SLASH+fileName)) ;
        //"user.home" +"/supportportal/user/rick/rick.jpg" ==>this is an example of the url
    }

    @GetMapping(path = "/image/profile/{username}",produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username")String username) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL+username) ;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(InputStream inputStream = url.openStream()){
            int bytesRead ;
            byte[] chunk = new byte[1024] ;
            while ((bytesRead = inputStream.read(chunk))>0){
                byteArrayOutputStream.write(chunk,0,bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray() ;
    }




    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(),httpStatus,httpStatus.getReasonPhrase().toUpperCase(),
                message.toUpperCase()),httpStatus) ;
    }

    private HttpHeaders getJwtHeader(UserPrincipal user) {
        HttpHeaders headers = new HttpHeaders() ;
        headers.add(JWT_TOKEN_HEADER,jwtTokenProvider.generateJwtToken(user));
        return headers ;
    }

    private void authenticate(String username, String password) {
//        If the authentication is not successful, the authenticate method of the authenticationManager will throw an exception.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
    }
}
