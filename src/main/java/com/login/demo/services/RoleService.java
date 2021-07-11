package com.login.demo.services;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.login.demo.models.Role;
import com.login.demo.repository.RoleRepository;

@Service
public class RoleService {
	
	  private final RoleRepository roleRepository;

	    @Autowired
	    public RoleService(RoleRepository roleRepository) {
	        this.roleRepository = roleRepository;
	    }

	    /**
	     * Find all roles from the database
	     */
	    public Collection<Role> findAll() {
	        return roleRepository.findAll();
	    }

}
