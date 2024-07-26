package com.example.service;

import com.example.client.AnonymizationClient;
import com.example.dto.PatientDto;
import com.example.entity.Patient;
import com.example.mapper.PatientMapper;
import com.example.repository.PatientRepository;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AnonymizationClient anonymizationClient;

    @Autowired
    private PatientBatchProcessingService patientBatchProcessingService;

    private static final int MAX_RETRIES = 3;
    private final Object lock = new Object();

    @Transactional
    public List<Patient> savePatients(List<Patient> patients) {
        return retryOnOptimisticLock(() -> {
            try {
                List<Patient> savedPatients = patients.stream()
                        .map(this::saveOrUpdatePatient)
                        .collect(Collectors.toList());
                if (!savedPatients.isEmpty() && savedPatients.size() >= 50) {
                    this.sendForAnonymization(savedPatients);
                }
                return savedPatients;
            } catch (DataIntegrityViolationException e) {
                throw new RuntimeException("Data integrity error: " + e.getMessage(), e);
            }
        });
    }

    public void sendForAnonymization(List<Patient> patients) {
        try {
            List<PatientDto> result = this.anonymizationClient.anonymizePatients(PatientMapper.INSTANCE.patientsToPatientDtos(patients));

            if (result != null && !result.isEmpty()) {
                this.patientBatchProcessingService.markPatientsAsAnonymized(patients);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Transactional
    public Patient savePatient(Patient patient) {
        return retryOnOptimisticLock(() -> {
            try {
                return saveOrUpdatePatient(patient);
            } catch (DataIntegrityViolationException e) {
                throw new RuntimeException("Data integrity error: " + e.getMessage(), e);
            }
        });
    }

    @Transactional
    public void deletePatients(List<Long> patientIds) {
        retryOnOptimisticLock(() -> {
            synchronized (lock) {
                for (Long id : patientIds) {
                    Optional<Patient> existingPatientOpt = patientRepository.findById(id);
                    if (existingPatientOpt.isPresent()) {
                        Patient existingPatient = existingPatientOpt.get();
                        patientRepository.delete(existingPatient);

                    } else {
                        throw new EntityNotFoundException("Patient with ID " + id + " not found");
                    }
                }

                // only anonymized patients to delete, the others are not even there yet
                List<Long> anonymizedPatientIds = patientIds.stream()
                        .map(patientRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(Patient::getAnonymized)
                        .map(Patient::getId)
                        .collect(Collectors.toList());

                if (!anonymizedPatientIds.isEmpty()) {
                    anonymizationClient.deleteAnonymizedPatients(anonymizedPatientIds);
                }
            }
            return null;
        });
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public boolean existsById(Long id) {
        return patientRepository.existsById(id);
    }

    public List<PatientDto> getAllPatientDtos() {
        List<Patient> patients = getAllPatients();
        return patients.stream()
                .map(PatientMapper.INSTANCE::patientToDto)
                .collect(Collectors.toList());
    }

    private Patient saveOrUpdatePatient(Patient patient) {
        if (patient.getVersion() == null) {
            patient.setVersion(0);
        }
        Optional<Patient> existingPatientOpt = patientRepository.findById(patient.getId());
        if (existingPatientOpt.isPresent()) {
            Patient existingPatient = existingPatientOpt.get();
            existingPatient.setName(patient.getName());
            existingPatient.setDateOfBirth(patient.getDateOfBirth());
            existingPatient.setZipCode(patient.getZipCode());
            existingPatient.setGender(patient.getGender());
            existingPatient.setDisease(patient.getDisease());
            existingPatient.setAnonymized(patient.getAnonymized());
            existingPatient.setCreatedAt(patient.getCreatedAt());
            // createdAt is not updated to preserve the original creation time
            return patientRepository.save(existingPatient);
        } else {
            return patientRepository.save(patient);
        }
    }

    private <T> T retryOnOptimisticLock(RetryableOperation<T> operation) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return operation.execute();
            } catch (ObjectOptimisticLockingFailureException | StaleStateException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("Optimistic lock error after " + MAX_RETRIES + " retries: " + e.getMessage(), e);
                }
            }
        }
        return null; // This should never be reached
    }

    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute();
    }
}
