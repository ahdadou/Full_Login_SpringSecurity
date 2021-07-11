package com.login.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;


import com.login.demo.dto.LoginDto;
import com.login.demo.dto.RegisterDto;

import com.login.demo.exceptions.ResourceAlreadyInUseException;
import com.login.demo.exceptions.ResourceNotFoundException;
import com.login.demo.models.CustomUserDetails;
import com.login.demo.models.User;
import com.login.demo.services.security.JwtTokenProvider;


@Service
public class AuthService {
	
	  //private static final Logger logger = Logger.getLogger(AuthService.class);
	    private final UserService userService;
	    private final PasswordEncoder passwordEncoder;
	    private final AuthenticationManager authenticationManager;
	    private final JwtTokenProvider tokenProvider;


	   

		
		
	    @Autowired
	    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
				AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider
				) {
			this.userService = userService;
			this.passwordEncoder = passwordEncoder;
			this.authenticationManager = authenticationManager;
			this.tokenProvider = tokenProvider;
		}

		/**
	     * Registers a new user in the database by performing a series of quick checks.
	     *
	     * @return A user object if successfully created
	     */
	    public Optional<User> registerUser(RegisterDto newRegistrationRequest) {
	        String newRegistrationRequestEmail = newRegistrationRequest.getEmail();
	        if (emailAlreadyExists(newRegistrationRequestEmail)) {
	           // logger.error("Email already exists: " + newRegistrationRequestEmail);
	            throw new ResourceAlreadyInUseException("Email", "Address", newRegistrationRequestEmail);
	        }
	       // logger.info("Trying to register new user [" + newRegistrationRequestEmail + "]");
	        User newUser = userService.createUser(newRegistrationRequest);
	        User registeredNewUser = userService.save(newUser);
	        return Optional.ofNullable(registeredNewUser);
	    }
	    
	    /**
	     * Checks if the given email already exists in the database repository or not
	     *
	     * @return true if the email exists else false
	     */
	    public Boolean emailAlreadyExists(String email) {
	        return userService.existsByEmail(email);
	    }

	    /**
	     * Checks if the given email already exists in the database repository or not
	     *
	     * @return true if the email exists else false
	     */
	    public Boolean usernameAlreadyExists(String username) {
	        return userService.existsByUsername(username);
	    }

	    /**
	     * Authenticate user and log them in given a loginRequest
	     */
	    public Optional<Authentication> authenticateUser(LoginDto loginRequest) {
	    	System.out.println("----------------------00011");
	    	System.out.println(loginRequest.getEmail()+"   "+loginRequest.getPassword());
	        return Optional.ofNullable(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
	                loginRequest.getPassword())));
	    }
	   
	    
	    
	    
	    /**
	     * Generates a JWT token for the validated client
	     */
	    public String generateToken(CustomUserDetails customUserDetails) {
	        return tokenProvider.generateToken(customUserDetails);
	    }

	    /**
	     * Generates a JWT token for the validated client by userId
	     */
	    private String generateTokenFromUserId(Long userId) {
	        return tokenProvider.generateTokenFromUserId(userId);
	    }
	    
	    
	   
	

	    
	    
	    
	

}
