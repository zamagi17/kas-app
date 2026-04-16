package com.zamagi.kas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "users_id_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    // Menyimpan daftar aset dompet harian sebagai JSON string
    // Contoh: ["BCA","SeaBank","Dompet Tunai"]
    @Column(name = "dompet_harian", columnDefinition = "TEXT")
    private String dompetHarian;

    // --- Constructor Kosong ---
    public User() {
    }

    // --- Getter dan Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDompetHarian() { return dompetHarian; }
    public void setDompetHarian(String dompetHarian) { this.dompetHarian = dompetHarian; }
}