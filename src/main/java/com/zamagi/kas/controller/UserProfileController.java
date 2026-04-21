package com.zamagi.kas.controller;

import com.zamagi.kas.model.User;
import com.zamagi.kas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserProfileController {

    @Autowired
    private UserRepository userRepository;

    // 1. GET: Mengirimkan data profil ke frontend saat halaman Settings dimuat
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        // Ambil username dari token JWT yang sedang login
        String username = authentication.getName();
        
        // Cari user di database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User tidak ditemukan."));
        
        // Bungkus data ke dalam Map/JSON untuk dikirim ke frontend
        Map<String, String> profil = new HashMap<>();
        profil.put("username", user.getUsername());
        profil.put("namaLengkap", user.getNamaLengkap());
        profil.put("email", user.getEmail());
        profil.put("nomorHp", user.getNomorHp());
        
        return ResponseEntity.ok(profil);
    }

    // 2. PUT: Menyimpan perubahan data profil yang diedit dari frontend
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, String> req, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User tidak ditemukan."));
        
        // Jika data dikirimkan dari React, simpan ke objek user
        if (req.containsKey("namaLengkap")) {
            user.setNamaLengkap(req.get("namaLengkap"));
        }
        if (req.containsKey("email")) {
            user.setEmail(req.get("email"));
        }
        if (req.containsKey("nomorHp")) {
            user.setNomorHp(req.get("nomorHp"));
        }
        
        // Simpan pembaruan ke database
        userRepository.save(user);
        
        return ResponseEntity.ok("Profil berhasil diupdate");
    }
}