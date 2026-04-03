package com.zamagi.kas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "utang_piutang")
public class UtangPiutang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String jenis; // "Utang" atau "Piutang"

    @Column(nullable = false)
    private String namaPihak; // Nama orang

    @Column(nullable = false)
    private Long nominalAwal; // Total utang/piutang awal

    @Column(nullable = false)
    private Long sudahDibayar; // Akumulasi pembayaran

    private String asetTerkait; // "BCA", "Dompet Tunai", dll

    @Column(nullable = false)
    private LocalDate tanggalMulai;

    private LocalDate jatuhTempo; // Boleh kosong

    private String keterangan;

    @Column(nullable = false)
    private String status; // "Belum Lunas" atau "Lunas"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    // Constructor kosong
    public UtangPiutang() {
        this.sudahDibayar = 0L;
        this.status = "Belum Lunas";
    }

    // Getter & Setter
    public Long getId() { return id; }

    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }

    public String getNamaPihak() { return namaPihak; }
    public void setNamaPihak(String namaPihak) { this.namaPihak = namaPihak; }

    public Long getNominalAwal() { return nominalAwal; }
    public void setNominalAwal(Long nominalAwal) { this.nominalAwal = nominalAwal; }

    public Long getSudahDibayar() { return sudahDibayar; }
    public void setSudahDibayar(Long sudahDibayar) { this.sudahDibayar = sudahDibayar; }

    public Long getSisaTagihan() { return nominalAwal - sudahDibayar; } // Kalkulasi otomatis

    public String getAsetTerkait() { return asetTerkait; }
    public void setAsetTerkait(String asetTerkait) { this.asetTerkait = asetTerkait; }

    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(LocalDate tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public LocalDate getJatuhTempo() { return jatuhTempo; }
    public void setJatuhTempo(LocalDate jatuhTempo) { this.jatuhTempo = jatuhTempo; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}