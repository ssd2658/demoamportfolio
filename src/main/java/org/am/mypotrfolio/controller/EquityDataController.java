package org.am.mypotrfolio.controller;

import lombok.RequiredArgsConstructor;

import org.am.mypotrfolio.dto.EquityDataDTO;
import org.am.mypotrfolio.dto.NseSecurityDto;
import org.am.mypotrfolio.service.EquityDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EquityDataController {
    
    private final EquityDataService equityDataService;
    

    @PostMapping("/upload/nsedata/excel")
    public ResponseEntity<List<EquityDataDTO>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<EquityDataDTO> processedData = equityDataService.processExcelFile(file);
            return ResponseEntity.ok(processedData);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload/nsesecurity/excel")
    public ResponseEntity<String> uploadNseSecurityExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<List<NseSecurityDto>> processedBatches = equityDataService.processNseSecurityExcelFile(file);
            return ResponseEntity.ok(String.format("Successfully processed %d batches of NSE security data", processedBatches.size()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/csv")
    public ResponseEntity<List<EquityDataDTO>> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            List<EquityDataDTO> processedData = equityDataService.processCsvFile(file);
            return ResponseEntity.ok(processedData);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 