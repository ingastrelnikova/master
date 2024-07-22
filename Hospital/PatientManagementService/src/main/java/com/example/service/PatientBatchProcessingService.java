package com.example.service;

import com.example.client.AnonymizationClient;
import com.example.dto.PatientDto;
import com.example.entity.Patient;
import com.example.mapper.PatientMapper;
import com.example.repository.PatientRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PatientBatchProcessingService {
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private AnonymizationClient anonymizationClient;

    @Scheduled(fixedDelay = 10000) // check every 10 seconds
    public void processAndSendForAnonymization() {
        List<Patient> patientsToAnonymize = patientRepository.findByAnonymizedFalse();
        // 50 as threshold to test
        if (patientsToAnonymize.size() >= 50) {
            try {
                List<PatientDto> p = PatientMapper.INSTANCE.patientsToPatientDtos(patientsToAnonymize);
                List<PatientDto> result = this.anonymizationClient.anonymizePatients(PatientMapper.INSTANCE.patientsToPatientDtos(patientsToAnonymize));

                if (result != null && !result.isEmpty()) {
                    this.markPatientsAsAnonymized(patientsToAnonymize);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("Not enough patients to anonymize: " + patientsToAnonymize.size());
        }
    }

    @Transactional
    public void markPatientsAsAnonymized(List<Patient> patients) {
        for (Patient patient : patients) {
            Patient managedPatient = patientRepository.findById(patient.getId()).orElseThrow(() -> new RuntimeException("Patient not found: " + patient.getId()));
            managedPatient.setAnonymized(true);
            patientRepository.save(managedPatient);
        }
    }
}


