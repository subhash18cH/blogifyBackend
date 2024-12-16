package com.subhash.backend.service;

import com.subhash.backend.model.PasswordResetToken;
import com.subhash.backend.model.Userr;
import com.subhash.backend.repository.PasswordResetTokenRepository;
import com.subhash.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Value("${frontend.url}")
    private  String frontendUrl;

    @Autowired
    EmailService emailService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    public void generatePasswordResetToken(String email) {

        Userr user =userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not found with this email"));
        String token= UUID.randomUUID().toString();
        Instant expiryDate=Instant.now().plus(24, ChronoUnit.HOURS);
        PasswordResetToken passwordResetToken=new PasswordResetToken(token,expiryDate,user);
        passwordResetTokenRepository.save(passwordResetToken);

        String resetUrl= frontendUrl+"/reset-password?token="+token;

        emailService.sendPasswordResetEmail(user.getEmail(),resetUrl);

    }

    public void resetPassword(String token, String newPass) {
        PasswordResetToken resetToken=passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()-> new RuntimeException("Invalid password reset token"));
        if(resetToken.isUsed()){
            throw new RuntimeException("Password reset token has already been used");
        }
        if(resetToken.getExpiryDate().isBefore(Instant.now())){
            throw new RuntimeException("Password reset token has expired");
        }
        Userr user =resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPass));
        userRepository.save(user);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public Userr findByUserName(String username) {
        Optional<Userr> user=userRepository.findByUserName(username);
        return user.orElseThrow(()-> new RuntimeException("User not found with Username:"+ username));
    }

    public Optional<Userr> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Userr registerUser(Userr user) {
        if(user.getPassword()!= null){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }
}

