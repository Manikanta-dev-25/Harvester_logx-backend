package com.mani.Contoller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.mani.Entity.LogEntry;
import com.mani.Entity.Users;
import com.mani.Service.LogService;
import com.mani.Service.UserService;
import com.mani.respository.LogRepository;
import com.mani.respository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://manikanta-dev-25.github.io") // Allow your frontend
public class AuthController {

    @Autowired
    public UserService us;

    @Autowired
    public UserRepository ur;

    @Autowired
    public LogRepository lr;

    @Autowired
    public LogService ls;

    // ----------------- LOGIN -----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        Users existingUser = us.ByEmail(user.getEmail());
        if (existingUser != null) {
            if (us.ValidateUser(user.getEmail(), user.getPassword())) {
                Map<String, String> response = new HashMap<>();
                response.put("name", existingUser.getName()); // Send real name
                response.put("message", "Login successful");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Password doesn't match"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
    }

    // ----------------- SIGNUP -----------------
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> Signup(@RequestBody Users LoginDetails) {
        us.RegisterUser(LoginDetails);
        Map<String, String> response = new HashMap<>();
        response.put("name", LoginDetails.getName());
        response.put("message", "Signup successful ✅");
        return ResponseEntity.ok(response);
    }

    // ----------------- FORGOT PASSWORD -----------------
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Users user) {
        if (!us.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(404).body("Email not found");
        }
        us.sendResetLink(user.getEmail());
        return ResponseEntity.ok("Password reset link sent (simulated)");
    }

    // ----------------- RESET PASSWORD -----------------
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        Users user = ur.findByResetToken(token);
        if (user == null) {
            return ResponseEntity.status(404).body("Invalid token");
        }

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(400).body("Token has expired");
        }

        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        ur.save(user);

        return ResponseEntity.ok("Password has been reset successfully!");
    }

    // ----------------- SAVE SINGLE LOG -----------------
    @PostMapping("/logs/save")
    public ResponseEntity<LogEntry> saveLog(@RequestBody LogEntry lr) {
        if (lr.getCreatedBy() == null || lr.getName() == null || lr.getPhno() == null || lr.getVillage() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        LogEntry savedLog = ls.SaveEntries(lr);
        return ResponseEntity.ok(savedLog);
    }

    // ----------------- GET LOGS BY USER -----------------
    @GetMapping("/logs/user/{createdBy}")
    public ResponseEntity<List<LogEntry>> getLogsByCreatedBy(@PathVariable String createdBy) {
        List<LogEntry> logs = lr.findByCreatedByIgnoreCase(createdBy.trim());
        return ResponseEntity.ok(logs);
    }

    // ----------------- BATCH UPDATE LOGS -----------------
    @PutMapping("/logs/batch-update")
    public ResponseEntity<?> updateLogs(@RequestBody List<LogEntry> updatedLogs, @RequestParam String user) {
        List<LogEntry> savedLogs = new ArrayList<>();

        for (LogEntry updatedLog : updatedLogs) {
            LogEntry existing = lr.findById(updatedLog.getId()).orElse(null);
            if (existing == null) continue; // Skip if not found
            if (!existing.getCreatedBy().equalsIgnoreCase(user.trim())) continue; // Skip unauthorized updates

            updatedLog.setCreatedBy(existing.getCreatedBy());
            LogEntry saved = ls.SaveEntries(updatedLog);
            savedLogs.add(saved);
        }

        if (savedLogs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No logs were updated.");
        }

        return ResponseEntity.ok(savedLogs);
    }

    // ----------------- DELETE SINGLE LOG -----------------
    @DeleteMapping("/logs/delete/{id}")
    public ResponseEntity<String> deleteLog(@PathVariable Long id) {
        lr.deleteById(id);
        return ResponseEntity.ok("Log deleted");
    }

    // ----------------- BATCH DELETE LOGS -----------------
    @DeleteMapping("/logs/batch-delete")
    public ResponseEntity<String> deleteSelectedLogs(@RequestBody Map<String, List<Long>> body,
                                                     @RequestParam String user) {
        List<Long> idsToDelete = body.get("ids");

        if (idsToDelete == null || idsToDelete.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No log IDs provided for deletion.");
        }

        try {
            List<Long> authorizedIds = new ArrayList<>();
            List<LogEntry> logs = lr.findAllById(idsToDelete);

            for (LogEntry log : logs) {
                if (log.getCreatedBy().equalsIgnoreCase(user.trim())) {
                    authorizedIds.add(log.getId());
                }
            }

            if (authorizedIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Unauthorized: Cannot delete logs you did not create.");
            }

            lr.deleteAllById(authorizedIds);
            return ResponseEntity.ok(authorizedIds.size() + " log(s) deleted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to execute batch delete: " + e.getMessage());
        }
    }

    // ----------------- OPTIONAL: FILTER LOGS -----------------
    @GetMapping("/logs/filter")
    public ResponseEntity<List<LogEntry>> filterLogs(@RequestParam(required = false) String name,
                                                     @RequestParam(required = false) String village,
                                                     @RequestParam(required = false) Double hourlyWage) {
        List<LogEntry> results = ls.filterLogs(name, village, hourlyWage);
        return ResponseEntity.ok(results);
    }
}
