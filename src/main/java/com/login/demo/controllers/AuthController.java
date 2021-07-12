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
import com.login.demo.event.OnGenerateResetLinkEvent;
import com.login.demo.event.OnRegenerateEmailVerificationEvent;
import com.login.demo.event.OnUserAccountChangeEvent;
import com.login.demo.event.OnUserRegistrationCompleteEvent;
import com.login.demo.exceptions.InvalidTokenRequestException;
import com.login.demo.exceptions.PasswordResetException;
import com.login.demo.exceptions.PasswordResetLinkException;
import com.login.demo.exceptions.UserLoginException;
import com.login.demo.exceptions.UserRegistrationException;
import com.login.demo.models.CustomUserDetails;
import com.login.demo.models.EmailVerificationToken;
import com.login.demo.models.payload.ApiResponse;
import com.login.demo.models.payload.JwtAuthenticationResponse;
import com.login.demo.models.payload.PasswordResetLinkRequest;
import com.login.demo.models.payload.PasswordResetRequest;
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
                	OnUserRegistrationCompleteEvent onUserRegistrationCompleteEvent = new OnUserRegistrationCompleteEvent(user, urlBuilder);
                    applicationEventPublisher.publishEvent(onUserRegistrationCompleteEvent);
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
    
    /**
     * Resend the email registration mail with an updated token expiry. Safe to
     * assume that the user would always click on the last re-verification email and
     * any attempts at generating new token from past (possibly archived/deleted)
     * tokens should fail and report an exception.
     */
    @GetMapping("/resendRegistrationToken")   
    public ResponseEntity resendRegistrationToken(@RequestParam("token") String existingToken) {

        EmailVerificationToken newEmailToken = authService.recreateRegistrationToken(existingToken)
                .orElseThrow(() -> new InvalidTokenRequestException("Email Verification Token", existingToken, "User is already registered. No need to re-generate token"));

        return Optional.ofNullable(newEmailToken.getUser())
                .map(registeredUser -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/auth/registrationConfirmation");
                    OnRegenerateEmailVerificationEvent regenerateEmailVerificationEvent = new OnRegenerateEmailVerificationEvent(registeredUser, urlBuilder, newEmailToken);
                    applicationEventPublisher.publishEvent(regenerateEmailVerificationEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Email verification resent successfully"));
                })
                .orElseThrow(() -> new InvalidTokenRequestException("Email Verification Token", existingToken, "No user associated with this request. Re-verification denied"));
    }
    
    
    /**
     * Get Token By Email 
     * 
     */
    @GetMapping("/resentTokenByEmail")
    public ResponseEntity resendTokenByEmail(@RequestParam("email") String email) {
    	return ResponseEntity.ok(authService.getTokenByEmail(email));
    	
    }
    
    
    /**
     * Receives the reset link request and publishes an event to send email id containing
     * the reset link if the request is valid. In future the deeplink should open within
     * the app itself.
     */
    @PostMapping("/password/resetlink")
    public ResponseEntity resetLink(@Valid @RequestBody PasswordResetLinkRequest passwordResetLinkRequest) {

        return authService.generatePasswordResetToken(passwordResetLinkRequest)
                .map(passwordResetToken -> {
                    UriComponentsBuilder urlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath().path("/password/reset");
                    OnGenerateResetLinkEvent generateResetLinkMailEvent = new OnGenerateResetLinkEvent(passwordResetToken,
                            urlBuilder);
                    applicationEventPublisher.publishEvent(generateResetLinkMailEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Password reset link sent successfully"));
                })
                .orElseThrow(() -> new PasswordResetLinkException(passwordResetLinkRequest.getEmail(), "Couldn't create a valid token"));
    }

    /**
     * Receives a new passwordResetRequest and sends the acknowledgement after
     * changing the password to the user's mail through the event.
     */

    @PostMapping("/password/reset")
    public ResponseEntity resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) {

        return authService.resetPassword(passwordResetRequest)
                .map(changedUser -> {
                    OnUserAccountChangeEvent onPasswordChangeEvent = new OnUserAccountChangeEvent(changedUser, "Reset Password",
                            "Changed Successfully");
                    applicationEventPublisher.publishEvent(onPasswordChangeEvent);
                    return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
                })
                .orElseThrow(() -> new PasswordResetException(passwordResetRequest.getToken(), "Error in resetting password"));
    }
    
    
    
    
}
