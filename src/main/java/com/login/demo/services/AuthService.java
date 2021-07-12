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
import com.login.demo.models.EmailVerificationToken;
import com.login.demo.models.User;
import com.login.demo.services.security.JwtTokenProvider;


@Service
public class AuthService {
	
	  //private static final Logger logger = Logger.getLogger(AuthService.class);
	    private final UserService userService;
	    private final PasswordEncoder passwordEncoder;
	    private final AuthenticationManager authenticationManager;
	    private final JwtTokenProvider tokenProvider;
	    private final EmailVerificationTokenService emailVerificationTokenService;


	   

		
		@Autowired
	    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
				AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
				EmailVerificationTokenService emailVerificationTokenService) {
			super();
			this.userService = userService;
			this.passwordEncoder = passwordEncoder;
			this.authenticationManager = authenticationManager;
			this.tokenProvider = tokenProvider;
			this.emailVerificationTokenService = emailVerificationTokenService;
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
	    
	    
	   
	

	    /**
	     * Confirms the user verification based on the token expiry and mark the user as active.
	     * If user is already verified, save the unnecessary database calls.
	     */
	    public Optional<User> confirmEmailRegistration(String emailToken) {
	        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(emailToken)
	                .orElseThrow(() -> new ResourceNotFoundException("Token", "Email verification", emailToken));

	        User registeredUser = emailVerificationToken.getUser();
	        if (registeredUser.getEmailVerified()) {
	            return Optional.of(registeredUser);
	        }

	        emailVerificationTokenService.verifyExpiration(emailVerificationToken);
	        emailVerificationToken.setConfirmedStatus();
	        emailVerificationTokenService.save(emailVerificationToken);

	        registeredUser.markVerificationConfirmed();
	        userService.save(registeredUser);
	        return Optional.of(registeredUser);
	    }
	    
	    /**
	     * Attempt to regenerate a new email verification token given a valid
	     * previous expired token. If the previous token is valid, increase its expiry
	     * else update the token value and add a new expiration.
	     */
	    public Optional<EmailVerificationToken> recreateRegistrationToken(String existingToken) {
	        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByToken(existingToken)
	                .orElseThrow(() -> new ResourceNotFoundException("Token", "Existing email verification", existingToken));

	        if (emailVerificationToken.getUser().getEmailVerified()) {
	            return Optional.empty();
	        }
	        return Optional.ofNullable(emailVerificationTokenService.updateExistingTokenWithNameAndExpiry(emailVerificationToken));
	    }
	    
	    
	    public Optional<String> getTokenByEmail(String email) {
	    	EmailVerificationToken emailVerificationToken = emailVerificationTokenService.getTokenByEmail(email).get();
	    	return Optional.ofNullable(emailVerificationToken.getToken());
			
		}

	    
	    
	    
	

}
