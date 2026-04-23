package com.zamagi.kas.controller;

import com.zamagi.kas.model.Tabungan;
import com.zamagi.kas.model.User;
import com.zamagi.kas.repository.TabunganRepository;
import com.zamagi.kas.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tabungan")
public class TabunganController {

    @Autowired
    private TabunganRepository tabunganRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method untuk mendapatkan User yang sedang login
    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));
    }

    // GET: Ambil semua target tabungan milik user yang sedang login
    @GetMapping
    public ResponseEntity<List<Tabungan>> getTabungan(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Tabungan> tabunganList = tabunganRepository.findByUserId(user.getId());
        return ResponseEntity.ok(tabunganList);
    }

    // POST: Tambah target tabungan baru
    @PostMapping
    public ResponseEntity<?> createTabungan(@RequestBody Tabungan tabunganRequest, Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        Tabungan tabungan = new Tabungan();
        tabungan.setUser(user);
        tabungan.setNama(tabunganRequest.getNama());
        tabungan.setTargetNominal(tabunganRequest.getTargetNominal());
        tabungan.setTerkumpul(tabunganRequest.getTerkumpul() != null ? tabunganRequest.getTerkumpul() : 0.0);
        tabungan.setDeadline(tabunganRequest.getDeadline());

        tabunganRepository.save(tabungan);
        return ResponseEntity.ok(tabungan);
    }

    // (Opsional) DELETE: Hapus target tabungan
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTabungan(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Tabungan> tabunganOpt = tabunganRepository.findById(id);

        if (tabunganOpt.isPresent() && tabunganOpt.get().getUser().getId().equals(user.getId())) {
            tabunganRepository.deleteById(id);
            return ResponseEntity.ok("Target tabungan berhasil dihapus");
        }
        return ResponseEntity.badRequest().body("Gagal menghapus atau data tidak ditemukan");
    }
    
    // PUT: Update target tabungan
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTabungan(@PathVariable Long id, @RequestBody Tabungan tabunganRequest, Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Tabungan> tabunganOpt = tabunganRepository.findById(id);

        // Pastikan data ada dan milik user yang sedang login
        if (tabunganOpt.isPresent() && tabunganOpt.get().getUser().getId().equals(user.getId())) {
            Tabungan tabungan = tabunganOpt.get();
            
            // Ubah nilai dengan data yang baru dari request
            tabungan.setNama(tabunganRequest.getNama());
            tabungan.setTargetNominal(tabunganRequest.getTargetNominal());
            
            // Jika Anda menggunakan metode "Virtual Envelope", terkumpul juga bisa diupdate dari sini.
            // Jika menggunakan metode Top-Up transaksi terpisah, baris ini bisa dihapus/disesuaikan.
            if (tabunganRequest.getTerkumpul() != null) {
                tabungan.setTerkumpul(tabunganRequest.getTerkumpul());
            }
            
            tabungan.setDeadline(tabunganRequest.getDeadline());

            // Save akan men-trigger SQL UPDATE, dan @UpdateTimestamp akan otomatis bekerja
            tabunganRepository.save(tabungan);
            
            return ResponseEntity.ok(tabungan);
        }
        
        return ResponseEntity.badRequest().body("Gagal update: Data tidak ditemukan atau Anda tidak memiliki akses.");
    }
}