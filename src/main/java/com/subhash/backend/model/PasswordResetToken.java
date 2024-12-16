package com.subhash.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String token;

    private Instant expiryDate;

    private boolean used;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Userr user;

    public PasswordResetToken(String token, Instant expiryDate, Userr user) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.user = user;
    }
}

