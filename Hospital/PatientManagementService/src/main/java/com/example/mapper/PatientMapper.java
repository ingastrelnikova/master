package com.example.mapper;

import com.example.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.example.dto.PatientDto;

import java.util.List;

@Mapper
public interface PatientMapper {
    PatientMapper INSTANCE = Mappers.getMapper(PatientMapper.class);
    PatientDto patientToDto(Patient patient);
    Patient dtoToPatient(PatientDto patientDto);
    List<PatientDto> patientsToPatientDtos(List<Patient> patients);
}

