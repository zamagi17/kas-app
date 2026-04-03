package com.zamagi.kas.repository;

import com.zamagi.kas.model.UtangPiutang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UtangPiutangRepository extends JpaRepository<UtangPiutang, Long> {
    List<UtangPiutang> findByUserUsernameOrderByStatusAscJatuhTempoAsc(String username);
}