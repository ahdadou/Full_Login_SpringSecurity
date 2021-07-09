package com.login.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.login.demo.models.Role;
import com.login.demo.models.RoleName;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

	Optional<Role> findByRoleName(RoleName roleName);

}
