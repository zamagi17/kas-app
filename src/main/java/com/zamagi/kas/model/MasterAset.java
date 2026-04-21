package com.zamagi.kas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "master_aset")
public class MasterAset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nama;

    @Column(nullable = false)
    private Boolean isAktif = true;

    @Column(nullable = false)
    private Integer urutan = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public MasterAset() {}

    public Long getId() { return id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public Boolean getIsAktif() { return isAktif; }
    public void setIsAktif(Boolean isAktif) { this.isAktif = isAktif; }

    public Integer getUrutan() { return urutan; }
    public void setUrutan(Integer urutan) { this.urutan = urutan; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}