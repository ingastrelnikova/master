package com.example.controller;

import com.example.dto.PatientDto;
import com.example.entity.Patient;
import com.example.mapper.PatientMapper;
import com.example.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping("/createPatients")
    public ResponseEntity<List<PatientDto>> createPatients(@RequestBody List<PatientDto> patientDtos) {
        List<Patient> patients = patientDtos.stream()
                .map(PatientMapper.INSTANCE::dtoToPatient)
                .collect(Collectors.toList());
        List<Patient> savedPatients = patientService.savePatients(patients);
        List<PatientDto> savedPatientDtos = savedPatients.stream()
                .map(PatientMapper.INSTANCE::patientToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(savedPatientDtos, HttpStatus.CREATED);
    }

    @PostMapping("/createPatient")
    public ResponseEntity<PatientDto> createPatient(@RequestBody PatientDto patientDto) {
        Patient patient = PatientMapper.INSTANCE.dtoToPatient(patientDto);
        Patient savedPatient = patientService.savePatient(patient);
        PatientDto savedPatientDto = PatientMapper.INSTANCE.patientToDto(savedPatient);
        return new ResponseEntity<>(savedPatientDto, HttpStatus.CREATED);
    }

    @PostMapping("/deletePatientsByIds")
    public ResponseEntity<Void> deletePatientsByIds(@RequestBody List<Long> ids) {
        patientService.deletePatients(ids);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/getPatients")
    public ResponseEntity<List<PatientDto>> getPatients() {
        List<Patient> patients = patientService.getAllPatients();
        List<PatientDto> patientDtos = patients.stream()
                .map(PatientMapper.INSTANCE::patientToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(patientDtos, HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        List<PatientDto> patientDtos = patientService.getAllPatientDtos();
        return new ResponseEntity<>(patientDtos, HttpStatus.OK);
    }
}
