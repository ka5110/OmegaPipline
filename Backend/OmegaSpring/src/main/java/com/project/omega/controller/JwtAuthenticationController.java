package com.project.omega.controller;

import com.project.omega.bean.dao.auth.JwtRequest;
import com.project.omega.bean.dao.auth.JwtResponse;
import com.project.omega.bean.dao.auth.Privilege;
import com.project.omega.bean.dao.auth.Token;
import com.project.omega.bean.dao.entity.User;
import com.project.omega.bean.dto.PasswordDTO;
import com.project.omega.bean.dto.UserDTO;
import com.project.omega.exceptions.DuplicateUserException;
import com.project.omega.exceptions.InvalidOldPasswordException;
import com.project.omega.helper.GenericResponse;
import com.project.omega.service.JwtUserDetailsService;
import com.project.omega.service.interfaces.AuthenticationService;
import com.project.omega.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
public class JwtAuthenticationController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messages;

    @PostMapping(value = "/api/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        LOGGER.debug("Authentication for JwtRequest: {}", authenticationRequest);
        authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
        return ResponseEntity.ok(new JwtResponse(authenticationService.createJWTToken(authenticationRequest)));
    }

    @PostMapping(value = "/api/registration", headers = "Accept=application/json")
    public ResponseEntity createUser(@RequestBody UserDTO user) throws DuplicateUserException, Exception {
        LOGGER.debug("User Registration Process: {}", user);
        /*CALLS THE authenticate() method in the same class */
        authenticate(user.getEmail(), user.getPassword());

        /*Binds the Information sent via UserDTO with the JwtRequestBuilder.*/
        JwtRequest jwtRequest = new JwtRequest.JwtRequestBuilder()
                .setEmail(user.getEmail())
                .setPassword(user.getPassword())
                .build();

        /*Pass the JwtRequest bean into the authentication service  and create the JWT token.*/
        String jwtToken = authenticationService.createJWTToken(jwtRequest);

        /*The JWT token along with the user details is saved onto the Token entity */
        userService.createVerificationTokenForUser(jwtToken, user);

        /*Pass the user detail into userDetailService and create User*/
        User newUser = userDetailsService.createUser(user);

        return new ResponseEntity(newUser, HttpStatus.CREATED);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/confirmRegistration")
    public ResponseEntity confirmRegistration(@RequestParam("token") final String token) {
        final String information = userService.validateVerificationToken(token);
        if (information.equals("valid")) {
            final User user = userService.getUser(token);
            authenticationWithoutPassword(user);
            return new ResponseEntity(
                    new GenericResponse(messages.getMessage("message.accountVerified", null, null)),
                    HttpStatus.OK);
        }
        Properties properties = new Properties();
        properties.setProperty("message", messages.getMessage("auth.message." + information, null, null));
        properties.setProperty("expired", String.valueOf("expired".equals(information)));
        properties.setProperty("token", token);
        return new ResponseEntity(new GenericResponse(properties), HttpStatus.NOT_FOUND);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("/resendRegistrationToken")
    public ResponseEntity resendRegistrationToken(@RequestParam("token") final String existingToken) {
        Token newVerificationToken = userService.generateNewVerificationToken(existingToken);
        final User user = userService.getUser(newVerificationToken.getToken());
        Properties properties = new Properties();
        properties.setProperty("message", messages.getMessage("message.resendToken", null, null));
        properties.setProperty("newToken", newVerificationToken.getToken());
        return new ResponseEntity(new GenericResponse(properties), HttpStatus.OK);
    }

    /*To be Used When the User DOES NOT remember their password*/
    @SuppressWarnings("unchecked")
    @PostMapping(value = "/resetPassword", headers = "Accept=application/json")
    public ResponseEntity resetUserPassword(@RequestParam("email") final String userEmail) {
        final User user = userService.findUserByEmail(userEmail);
        Properties properties = null;
        if (user != null) {
            final String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            properties.setProperty("message", messages.getMessage("message.resetPasswordEmail", null, null));
            properties.setProperty("passwordResetToken", token);
            properties.setProperty("tokenType", "UUID");
        }
        return new ResponseEntity(new GenericResponse(properties), HttpStatus.CREATED);
    }

    /*To be User When User KNOWS his password*/
    @PostMapping(value = "/updatePassword", headers = "Accept=application/json")
    @SuppressWarnings("unchecked")
    public ResponseEntity changeUserPassword(@Valid PasswordDTO passwordDto) {
        final User user = userService.findUserByEmail(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
        if (!userService.checkIfOldPasswordIsValid(user, passwordDto.getOldPassword())) {
            throw new InvalidOldPasswordException();
        }
        userService.changeUserPassword(user, passwordDto.getNewPassword());
        Properties properties = new Properties();
        properties.setProperty("message", messages.getMessage("message.updatePasswordSuc", null, null));
        properties.setProperty("oldPassword", user.getPassword());
        properties.setProperty("changedPassword", passwordDto.getNewPassword());
        return new ResponseEntity(new GenericResponse(messages.getMessage("message.updatePasswordSuc", null, null)), HttpStatus.CREATED);
    }

    @GetMapping("/changePassword")
    public String showChangePasswordPage(@RequestParam("id") final long id, @RequestParam("token") final String token) {
        final String result = userService.validatePasswordResetToken(id, token);
        if (result != null) {
            /**/
        }
        return "";
    }


    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    public void authenticationWithoutPassword(User user) {
        List<Privilege> privileges = user.getRoles().stream().map(role -> role.getPrivileges()).flatMap(list -> list.stream()).distinct().collect(Collectors.toList());
        List<GrantedAuthority> authorities = privileges.stream().map(p -> new SimpleGrantedAuthority(p.getName())).collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}