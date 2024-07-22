package com.example.client;

import com.example.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import com.example.dto.PatientDto;

// change if anonymization service finally works with docker
@FeignClient(name="anonymization-service", url = "http://anonymization-service:8081", configuration = FeignConfig.class)
public interface AnonymizationClient {

    @PostMapping("/anonymize/patients")
    List<PatientDto> anonymizePatients(@RequestBody List<PatientDto> patients);
    @DeleteMapping("/anonymize/deletePatients")
    void deleteAnonymizedPatients(@RequestBody List<Long> patientIds);
}
