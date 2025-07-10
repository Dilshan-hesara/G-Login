package com.dilshan.login.controller;



import com.dilshan.login.model.User;
import com.dilshan.login.payload.JwtAuthenticationResponse;
import com.dilshan.login.payload.LoginRequest;
import com.dilshan.login.payload.SignUpRequest;
import com.dilshan.login.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow requests from any origin (for testing)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    // We will create JwtTokenProvider later
    // @Autowired
    // JwtTokenProvider tokenProvider;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // This is a simplified login. In a real app, use the tokenProvider to generate a token.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // String jwt = tokenProvider.generateToken(authentication);
        // For now, let's return a simple success message. We'll add JWT later.
        return ResponseEntity.ok(new JwtAuthenticationResponse("DUMMY_JWT_TOKEN_GOES_HERE"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        if(userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return new ResponseEntity<>("Email Address already in use!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }



    // goole ac save


    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody SignUpRequest googleUser) { // We can reuse the SignUpRequest DTO
        // Check if user with this email already exists
        Optional<User> existingUserOptional = userRepository.findByEmail(googleUser.getEmail());

        User user;
        if (existingUserOptional.isPresent()) {
            // User already exists, so this is a login
            user = existingUserOptional.get();
        } else {
            // User does not exist, so we register them (Sign Up)
            user = new User();
            user.setName(googleUser.getName());
            user.setEmail(googleUser.getEmail());
            // For Google users, we can set a random secure password as they won't use it
            user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            userRepository.save(user);
        }

        // Now, create an authentication token for this user (for both login and new sign up)
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // For now, we will return a dummy token. In a real app, you would generate a proper JWT.
        // NOTE: This part needs a proper JWT generation logic for full functionality.
        // We are simplifying it to return a basic response.
        return ResponseEntity.ok(new JwtAuthenticationResponse("DUMMY_JWT_TOKEN_FOR_" + user.getEmail()));
    }
}
