package com.zamagi.kas.controller;

import com.zamagi.kas.model.MasterAset;
import com.zamagi.kas.model.MasterKategori;
import com.zamagi.kas.model.User;
import com.zamagi.kas.repository.MasterAsetRepository;
import com.zamagi.kas.repository.MasterKategoriRepository;
import com.zamagi.kas.repository.TransaksiRepository;
import com.zamagi.kas.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/master")
@CrossOrigin(origins = "*")
public class MasterController {

    @Autowired private MasterAsetRepository asetRepo;
    @Autowired private MasterKategoriRepository kategoriRepo;
    @Autowired private TransaksiRepository transaksiRepo;
    @Autowired private UserRepository userRepo;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    // ===================== ASET =====================

    @GetMapping("/aset")
    public List<MasterAset> getAllAset() {
        return asetRepo.findByUserUsernameOrderByUrutanAscNamaAsc(getCurrentUser().getUsername());
    }

    @PostMapping("/aset")
    @Transactional
    public ResponseEntity<?> tambahAset(@RequestBody Map<String, String> body) {
        String nama = body.get("nama");
        if (nama == null || nama.isBlank())
            return ResponseEntity.badRequest().body("Nama aset wajib diisi");
        if (nama.length() > 100)
            return ResponseEntity.badRequest().body("Nama aset maksimal 100 karakter");

        User user = getCurrentUser();
        if (asetRepo.findByUserUsernameAndNamaIgnoreCase(user.getUsername(), nama.trim()).isPresent())
            return ResponseEntity.badRequest().body("Aset dengan nama ini sudah ada");

        MasterAset aset = new MasterAset();
        aset.setNama(nama.trim());
        aset.setIsAktif(true);
        aset.setUrutan(0);
        aset.setUser(user);

        return ResponseEntity.ok(asetRepo.save(aset));
    }

    @PutMapping("/aset/{id}")
    @Transactional
    public ResponseEntity<?> editAset(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        MasterAset aset = asetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Aset tidak ditemukan"));

        User user = getCurrentUser();

        // Edit nama
        if (body.containsKey("nama")) {
            String namaBaru = body.get("nama").toString().trim();
            if (namaBaru.isBlank())
                return ResponseEntity.badRequest().body("Nama aset tidak boleh kosong");
            if (namaBaru.length() > 100)
                return ResponseEntity.badRequest().body("Nama aset maksimal 100 karakter");
            if (asetRepo.existsByUserUsernameAndNamaIgnoreCaseAndIdNot(user.getUsername(), namaBaru, id))
                return ResponseEntity.badRequest().body("Nama aset sudah digunakan");

            String namaLama = aset.getNama();
            aset.setNama(namaBaru);
            asetRepo.save(aset);

            // Update nama di semua transaksi yang pakai aset ini
            updateNamaAsetDiTransaksi(user.getUsername(), namaLama, namaBaru);
        }

        // Toggle aktif/nonaktif
        if (body.containsKey("isAktif")) {
            aset.setIsAktif(Boolean.parseBoolean(body.get("isAktif").toString()));
            asetRepo.save(aset);
        }

        // Update urutan
        if (body.containsKey("urutan")) {
            aset.setUrutan(Integer.parseInt(body.get("urutan").toString()));
            asetRepo.save(aset);
        }

        return ResponseEntity.ok(aset);
    }

    @DeleteMapping("/aset/{id}")
    @Transactional
    public ResponseEntity<?> hapusAset(@PathVariable Long id) {
        MasterAset aset = asetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Aset tidak ditemukan"));

        // Cek apakah aset masih dipakai di transaksi
        long jumlahTransaksi = transaksiRepo
                .findByUserUsernameOrderByTanggalDescIdDesc(getCurrentUser().getUsername())
                .stream()
                .filter(t -> aset.getNama().equals(t.getSumberDana()))
                .count();

        if (jumlahTransaksi > 0)
            return ResponseEntity.badRequest().body(
                "Aset tidak bisa dihapus karena masih digunakan di " + jumlahTransaksi + " transaksi. " +
                "Nonaktifkan saja jika tidak ingin ditampilkan."
            );

        asetRepo.deleteById(id);
        return ResponseEntity.ok("Aset berhasil dihapus");
    }

    // Helper: update nama aset di semua transaksi
    private void updateNamaAsetDiTransaksi(String username, String namaLama, String namaBaru) {
        List<com.zamagi.kas.model.Transaksi> transaksis = transaksiRepo
                .findByUserUsernameOrderByTanggalDescIdDesc(username);
        transaksis.forEach(t -> {
            if (namaLama.equals(t.getSumberDana())) {
                t.setSumberDana(namaBaru);
                transaksiRepo.save(t);
            }
        });
    }

    // ===================== KATEGORI =====================

    @GetMapping("/kategori")
    public List<MasterKategori> getAllKategori() {
        return kategoriRepo.findByUserUsernameOrderByNamaAsc(getCurrentUser().getUsername());
    }

    @PostMapping("/kategori")
    @Transactional
    public ResponseEntity<?> tambahKategori(@RequestBody Map<String, String> body) {
        String nama = body.get("nama");
        if (nama == null || nama.isBlank())
            return ResponseEntity.badRequest().body("Nama kategori wajib diisi");
        if (nama.length() > 100)
            return ResponseEntity.badRequest().body("Nama kategori maksimal 100 karakter");

        User user = getCurrentUser();
        if (kategoriRepo.existsByUserUsernameAndNamaIgnoreCase(user.getUsername(), nama.trim()))
            return ResponseEntity.badRequest().body("Kategori dengan nama ini sudah ada");

        MasterKategori kategori = new MasterKategori();
        kategori.setNama(nama.trim());
        kategori.setUser(user);

        return ResponseEntity.ok(kategoriRepo.save(kategori));
    }

    @PutMapping("/kategori/{id}")
    @Transactional
    public ResponseEntity<?> editKategori(@PathVariable Long id, @RequestBody Map<String, String> body) {
        MasterKategori kategori = kategoriRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        String namaBaru = body.get("nama");
        if (namaBaru == null || namaBaru.isBlank())
            return ResponseEntity.badRequest().body("Nama kategori tidak boleh kosong");
        if (namaBaru.length() > 100)
            return ResponseEntity.badRequest().body("Nama kategori maksimal 100 karakter");

        User user = getCurrentUser();
        if (kategoriRepo.existsByUserUsernameAndNamaIgnoreCaseAndIdNot(user.getUsername(), namaBaru.trim(), id))
            return ResponseEntity.badRequest().body("Nama kategori sudah digunakan");

        String namaLama = kategori.getNama();
        kategori.setNama(namaBaru.trim());
        kategoriRepo.save(kategori);

        // Update nama kategori di semua transaksi
        List<com.zamagi.kas.model.Transaksi> transaksis = transaksiRepo
                .findByUserUsernameOrderByTanggalDescIdDesc(user.getUsername());
        transaksis.forEach(t -> {
            if (namaLama.equals(t.getKategori())) {
                t.setKategori(namaBaru.trim());
                transaksiRepo.save(t);
            }
        });

        return ResponseEntity.ok(kategori);
    }

    @DeleteMapping("/kategori/{id}")
    @Transactional
    public ResponseEntity<?> hapusKategori(@PathVariable Long id) {
        MasterKategori kategori = kategoriRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        // Cek apakah kategori masih dipakai
        long jumlahTransaksi = transaksiRepo
                .findByUserUsernameOrderByTanggalDescIdDesc(getCurrentUser().getUsername())
                .stream()
                .filter(t -> kategori.getNama().equals(t.getKategori()))
                .count();

        if (jumlahTransaksi > 0)
            return ResponseEntity.badRequest().body(
                "Kategori tidak bisa dihapus karena masih digunakan di " + jumlahTransaksi + " transaksi."
            );

        kategoriRepo.deleteById(id);
        return ResponseEntity.ok("Kategori berhasil dihapus");
    }
}