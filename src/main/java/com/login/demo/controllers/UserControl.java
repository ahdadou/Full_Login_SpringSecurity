package com.login.demo.controllers;

import org.springframework.http.MediaType;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.login.demo.dto.UserDto;
import com.login.demo.models.User;
import com.login.demo.services.UserService;

@RestController
@RequestMapping("/user")
public class UserControl {
	
	
	private UserService userService;
	
	
	@Autowired
	public UserControl(UserService userService) {
		this.userService = userService;
	}

//	@PostMapping
//	public ResponseEntity<?> saveUser(@RequestBody User user){
//		System.out.println(user);
//
//		User u = userService.save(user);
//		return ResponseEntity.ok(u);
//	}
	
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> saveUser(@RequestBody UserDto user){

		User u = userService.save(user);
		return ResponseEntity.ok(u);
	}
	
	@DeleteMapping
	public ResponseEntity<?> deleteUser(@RequestParam(name = "id") long id){
		userService.delete(id);
		return ResponseEntity.ok("User delete Successfully");
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> getOneUser(@PathVariable(name = "id") long id){
		try {
			User user = userService.getOne(id);
			return ResponseEntity.ok(user);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Exception("Cant Find This user"));
		} 
		
	}
	
	@GetMapping(name = "/all")
	public ResponseEntity<?> gyy(){
		List<User> users = userService.getAll();
		return ResponseEntity.ok(users);
	}

}
