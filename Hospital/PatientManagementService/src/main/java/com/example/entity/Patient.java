package com.example.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String zipCode;
    private String gender;
    private String disease;
    @Version
    private Integer version = 0;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private boolean anonymized = false;

    public boolean getAnonymized() {
        return anonymized;
    }

    public void setAnonymized(boolean anonymized) {
        this.anonymized = anonymized;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isAnonymized() {
        return anonymized;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGender() {
        return gender;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getDisease() {
        return disease;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }
}
