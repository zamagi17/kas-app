package com.zamagi.kas.repository;

import com.zamagi.kas.model.Transaksi;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    // Spring Data JPA otomatis membuatkan fungsi save(), findAll(), deleteById(), dll!
    List<Transaksi> findByUserUsernameOrderByTanggalDescIdDesc(String username);
    
    // Hapus transaksi berdasarkan pola keterangan (untuk cascade delete saat UtangPiutang dihapus)
    @Modifying
    @Query("DELETE FROM Transaksi t WHERE t.keterangan LIKE CONCAT('%', ?1, '%')")
    void deleteByKeteranganContaining(String pattern);
}