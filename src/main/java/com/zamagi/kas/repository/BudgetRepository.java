package com.zamagi.kas.repository;

import com.zamagi.kas.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndBulan(String userId, String bulan);

    Optional<Budget> findByUserIdAndKategoriAndBulan(String userId, String kategori, String bulan);
}
