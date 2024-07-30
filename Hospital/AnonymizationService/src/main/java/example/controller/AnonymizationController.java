package example.controller;

import example.dto.AnonymizedPatientDto;
import example.dto.PatientDto;
import example.service.AnonymizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/anonymize")
public class AnonymizationController {

    @Autowired
    private AnonymizationService anonymizationService;

    @PostMapping("/patients")
    public ResponseEntity<List<AnonymizedPatientDto>> anonymizePatients(@RequestBody List<PatientDto> patientDtos) {
        List<AnonymizedPatientDto> anonymizedPatients = this.anonymizationService.anonymizePatients(patientDtos);
        if (anonymizedPatients.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(anonymizedPatients, HttpStatus.OK);
    }

    @DeleteMapping("/deletePatients")
    public void deleteAnonymizedPatients(@RequestBody List<Long> patientIds) {
        System.out.println("we are here");
        anonymizationService.deletePatientsByIds(patientIds);
    }
}
