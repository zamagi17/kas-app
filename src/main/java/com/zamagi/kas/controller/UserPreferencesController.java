package com.zamagi.kas.controller;

import com.zamagi.kas.model.User;
import com.zamagi.kas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserPreferencesController {

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // GET /api/user/preferences
    @GetMapping("/preferences")
    public ResponseEntity<?> getPreferences() {
        User user = getCurrentUser();
        List<String> dompetList = parseDompetHarian(user.getDompetHarian());
        return ResponseEntity.ok(Map.of("dompetHarian", dompetList));
    }

    // PUT /api/user/preferences
    // Body: { "dompetHarian": ["BCA", "SeaBank", "Dompet Tunai"] }
    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(@RequestBody Map<String, List<String>> body) {
        List<String> dompetList = body.get("dompetHarian");

        if (dompetList == null) {
            return ResponseEntity.badRequest().body("Field 'dompetHarian' wajib ada");
        }

        for (String item : dompetList) {
            if (item == null || item.isBlank()) {
                return ResponseEntity.badRequest().body("Nama aset tidak boleh kosong");
            }
            if (item.length() > 100) {
                return ResponseEntity.badRequest().body("Nama aset maksimal 100 karakter");
            }
        }

        User user = getCurrentUser();

        // Simpan sebagai separator string — tidak butuh Jackson sama sekali
        // Format: "BCA||SeaBank||Dompet Tunai"
        String stored = dompetList.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("||"));

        user.setDompetHarian(stored);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Dompet harian berhasil disimpan",
                "dompetHarian", dompetList
        ));
    }

    // Parse "BCA||SeaBank||Dompet Tunai" → ["BCA", "SeaBank", "Dompet Tunai"]
    private List<String> parseDompetHarian(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return Arrays.stream(value.split("\\|\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}