package com.zamagi.kas.repository;

import com.zamagi.kas.model.MasterAset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MasterAsetRepository extends JpaRepository<MasterAset, Long> {
    List<MasterAset> findByUserUsernameOrderByUrutanAscNamaAsc(String username);
    Optional<MasterAset> findByUserUsernameAndNamaIgnoreCase(String username, String nama);
    boolean existsByUserUsernameAndNamaIgnoreCaseAndIdNot(String username, String nama, Long id);
}