package com.login.demo.controllers;

import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.login.demo.dto.UserDto;
import com.login.demo.exceptions.HandleExceptionC;
import com.login.demo.models.User;
import com.login.demo.services.AuthService;
import com.login.demo.services.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	
    private final AuthService authService;

    private final UserService userService;


    @Autowired
    public UserController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
        
    }
    
    
    /**
     * Returns all admins in the system. Requires Admin access
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity getAllAdmins() {
        //logger.info("Inside secured resource with admin");
        return ResponseEntity.ok("Hello. This is about admins");
    }
    

	


}
