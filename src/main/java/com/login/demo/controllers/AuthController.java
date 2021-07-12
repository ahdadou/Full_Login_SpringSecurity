package com.login.demo.controllers;


import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import com.login.demo.dto.LoginDto;
import com.login.demo.dto.RegisterDto;
import com.login.demo.event.OnUserRegistrationCompleteEvent;
import com.login.demo.exceptions.InvalidTokenRequestException;
import com.login.demo.exceptions.UserLoginException;
import com.login.demo.exceptions.UserRegistrationException;
import com.login.demo.models.CustomUserDetails;
import com.login.demo.models.payload.ApiResponse;
import com.login.demo.models.payload.JwtAuthenticationResponse;
import com.login.demo.services.AuthService;
import com.login.demo.services.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	
    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;
    private final ApplicationEventPublisher applicationEventPublisher;



    @Autowired
    public AuthController(AuthService authService, JwtTokenProvider tokenProvider,ApplicationEventPublisher applicationEventPublisher) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
        this.applicationEventPublisher=applicationEventPublisher;
    }
    
    
    /**
     * Checks is a given email is in use or not.
     */
    @GetMapping("/checkEmailInUse")
    public ResponseEntity checkEmailInUse(@RequestParam("email") String email) {
        Boolean emailExists = authService.emailAlreadyExists(email);
        return ResponseEntity.ok(new ApiResponse(true, emailExists.toString()));
    }
    
    
    /**
     * Checks is a given username is in use or not.
     */
    @GetMapping("/checkUsernameInUse")
    public ResponseEntity checkUsernameInUse( @RequestParam("username") String username) {
        Boolean usernameExists = authService.usernameAlreadyExists(username);
        return ResponseEntity.ok(new ApiResponse(true, usernameExists.toString()));
    }
    
    
    /**
     * Entry point for the user log in. Return the jwt auth token and the refresh token
     */
    @PostMapping("/login")
    public ResponseEntity authenticateUser(@RequestBody LoginDto loginRequest) {

        Authentication authentication = authService.authenticateUser(loginRequest)
                .orElseThrow(() -> new UserLoginException("Couldn't login user [" + loginRequest + "]"));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwtToken = authService.generateToken(customUserDetails);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwtToken, "", tokenProvider.getExpiryDuration()));
        		
        		
    }

    
    /**
     * Entry point for the user registration process. On successful registration,
     * publish an event to generate email verification token
     */
    @PostMapping("/register")
    public ResponseEntity registerUser(@Valid @RequestBody RegisterDto registrationRequest) {
        return authService.registerUser(registrationRequest)
                .map(user -> {
                	UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/auth/registrationConfirmation");
                    System.out.println(urlBuilder);
                	OnUserRegistrationCompleteEvent onUserRegistrationCompleteEvent = new OnUserRegistrationCompleteEvent(user, urlBuilder);
                    applicationEventPublisher.publishEvent(onUserRegistrationCompleteEvent);
                    System.out.println("-********* From registerUser authcontroller --*********");
                    System.out.println(onUserRegistrationCompleteEvent);

                    return ResponseEntity.ok(new ApiResponse(true, "User registered successfully. Check your email for verification"));
                })
                .orElseThrow(() -> new UserRegistrationException(registrationRequest.getEmail(), "Missing user object in database"));
    	}

    
    /**
     * Confirm the email verification token generated for the user during
     * registration. If token is invalid or token is expired, report error.
     */
    @GetMapping("/registrationConfirmation")
    public ResponseEntity confirmRegistration(@RequestParam("token") String token) {
    	System.out.println("-********* From registrationConfirmation authcontroller --*********");
        return authService.confirmEmailRegistration(token)
                .map(user -> 
                ResponseEntity.ok(new ApiResponse(true, "User verified successfully")))
                .orElseThrow(() -> new InvalidTokenRequestException("Email Verification Token", token, "Failed to confirm. Please generate a new email verification request"));
    }
    
    
    
    
}
