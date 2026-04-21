package com.zamagi.kas.repository;

import com.zamagi.kas.model.MasterKategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MasterKategoriRepository extends JpaRepository<MasterKategori, Long> {
    List<MasterKategori> findByUserUsernameOrderByNamaAsc(String username);
    boolean existsByUserUsernameAndNamaIgnoreCase(String username, String nama);
    boolean existsByUserUsernameAndNamaIgnoreCaseAndIdNot(String username, String nama, Long id);
}