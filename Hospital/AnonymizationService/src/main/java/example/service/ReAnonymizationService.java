package example.service;

import example.dto.AnonymizedPatientDto;
import example.dto.PatientDto;
import example.repository.AnonymizedPatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//import javax.transaction.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ReAnonymizationService {

    @Autowired
    private AnonymizedPatientRepository anonymizedPatientRepository;
    @Autowired
    private example.service.PrometheusClient prometheusClient;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private AnonymizationService anonymizationService;
    @Value("${anonymization.desiredKAnonymity}")
    private double desiredKAnonymity;
    @Value("${anonymization.desiredMDTD}")
    private double desiredMDTD;
    @Value("${patient.management.service.url}")
    private String patientManagementServiceUrl;

    private double currentKAnonymity = 0.0;
    private double currentMDTD = 0.0;


    @Scheduled(fixedRate = 30000)  // check metrics every 30 seconds
    public void checkMetrics() {
        try {
            currentKAnonymity = this.prometheusClient.queryMetric("k_anonymity");
            currentMDTD = this.prometheusClient.queryMetric("max_deletions_to_degrade");
            System.out.println("Updated Metrics: k-anonymity: " + currentKAnonymity + ", max_deletions_to_degrade:" + currentMDTD);

            if (currentKAnonymity < desiredKAnonymity || currentMDTD < desiredMDTD) {
                this.reanonymizeDataset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error: "+e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " +e.getMessage());
        }
    }

    private void reanonymizeDataset() {
        List<PatientDto> patients = fetchPatientData();
        if (!patients.isEmpty()) {
            List<AnonymizedPatientDto> newAnonymizedPatients = anonymizationService.anonymizePatients(patients, (int) desiredKAnonymity);
            this.replaceAnonymizedData(newAnonymizedPatients);
        }
    }

    private List<PatientDto> fetchPatientData() {
        ResponseEntity<PatientDto[]> response = restTemplate.getForEntity(patientManagementServiceUrl + "/patients/getAll", PatientDto[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Arrays.asList(Objects.requireNonNull(response.getBody()));
        } else {
            throw new RuntimeException("Failed to get data from patient management service");
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public synchronized void replaceAnonymizedData(List<AnonymizedPatientDto> newAnonymizedPatients) {
        this.anonymizedPatientRepository.deleteAll();
        this.anonymizationService.saveAnonymizedPatients(newAnonymizedPatients);
    }
}
