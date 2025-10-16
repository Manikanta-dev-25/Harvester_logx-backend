package com.mani.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mani.Entity.Users;
import com.mani.respository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository ur;

    @Autowired
    private JavaMailSender mailSender;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Users RegisterUser(Users User) {
        User.setPassword(passwordEncoder.encode(User.getPassword()));
        return ur.save(User);
    }

    public Boolean ValidateUser(String email, String Password) {
        Users User = ur.findByEmail(email);
        if (User == null) {
            return false;
        }
        return passwordEncoder.matches(Password, User.getPassword());
    }

    public boolean existsByEmail(String email) {
        return ur.findByEmail(email) != null;
    }

    public Users ByEmail(String email) {
        return ur.findByEmail(email);
    }

    
    public void sendResetLink(String email) {
       System.out.println("reset method called in service");
        Users user = ur.findByEmail(email);
        if (user == null) {
            System.out.println("DEBUG: User not found for email: " + email);
            return;
        }

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setTokenExpiry(LocalDateTime.now().plusMinutes(30));
        ur.save(user);

        // Use frontend URL (React dev server in dev)
        String frontendUrl = "http://localhost:5173"; // change to your deployed domain in production
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Hi " + user.getName() + ",\n\nClick the link below to reset your password:\n" 
                        + resetLink + "\n\nThis link will expire in 30 minutes.");

        System.out.println("DEBUG: Attempting to send email to " + email);

        try {
            mailSender.send(message);
            System.out.println("DEBUG: Email sent successfully!");
        } catch (Exception e) {
            System.out.println("DEBUG: Failed to send email!");
            e.printStackTrace();
        }
        System.out.println("reset method end called in service");
    }



}