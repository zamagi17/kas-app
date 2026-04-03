package com.zamagi.kas.controller;

import com.zamagi.kas.model.Transaksi;
import com.zamagi.kas.model.UtangPiutang;
import com.zamagi.kas.model.User;
import com.zamagi.kas.repository.TransaksiRepository;
import com.zamagi.kas.repository.UtangPiutangRepository;
import com.zamagi.kas.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/utang-piutang")
@CrossOrigin(origins = "*")
public class UtangPiutangController {

    @Autowired
    private UtangPiutangRepository utangPiutangRepository;

    @Autowired
    private TransaksiRepository transaksiRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
    }

    private String validasi(UtangPiutang u) {
        if (u.getJenis() == null || (!u.getJenis().equals("Utang") && !u.getJenis().equals("Piutang"))) {
            return "Jenis harus Utang atau Piutang";
        }
        if (u.getNamaPihak() == null || u.getNamaPihak().isBlank()) {
            return "Nama pihak wajib diisi";
        }
        if (u.getNamaPihak().length() > 100) {
            return "Nama pihak maksimal 100 karakter";
        }
        if (u.getNominalAwal() == null || u.getNominalAwal() <= 0) {
            return "Nominal harus lebih dari 0";
        }
        if (u.getTanggalMulai() == null) {
            return "Tanggal mulai wajib diisi";
        }
        if (u.getAsetTerkait() == null || u.getAsetTerkait().isBlank()) {
            return "Aset terkait wajib diisi";
        }
        return null;
    }

    // GET semua utang piutang milik user
    @GetMapping
    public List<UtangPiutang> getAll() {
        return utangPiutangRepository
                .findByUserUsernameOrderByStatusAscJatuhTempoAsc(getCurrentUser().getUsername());
    }

    // POST catat utang/piutang baru
    @PostMapping
    @Transactional
    public ResponseEntity<?> tambah(@RequestBody UtangPiutang data) {
        String error = validasi(data);
        if (error != null) {
            return ResponseEntity.badRequest().body(error);
        }

        User user = getCurrentUser();
        data.setUser(user);
        data.setSudahDibayar(0L);
        data.setStatus("Belum Lunas");

        UtangPiutang saved = utangPiutangRepository.save(data);

        // Otomatis catat ke transaksi untuk update saldo aset
        Transaksi t = new Transaksi();
        t.setTanggal(data.getTanggalMulai());
        t.setKategori(data.getJenis().equals("Utang") ? "Utang" : "Piutang");
        t.setAsetTerkait(data.getAsetTerkait());
        t.setSumberDana(data.getAsetTerkait());
        t.setNominal(data.getNominalAwal());
        t.setUser(user);

        if (data.getJenis().equals("Utang")) {
            t.setJenis("Utang Masuk");       // Jenis khusus, tidak masuk arus kas
            t.setKeterangan("Terima utang dari " + data.getNamaPihak() + " [ID:" + saved.getId() + "]");
        } else {
            t.setJenis("Piutang Keluar");    // Jenis khusus, tidak masuk arus kas
            t.setKeterangan("Piutang ke " + data.getNamaPihak() + " [ID:" + saved.getId() + "]");
        }
        transaksiRepository.save(t);

        return ResponseEntity.ok(saved);
    }

    // POST bayar cicilan
    @PostMapping("/{id}/bayar")
    @Transactional
    public ResponseEntity<?> bayar(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        UtangPiutang up = utangPiutangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

        Long nominalBayar = Long.parseLong(payload.get("nominalBayar").toString());
        String asetBayar = payload.get("asetBayar").toString();
        String keteranganBayar = payload.getOrDefault("keterangan", "").toString();

        if (nominalBayar <= 0) {
            return ResponseEntity.badRequest().body("Nominal bayar harus lebih dari 0");
        }
        if (asetBayar == null || asetBayar.isBlank()) {
            return ResponseEntity.badRequest().body("Aset bayar wajib diisi");
        }
        if (nominalBayar > up.getSisaTagihan()) {
            return ResponseEntity.badRequest().body("Nominal melebihi sisa tagihan");
        }

        // Update sudahDibayar
        up.setSudahDibayar(up.getSudahDibayar() + nominalBayar);

        // Cek apakah sudah lunas
        if (up.getSisaTagihan() <= 0) {
            up.setStatus("Lunas");
        }

        utangPiutangRepository.save(up);

        // Catat ke transaksi untuk update saldo aset
        Transaksi t = new Transaksi();
        t.setTanggal(LocalDate.now());
        t.setKategori(up.getJenis().equals("Utang") ? "Bayar Utang" : "Terima Piutang");
        t.setAsetTerkait(asetBayar);
        t.setSumberDana(asetBayar);
        t.setNominal(nominalBayar);
        t.setUser(up.getUser());

        if (up.getJenis().equals("Utang")) {
            t.setJenis("Bayar Utang");       // Jenis khusus
            t.setKategori("Bayar Utang");
            t.setKeterangan("Bayar utang ke " + up.getNamaPihak()
                    + (keteranganBayar.isBlank() ? "" : " - " + keteranganBayar)
                    + " [ID:" + id + "]");
        } else {
            t.setJenis("Terima Piutang");    // Jenis khusus
            t.setKategori("Terima Piutang");
            t.setKeterangan("Terima piutang dari " + up.getNamaPihak()
                    + (keteranganBayar.isBlank() ? "" : " - " + keteranganBayar)
                    + " [ID:" + id + "]");
        }
        transaksiRepository.save(t);

        return ResponseEntity.ok(up);
    }

    // PUT tandai lunas sekaligus
    @PutMapping("/{id}/lunas")
    @Transactional
    public ResponseEntity<?> tandaiLunas(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        UtangPiutang up = utangPiutangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

        if (up.getStatus().equals("Lunas")) {
            return ResponseEntity.badRequest().body("Sudah lunas");
        }

        String asetBayar = payload.get("asetBayar").toString();
        if (asetBayar == null || asetBayar.isBlank()) {
            return ResponseEntity.badRequest().body("Aset bayar wajib diisi");
        }

        Long sisaTagihan = up.getSisaTagihan();

        up.setSudahDibayar(up.getNominalAwal());
        up.setStatus("Lunas");
        utangPiutangRepository.save(up);

        // Catat sisa ke transaksi
        if (sisaTagihan > 0) {
            Transaksi t = new Transaksi();
            t.setTanggal(LocalDate.now());
            t.setAsetTerkait(asetBayar);
            t.setSumberDana(asetBayar);
            t.setNominal(sisaTagihan);
            t.setUser(up.getUser());

            if (up.getJenis().equals("Utang")) {
                t.setJenis("Bayar Utang");
                t.setKategori("Bayar Utang");
                t.setKeterangan("Pelunasan utang ke " + up.getNamaPihak() + " [ID:" + id + "]");
            } else {
                t.setJenis("Terima Piutang");
                t.setKategori("Terima Piutang");
                t.setKeterangan("Pelunasan piutang dari " + up.getNamaPihak() + " [ID:" + id + "]");
            }
            transaksiRepository.save(t);
        }

        return ResponseEntity.ok(up);
    }

    // DELETE hapus utang/piutang beserta transaksinya
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> hapus(@PathVariable Long id) {
        UtangPiutang up = utangPiutangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

        // Hapus transaksi terkait (yang dibuat otomatis saat membuat/membayar utang/piutang)
        transaksiRepository.deleteByKeteranganContaining("[ID:" + id + "]");

        // Hapus UtangPiutang
        utangPiutangRepository.deleteById(id);
        return ResponseEntity.ok("Data berhasil dihapus");
    }
}
