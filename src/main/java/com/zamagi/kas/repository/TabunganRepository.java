package com.zamagi.kas.repository;

import com.zamagi.kas.model.Tabungan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TabunganRepository extends JpaRepository<Tabungan, Long> {
    // Mengambil daftar tabungan milik user tertentu
    List<Tabungan> findByUserId(Long userId);
}