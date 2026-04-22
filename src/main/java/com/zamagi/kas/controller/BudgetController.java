package com.zamagi.kas.controller;

import com.zamagi.kas.model.Budget;
import com.zamagi.kas.repository.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    // GET /api/budget?bulan=2025-07
    @GetMapping
    public ResponseEntity<List<Budget>> getAll(
            Authentication auth,
            @RequestParam(defaultValue = "") String bulan) {

        String userId = auth.getName();
        List<Budget> list;

        if (bulan.isBlank()) {
            // Kembalikan semua budget user jika bulan tidak diisi
            list = budgetRepository.findAll().stream()
                    .filter(b -> b.getUserId().equals(userId))
                    .toList();
        } else {
            list = budgetRepository.findByUserIdAndBulan(userId, bulan);
        }
        return ResponseEntity.ok(list);
    }

    // POST /api/budget
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Budget budget, Authentication auth) {
        String userId = auth.getName();

        // Cek duplikat kategori di bulan yang sama
        if (budgetRepository.findByUserIdAndKategoriAndBulan(
                userId, budget.getKategori(), budget.getBulan()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Budget untuk kategori ini di bulan tersebut sudah ada.");
        }

        budget.setUserId(userId);
        return ResponseEntity.ok(budgetRepository.save(budget));
    }

    // PUT /api/budget/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Budget updated,
            Authentication auth) {

        return budgetRepository.findById(id)
                .filter(b -> b.getUserId().equals(auth.getName()))
                .map(b -> {
                    b.setLimitBulan(updated.getLimitBulan());
                    b.setCatatan(updated.getCatatan());
                    return ResponseEntity.ok(budgetRepository.save(b));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/budget/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        return budgetRepository.findById(id)
                .filter(b -> b.getUserId().equals(auth.getName()))
                .map(b -> {
                    budgetRepository.delete(b);
                    return ResponseEntity.ok(Map.of("message", "Budget dihapus"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
