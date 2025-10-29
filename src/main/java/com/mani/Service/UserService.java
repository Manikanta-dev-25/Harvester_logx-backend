package com.mani.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import sibApi.Configuration;
import sibApi.ApiClient;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Users RegisterUser(Users User) {
        User.setPassword(passwordEncoder.encode(User.getPassword()));
        return ur.save(User);
    }

    public Boolean ValidateUser(String email, String Password) {
        Users User = ur.findByEmailIgnoreCase(email.trim());
        if (User == null) {
            return false;
        }
        return passwordEncoder.matches(Password, User.getPassword());
    }

    public boolean existsByEmail(String email) {
        return ur.findByEmailIgnoreCase(email.trim()) != null;
    }

    public Users ByEmail(String email) {
        return ur.findByEmailIgnoreCase(email.trim());
    }

    
    public void sendResetLink(String email) {
    System.out.println("reset method called in service");

    Users user = ur.findByEmailIgnoreCase(email.trim());
    if (user == null) {
        System.out.println("DEBUG: User not found for email: " + email);
        return;
    }

    // Generate reset token and expiry
    String token = UUID.randomUUID().toString();
    user.setResetToken(token);
    user.setTokenExpiry(LocalDateTime.now().plusMinutes(30));
    ur.save(user);

    // ✅ Use deployed frontend URL instead of localhost
    String frontendUrl = "https://manikanta-dev-25.github.io/Harvester_logx-frontend";
    String resetLink = frontendUrl + "/reset-password?token=" + token;

    // ✅ Brevo API setup
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setApiKey("api-key", "bRTAMZI9Kfyp3FwV"); // Your Brevo API key

    TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(defaultClient);

    SendSmtpEmailSender sender = new SendSmtpEmailSender();
    sender.setEmail("kondapakamani75@gmail.com"); // Must match verified sender in Brevo

    SendSmtpEmailTo to = new SendSmtpEmailTo();
    to.setEmail(email);

    SendSmtpEmail emailRequest = new SendSmtpEmail();
    emailRequest.setSender(sender);
    emailRequest.setTo(Collections.singletonList(to));
    emailRequest.setSubject("Password Reset Request");
    emailRequest.setTextContent("Hi " + user.getName() + ",\n\n"
        + "Click the link below to reset your password:\n"
        + resetLink + "\n\n"
        + "This link will expire in 30 minutes.\n\n"
        + "If you didn’t request this, please ignore the email.");

    System.out.println("DEBUG: Attempting to send email to " + email);

    try {
        CreateSmtpEmail response = apiInstance.sendTransacEmail(emailRequest);
        System.out.println("DEBUG: Email sent! Message ID: " + response.getMessageId());
    } catch (Exception e) {
        System.out.println("DEBUG: Failed to send email!");
        e.printStackTrace();
    }

    System.out.println("reset method end called in service");
}



}
