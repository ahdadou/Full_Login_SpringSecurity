package com.login.demo.services;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;


import com.login.demo.dto.LoginDto;
import com.login.demo.dto.RegisterDto;
import com.login.demo.exceptions.PasswordResetLinkException;
import com.login.demo.exceptions.ResourceAlreadyInUseException;
import com.login.demo.exceptions.ResourceNotFoundException;
import com.login.demo.exceptions.TokenRefreshException;
import com.login.demo.exceptions.UpdatePasswordException;
import com.login.demo.models.CustomUserDetails;
import com.login.demo.models.EmailVerificationToken;
import com.login.demo.models.PasswordResetToken;
import com.login.demo.models.RefreshToken;
import com.login.demo.models.User;
import com.login.demo.models.payload.PasswordResetLinkRequest;
import com.login.demo.models.payload.PasswordResetRequest;
import com.login.demo.models.payload.TokenRefreshRequest;
import com.login.demo.models.payload.UpdatePasswordRequest;
import com.login.demo.services.security.JwtTokenProvider;


@Service
public class AuthService {
	
	  //private static final Logger logger = Logger.getLogger(AuthService.class);
	    private final UserService userService;
	    private final PasswordEncoder passwordEncoder;
	    private final AuthenticationManager authenticationManager;
	    private final JwtTokenProvider tokenProvider;
	    private final EmailVerificationTokenService emailVerificationTokenService;
	    private final PasswordResetTokenService passwordResetTokenService;
	    private final RefreshTokenService refreshTokenService;


	    
	    @Autowired
		public AuthService(UserService userService, PasswordEncoder passwordEncoder,
				AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
				EmailVerificationTokenService emailVerificationTokenService,
				PasswordResetTokenService passwordResetTokenService, RefreshTokenService refreshTokenService) {
			this.userService = userService;
			this.passwordEncoder = passwordEncoder;
			this.authenticationManager = authenticationManager;
			this.tokenProvider = tokenProvider;
			this.emailVerificationTokenService = emailVerificationTokenService;
			this.passwordResetTokenService = passwordResetTokenService;
			this.refreshTokenService = refreshTokenService;
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
	    
	    
	    /**
	     * Validates the password of the current logged in user with the given password
	     */
	    private Boolean currentPasswordMatches(User currentUser, String password) {
	        return passwordEncoder.matches(password, currentUser.getPassword());
	    }
	    
	    
	    /**
	     * Updates the password of the current logged in user
	     */
	    public Optional<User> updatePassword(CustomUserDetails customUserDetails,
	                                         UpdatePasswordRequest updatePasswordRequest) {
	        String email = customUserDetails.getEmail();
	        User currentUser = userService.findByEmail(email)
	                .orElseThrow(() -> new UpdatePasswordException(email, "No matching user found"));

	        if (!currentPasswordMatches(currentUser, updatePasswordRequest.getOldPassword())) {
	            throw new UpdatePasswordException(currentUser.getEmail(), "Invalid current password");
	        }
	        String newPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
	        currentUser.setPassword(newPassword);
	        userService.save(currentUser);
	        return Optional.of(currentUser);
	    }
	    

	    
	    
	    /**
	     * Generates a password reset token from the given reset request
	     */
	    public Optional<PasswordResetToken> generatePasswordResetToken(PasswordResetLinkRequest passwordResetLinkRequest) {
	        String email = passwordResetLinkRequest.getEmail();
	        return userService.findByEmail(email)
	                .map(user -> {
	                    PasswordResetToken passwordResetToken = passwordResetTokenService.createToken();
	                    passwordResetToken.setUser(user);
	                    passwordResetTokenService.save(passwordResetToken);
	                    return Optional.of(passwordResetToken);
	                })
	                .orElseThrow(() -> new PasswordResetLinkException(email, "No matching user found for the given request"));
	    }

	    /**
	     * Reset a password given a reset request and return the updated user
	     */
	    public Optional<User> resetPassword(PasswordResetRequest passwordResetRequest) {
	        String token = passwordResetRequest.getToken();
	        PasswordResetToken passwordResetToken = passwordResetTokenService.findByToken(token)
	                .orElseThrow(() -> new ResourceNotFoundException("Password Reset Token", "Token Id", token));

	        passwordResetTokenService.verifyExpiration(passwordResetToken);
	        final String encodedPassword = passwordEncoder.encode(passwordResetRequest.getPassword());

	        return Optional.of(passwordResetToken)
	                .map(PasswordResetToken::getUser)
	                .map(user -> {
	                    user.setPassword(encodedPassword);
	                    userService.save(user);
	                    return user;
	                });
	    }


	    
	    /**
	     * Refresh the expired jwt token using a refresh token and device info. The
	     * * refresh token is mapped to a specific device and if it is unexpired, can help
	     * * generate a new jwt. If the refresh token is inactive for a device or it is expired,
	     * * throw appropriate errors.
	     */
	    public Optional<String> refreshJwtToken(TokenRefreshRequest tokenRefreshRequest) {
	        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

	        return Optional.of(refreshTokenService.findByToken(requestRefreshToken)
	                .map(refreshToken -> {
	                    refreshTokenService.verifyExpiration(refreshToken);
//	                    userDeviceService.verifyRefreshAvailability(refreshToken);
	                    refreshTokenService.increaseCount(refreshToken);
	                    return refreshToken;
	                })
	                .map(RefreshToken::getUser)          
//	                .map(UserDevice::getUser)
	                .map(User::getId).map(this::generateTokenFromUserId))
	                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Missing refresh token in database.Please login again"));
	    }
	    
	    
	    
	    
	    /**
	     * Creates and persists the refresh token for the user device. If device exists
	     * already, we don't care. Unused devices with expired tokens should be cleaned
	     * with a cron job. The generated token would be encapsulated within the jwt.
	     * Remove the existing refresh token as the old one should not remain valid.
	     */
	    public Optional<RefreshToken> createAndPersistRefreshTokenForDevice(Authentication authentication, LoginDto loginRequest) {
	        User currentUser = (User) authentication.getPrincipal();
	        RefreshToken refreshToken = refreshTokenService.createRefreshToken();	
	        refreshToken.setUser(currentUser);
	        refreshToken = refreshTokenService.save(refreshToken);
	        return Optional.ofNullable(refreshToken);
	    }

}
