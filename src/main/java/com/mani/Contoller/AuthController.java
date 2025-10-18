package com.mani.Contoller; // Keep folder name consistent; fix typo if desired to "Controller"

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mani.Entity.LogEntry;
import com.mani.Entity.Users;
import com.mani.Service.LogService;
import com.mani.Service.UserService;
import com.mani.respository.LogRepository;
import com.mani.respository.UserRepository;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = {"https://manikanta-dev-25.github.io/Harvester_logx-frontend"})
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ------------------ AUTH ------------------

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        Users existingUser = userService.ByEmail(user.getEmail());
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        boolean valid = userService.ValidateUser(user.getEmail(), user.getPassword());
        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Password doesn't match"));
        }

        Map<String, String> response = new HashMap<>();
        response.put("name", existingUser.getName());
        response.put("message", "Login successful ✅");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Users user) {
        userService.RegisterUser(user);
        Map<String, String> response = new HashMap<>();
        response.put("name", user.getName());
        response.put("message", "Signup successful ✅");
        return ResponseEntity.ok(response);
    }

    // ------------------ PASSWORD RESET ------------------

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Users user) {
        boolean exists = userService.existsByEmail(user.getEmail());
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }

        userService.sendResetLink(user.getEmail());
        return ResponseEntity.ok("Password reset link sent ✅");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        Users user = userRepository.findByResetToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid token");
        }

        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password has been reset successfully ✅");
    }

    // ------------------ LOGS ------------------

    @PostMapping("/logs/save")
    public ResponseEntity<LogEntry> saveLog(@RequestBody LogEntry log) {
        if (log.getCreatedBy() == null || log.getName() == null || log.getPhno() == null || log.getVillage() == null) {
            return ResponseEntity.badRequest().body(null);
        }

        LogEntry saved = logService.SaveEntries(log);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/logs/user/{createdBy}")
    public ResponseEntity<List<LogEntry>> getLogsByCreatedBy(@PathVariable String createdBy) {
        List<LogEntry> logs = logRepository.findByCreatedByIgnoreCase(createdBy.trim());
        return ResponseEntity.ok(logs);
    }

    @PutMapping("/logs/batch-update")
    public ResponseEntity<?> updateLogs(@RequestBody List<LogEntry> updatedLogs, @RequestParam String user) {
        List<LogEntry> savedLogs = new ArrayList<>();

        for (LogEntry updatedLog : updatedLogs) {
            LogEntry existing = logRepository.findById(updatedLog.getId()).orElse(null);
            if (existing == null || !existing.getCreatedBy().equalsIgnoreCase(user.trim())) continue;

            updatedLog.setCreatedBy(existing.getCreatedBy());
            LogEntry saved = logService.SaveEntries(updatedLog);
            savedLogs.add(saved);
        }

        if (savedLogs.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("No logs were updated.");
        return ResponseEntity.ok(savedLogs);
    }

    @DeleteMapping("/logs/delete/{id}")
    public ResponseEntity<String> deleteLog(@PathVariable Long id, @RequestParam String user) {
        LogEntry log = logRepository.findById(id).orElse(null);
        if (log == null || !log.getCreatedBy().equalsIgnoreCase(user.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized to delete this log");
        }
        logRepository.deleteById(id);
        return ResponseEntity.ok("Log deleted ✅");
    }

    @DeleteMapping("/logs/batch-delete")
    public ResponseEntity<String> deleteSelectedLogs(@RequestBody Map<String, List<Long>> body,
                                                     @RequestParam String user) {
        List<Long> idsToDelete = body.get("ids");
        if (idsToDelete == null || idsToDelete.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No log IDs provided");

        List<LogEntry> logs = logRepository.findAllById(idsToDelete);
        List<Long> authorizedIds = new ArrayList<>();
        for (LogEntry log : logs) {
            if (log.getCreatedBy().equalsIgnoreCase(user.trim())) {
                authorizedIds.add(log.getId());
            }
        }

        if (authorizedIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Unauthorized: Cannot delete logs you did not create");
        }

        logRepository.deleteAllById(authorizedIds);
        return ResponseEntity.ok(authorizedIds.size() + " log(s) deleted ✅");
    }

    @GetMapping("/logs/filter")
    public ResponseEntity<List<LogEntry>> filterLogs(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String village,
            @RequestParam(required = false) Double hourlyWage) {

        List<LogEntry> results = logService.filterLogs(name, village, hourlyWage);
        return ResponseEntity.ok(results);
    }
}
