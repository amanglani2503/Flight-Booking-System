package com.example.user_service.controller;

import com.example.user_service.model.UserRegistration;
import com.example.user_service.service.JWTService;
import com.example.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/profile")
    public ResponseEntity<UserRegistration> getUserProfile(HttpServletRequest request) {
        String token = userService.extractToken(request);
        String email = jwtService.extractUsername(token);
        UserRegistration userRegistration = userService.getUserByEmail(email);
        return ResponseEntity.ok(userRegistration);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<UserRegistration> updateUserProfile(@PathVariable Integer id, @Valid @RequestBody UserRegistration updatedUserRegistration) {
        UserRegistration userRegistration = userService.updateUser(id, updatedUserRegistration);
        return ResponseEntity.ok(userRegistration);
    }

}
