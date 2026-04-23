package com.zamagi.kas.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tabungan")
public class Tabungan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String nama;

    @Column(name = "target_nominal", nullable = false)
    private Double targetNominal;

    @Column(nullable = false)
    private Double terkumpul;

    @Column(nullable = false)
    private LocalDate deadline;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Constructor kosong (wajib untuk JPA)
    public Tabungan() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public Double getTargetNominal() { return targetNominal; }
    public void setTargetNominal(Double targetNominal) { this.targetNominal = targetNominal; }

    public Double getTerkumpul() { return terkumpul; }
    public void setTerkumpul(Double terkumpul) { this.terkumpul = terkumpul; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}