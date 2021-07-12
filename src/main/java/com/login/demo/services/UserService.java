package com.login.demo.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.login.demo.dto.RegisterDto;
import com.login.demo.dto.UserDto;
import com.login.demo.models.Role;
import com.login.demo.models.User;
import com.login.demo.repository.RoleRepository;
import com.login.demo.repository.UserRepository;


@Service
public class UserService {
	
   // private static final Logger logger = Logger.getLogger(UserService.class);
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleService roleService;

	@Autowired
	public UserService(PasswordEncoder passwordEncoder,UserRepository userRepository, RoleService roleService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleService = roleService;
	}
	
	
	 /**
     * Finds a user in the database by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user in the database by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find a user in db by id.
     */
    public Optional<User> findById(Long Id) {
        return userRepository.findById(Id);
    }

    /**
     * Save the user to the database
     */
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Check is the user exists given the email: naturalId
     */
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check is the user exists given the username: naturalId
     */
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }


    /**
     * Creates a new user from the registration request
     */
    public User createUser(RegisterDto registerRequest) {
    	  User newUser = new User();
          Boolean isNewUserAsAdmin = registerRequest.getRegisterAsAdmin();
          newUser.setEmail(registerRequest.getEmail());
          newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
          newUser.setUsername(registerRequest.getEmail());
          newUser.addRoles(getRolesForNewUser(isNewUserAsAdmin));
          newUser.setActive(true);
          newUser.setEmailVerified(false);
          return newUser;
    }

    /**
     * Performs a quick check to see what roles the new user could be assigned to.
     *
     * @return list of roles for the new user
     */
    private Set<Role> getRolesForNewUser(Boolean isToBeMadeAdmin) {
        Set<Role> newUserRoles = new HashSet<>(roleService.findAll());
        if (!isToBeMadeAdmin) {
            newUserRoles.removeIf(Role::isAdminRole);
        }
        // logger.info("Setting user roles: " + newUserRoles);
        return newUserRoles;
    }


	
	

}
