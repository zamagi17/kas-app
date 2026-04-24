package com.zamagi.kas.repository;

import com.zamagi.kas.model.Transaksi;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaksiRepository extends JpaRepository<Transaksi, Long> {
    // Spring Data JPA otomatis membuatkan fungsi save(), findAll(), deleteById(), dll!
    List<Transaksi> findByUserUsernameOrderByTanggalDescIdDesc(String username);
    
    long countByUserUsername(String username);
    
    // Hapus transaksi berdasarkan pola keterangan (untuk cascade delete saat UtangPiutang dihapus)
    @Modifying
    @Query("DELETE FROM Transaksi t WHERE t.keterangan LIKE CONCAT('%', ?1, '%')")
    void deleteByKeteranganContaining(String pattern);
    
    List<Transaksi> findByUserUsernameAndTanggalBetweenOrderByTanggalDescIdDesc(
        String username, 
        LocalDate startDate, 
        LocalDate endDate
    );

    /**
     * Menghitung Saldo Awal (sebelum tanggal 1 pada bulan yang difilter).
     * Hanya menghitung jenis Pemasukan dan Pengeluaran riil.
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.jenis = 'Pemasukan' THEN t.nominal ELSE -t.nominal END), 0) " +
           "FROM Transaksi t WHERE t.user.username = :username AND t.tanggal < :startDate AND t.jenis IN ('Pemasukan', 'Pengeluaran')")
    Long calculateSaldoAwal(@Param("username") String username, @Param("startDate") LocalDate startDate);
    
    /**
     * Menghitung Total Saldo Portofolio per Aset/Dompet (sejak awal sampai akhir bulan yang difilter).
     * Memperhitungkan efek dari utang masuk, bayar utang, piutang keluar, dan terima piutang.
     */
    @Query("SELECT t.sumberDana, SUM(CASE WHEN t.jenis IN ('Pemasukan', 'Utang Masuk', 'Terima Piutang') THEN t.nominal " +
           "WHEN t.jenis IN ('Pengeluaran', 'Piutang Keluar', 'Bayar Utang') THEN -t.nominal ELSE 0 END) " +
           "FROM Transaksi t WHERE t.user.username = :username AND t.tanggal <= :endDate " +
           "GROUP BY t.sumberDana")
    List<Object[]> getPortofolioSaldo(@Param("username") String username, @Param("endDate") LocalDate endDate);
   
}