package com.zamagi.kas.dto;

public class LaporanSummary {

    private long totalMasuk;
    private long totalKeluar;

    public LaporanSummary(long totalMasuk, long totalKeluar) {
        this.totalMasuk = totalMasuk;
        this.totalKeluar = totalKeluar;
    }

    public long getTotalMasuk() {
        return totalMasuk;
    }

    public long getTotalKeluar() {
        return totalKeluar;
    }
}