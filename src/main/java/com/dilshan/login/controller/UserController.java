package com.dilshan.login.controller;



import com.dilshan.login.model.User;
import com.dilshan.login.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Endpoint to get current user's details
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        // Don't send the password back to the frontend
        User user = userOptional.get();
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    // Endpoint to update user's name and password
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody Map<String, String> updates) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User userToUpdate = userOptional.get();

        String newName = updates.get("name");
        if (newName != null && !newName.isEmpty()) {
            userToUpdate.setName(newName);
        }

        String newPassword = updates.get("password");
        if (newPassword != null && !newPassword.isEmpty()) {
            userToUpdate.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(userToUpdate);
        return ResponseEntity.ok("User details updated successfully!");
    }
}