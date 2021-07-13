package com.login.demo.models;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.NaturalId;

import com.login.demo.models.audit.DateAudit;

import java.time.Instant;

@Entity(name = "REFRESH_TOKEN")
public class RefreshToken extends DateAudit {

    @Id
    @Column(name = "TOKEN_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_seq")
    @SequenceGenerator(name = "refresh_token_seq", allocationSize = 1)
    private Long id;

    @Column(name = "TOKEN", nullable = false, unique = true)
    @NaturalId(mutable = true)
    private String token;

    @Column(name = "REFRESH_COUNT")
    private Long refreshCount;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Column(name = "EXPIRY_DT", nullable = false)
    private Instant expiryDate;

    public RefreshToken() {
    }

    public RefreshToken(Long id, String token, Long refreshCount, Instant expiryDate) {
        this.id = id;
        this.token = token;
        this.refreshCount = refreshCount;
        this.expiryDate = expiryDate;
    }

    
    public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void incrementRefreshCount() {
        refreshCount = refreshCount + 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Long getRefreshCount() {
        return refreshCount;
    }

    public void setRefreshCount(Long refreshCount) {
        this.refreshCount = refreshCount;
    }
}

