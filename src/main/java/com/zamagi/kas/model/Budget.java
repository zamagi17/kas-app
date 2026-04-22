package com.zamagi.kas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "kategori", "bulan"}))
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String kategori;

    @Column(nullable = false)
    private Long limitBulan;   // dalam rupiah

    @Column(nullable = false, length = 7) // format: "2025-07"
    private String bulan;

    private String catatan;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public Long getLimitBulan() {
        return limitBulan;
    }

    public void setLimitBulan(Long limitBulan) {
        this.limitBulan = limitBulan;
    }

    public String getBulan() {
        return bulan;
    }

    public void setBulan(String bulan) {
        this.bulan = bulan;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
