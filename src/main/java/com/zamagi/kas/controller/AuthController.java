package com.zamagi.kas.controller;

import com.zamagi.kas.model.User;
import com.zamagi.kas.repository.UserRepository;
import com.zamagi.kas.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Error: Username sudah terdaftar!";
        }
        // Simpan password dalam bentuk ter-enkripsi
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User berhasil didaftarkan!";
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user) {
        var userOptional = userRepository.findByUsername(user.getUsername());

        if (userOptional.isPresent()) {
            User u = userOptional.get();
            if (encoder.matches(user.getPassword(), u.getPassword())) {
                String token = jwtUtils.generateJwtToken(u.getUsername());
                return ResponseEntity.ok(Map.of("token", token, "username", u.getUsername()));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Username atau Password salah!"));
    }
}
