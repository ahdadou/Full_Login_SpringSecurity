package com.login.demo.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.login.demo.dao.RoleRepository;
import com.login.demo.dao.UserRepository;
import com.login.demo.dto.UserDto;
import com.login.demo.models.Role;
import com.login.demo.models.RoleName;
import com.login.demo.models.User;

@Service
public class UserService {
	
	private UserRepository userRepository;
	private RoleRepository roleRepository;

	@Autowired
	public UserService(UserRepository userRepository,RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository= roleRepository;
	}
	
	
	public User save(UserDto user) {
		
		if(userRepository.existsByEmail(user.getEmail())) {
			throw new RuntimeException("User Exist");
		}
		
		if(userRepository.existsByUsername(user.getUsername())) {
			throw new RuntimeException("User Exist");
		
		}
		
		
		User u = new User();
		u.setUsername(user.getUsername());
		u.setEmail(user.getEmail());
		u.setPassword(user.getPassword());
		
		System.out.println(u.getRoles());
		ArrayList<Role> roles = new ArrayList<>();
		
		user.getRole().forEach(role -> {
			Role r = null;
			switch (role) {
			
			case "ADMIN":
				 r = roleRepository.findByRoleName(RoleName.ADMIN)
						.orElseThrow(()-> new RuntimeException("This Role Not Found"));
				roles.add(r);
				break;

			default:
				 r = roleRepository.findByRoleName(RoleName.USER)
				.orElseThrow(()-> new RuntimeException("This Role Not Found"));
				roles.add(r);
				break;
			}
		});
		
		u.setRoles(roles);
		
		
		return userRepository.save(u);
	}
	
	public void delete(long id) {
		userRepository.deleteById(id);
	}
	
	public User getOne(long id) throws Exception {
		return userRepository.findById(id).orElseThrow(()->new  Exception("User Not Found"));
	}
	
	public List<User> getAll() {
		return userRepository.findAll();
	}
	
	

}
