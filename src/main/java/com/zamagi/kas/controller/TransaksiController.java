package com.zamagi.kas.controller;

import com.zamagi.kas.model.Transaksi;
import com.zamagi.kas.model.User;
import com.zamagi.kas.model.UtangPiutang;
import com.zamagi.kas.repository.TransaksiRepository;
import com.zamagi.kas.repository.UserRepository;
import com.zamagi.kas.repository.UtangPiutangRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transaksi")
@CrossOrigin(origins = "*")
public class TransaksiController {

    @Autowired
    private TransaksiRepository transaksiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UtangPiutangRepository utangPiutangRepository;

    // Daftar jenis yang diizinkan
    private static final List<String> JENIS_VALID = List.of(
        "Pemasukan", "Pengeluaran",
        "Rencana Pemasukan", "Rencana Pengeluaran"
    );

    // Helper validasi, return pesan error atau null kalau valid
    private String validasiTransaksi(Transaksi t) {
        if (t.getNominal() == null || t.getNominal() <= 0)
            return "Nominal harus lebih dari 0";

        if (t.getNominal() > 999_999_999_999L)
            return "Nominal maksimal Rp 999.999.999.999";

        if (t.getJenis() == null || !JENIS_VALID.contains(t.getJenis()))
            return "Jenis transaksi tidak valid";

        if (t.getTanggal() == null)
            return "Tanggal wajib diisi";

        if (t.getTanggal().isBefore(LocalDate.of(2000, 1, 1)))
            return "Tanggal terlalu jauh ke belakang (minimal tahun 2000)";

        if (t.getTanggal().isAfter(LocalDate.now().plusYears(1)))
            return "Tanggal terlalu jauh ke depan (maksimal 1 tahun)";

        if (t.getKategori() == null || t.getKategori().isBlank())
            return "Kategori wajib diisi";

        if (t.getKategori().length() > 100)
            return "Kategori maksimal 100 karakter";

        if (t.getSumberDana() == null || t.getSumberDana().isBlank())
            return "Sumber dana wajib diisi";

        if (t.getSumberDana().length() > 100)
            return "Sumber dana maksimal 100 karakter";

        if (t.getKeterangan() != null && t.getKeterangan().length() > 500)
            return "Keterangan maksimal 500 karakter";

        return null; // valid
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> tambahTransaksi(@RequestBody Transaksi transaksi) {
        String error = validasiTransaksi(transaksi);
        if (error != null) return ResponseEntity.badRequest().body(error);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        transaksi.setUser(user);

        return ResponseEntity.ok(transaksiRepository.save(transaksi));
    }

    @GetMapping
    public List<Transaksi> getAllTransaksi() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return transaksiRepository.findByUserUsernameOrderByTanggalDescIdDesc(username);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateTransaksi(@PathVariable Long id, @RequestBody Transaksi detailTransaksi) {
        String error = validasiTransaksi(detailTransaksi);
        if (error != null) return ResponseEntity.badRequest().body(error);

        Transaksi transaksi = transaksiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

        transaksi.setTanggal(detailTransaksi.getTanggal());
        transaksi.setKategori(detailTransaksi.getKategori());
        transaksi.setJenis(detailTransaksi.getJenis());
        transaksi.setSumberDana(detailTransaksi.getSumberDana());
        transaksi.setNominal(detailTransaksi.getNominal());
        transaksi.setKeterangan(detailTransaksi.getKeterangan());

        return ResponseEntity.ok(transaksiRepository.save(transaksi));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> hapusTransaksi(@PathVariable Long id) {
        Transaksi transaksi = transaksiRepository.findById(id)
                .orElse(null);
        if (transaksi == null) {
            return ResponseEntity.badRequest().body("Data tidak ditemukan");
        }

        // Jika kategori "Bayar Utang" atau "Terima Piutang", kurangi sudah_bayar di utang_piutang
        if ("Bayar Utang".equals(transaksi.getKategori()) || "Terima Piutang".equals(transaksi.getKategori())) {
            // Ekstrak ID dari keterangan, misalnya "[ID:123]"
            String keterangan = transaksi.getKeterangan();
            if (keterangan != null) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[ID:(\\d+)\\]");
                java.util.regex.Matcher matcher = pattern.matcher(keterangan);
                if (matcher.find()) {
                    Long utangId = Long.parseLong(matcher.group(1));
                    UtangPiutang utang = utangPiutangRepository.findById(utangId).orElse(null);
                    if (utang != null) {
                        // Kurangi sudah_bayar
                        Long newSudahBayar = utang.getSudahDibayar() - transaksi.getNominal();
                        if (newSudahBayar < 0) {
                            newSudahBayar = 0L; // Tidak boleh negatif
                        }
                        utang.setSudahDibayar(newSudahBayar);

                        // Update status
                        if (utang.getSisaTagihan() <= 0) {
                            utang.setStatus("Lunas");
                        } else {
                            utang.setStatus("Belum Lunas");
                        }

                        utangPiutangRepository.save(utang);
                    }
                }
            }
        }

        transaksiRepository.deleteById(id);
        return ResponseEntity.ok("Transaksi berhasil dihapus!");
    }
}