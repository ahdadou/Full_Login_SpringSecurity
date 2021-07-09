package com.login.demo.models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
public class Role {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@Enumerated(EnumType.STRING)
	@NaturalId
	private RoleName roleName;
	
	
	
	
	
	
	public Role() {
		super();
	}
	
	public Role(long id, RoleName roleName) {
		super();
		this.id = id;
		this.roleName = roleName;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public RoleName getRoleName() {
		return roleName;
	}
	public void setRoleName(RoleName roleName) {
		this.roleName = roleName;
	}

	
	

}
