package com.example.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientDto {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String zipcode;
    private String gender;
    private String disease;

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public boolean isAnonymized() {
        return anonymized;
    }

    private LocalDateTime createdAt;
    private boolean anonymized;

    public PatientDto() {}

    public PatientDto(Long id, String name, LocalDate dateOfBirth, String zipCode, String gender, String disease, LocalDateTime createdAt, boolean anonymized) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.zipcode = zipCode;
        this.gender = gender;
        this.disease = disease;
        this.createdAt = createdAt;
        this.anonymized = anonymized;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getAnonymized() {
        return anonymized;
    }

    public void setAnonymized(boolean anonymized) {
        this.anonymized = anonymized;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getZipCode() {
        return zipcode;
    }

    public void setZipCode(String zipCode) {
        this.zipcode = zipCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
