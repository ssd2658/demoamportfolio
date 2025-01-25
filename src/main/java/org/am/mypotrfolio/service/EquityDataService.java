package org.am.mypotrfolio.service;

import jakarta.persistence.EntityManager;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.dto.EquityDataDTO;
import org.am.mypotrfolio.entity.EquityDataEntity;
import org.am.mypotrfolio.mapper.CompanyMapper;
import org.am.mypotrfolio.repo.EquityDataRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquityDataService {

    private final EquityDataRepository equityDataRepository;
    
    public List<EquityDataDTO> processExcelFile(MultipartFile file) throws IOException {
        List<EquityDataDTO> equityDataList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            int currentRowNum = 0;
            for (Row row : sheet) {
                currentRowNum ++;
                if (row.getRowNum() == 0) continue;
                if (row.getCell(0).getStringCellValue().endsWith(".BO")) continue;//
                var equityData = getEquityData(row, currentRowNum);
                equityDataList.add(equityData);
            }
        }
    
        saveEquityData(equityDataList);
        return equityDataList;
    }
    
    public List<EquityDataDTO> processCsvFile(MultipartFile file) throws IOException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            
            val data = rows.stream()
                .skip(1) // Skip header
                .map(row -> EquityDataDTO.builder()
                    .symbol(row[0])
                    .isin(row[6])
                    .name(row[1])
                    .series(row[2])
                    .faceValue(parseDouble(row[4]))
                    .build())
                .collect(Collectors.toList());

            saveEquityData(data);
            return data;
        } catch (CsvException e) {
            throw new RuntimeException("Failed to process CSV file", e);
        }
    }
    
    @Transactional
    private void saveEquityData(List<EquityDataDTO> equityDataList) {
        log.info("Starting to save {} equity records", equityDataList.size());
        
        List<EquityDataEntity> entities = equityDataList.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());

        saveEquityEntities(entities);
    }

    private EquityDataDTO getEquityData(Row stock, int currentRowNum) {
        System.out.println("Current row num: " + currentRowNum);
        var equityData =  EquityDataDTO.builder()
                .symbol(stock.getCell(0).getStringCellValue().replace(".NS", ""))
                .name(stock.getCell(15) == null ? "Not Found" : stock.getCell(8).getStringCellValue())
                .marketCap(stock.getCell(4).getNumericCellValue())
                .industry(stock.getCell(15) == null ? "Others" : stock.getCell(15).getStringCellValue())
                .isin(stock.getCell(11) != null ? stock.getCell(11).getStringCellValue() : "XXXXXX"+currentRowNum)
                .build();
        return equityData;
    }
    
    private EquityDataEntity convertToEntity(EquityDataDTO dto) {
        EquityDataEntity entity = new EquityDataEntity();
        entity.setSymbol(dto.getSymbol());
        entity.setName(dto.getName());
        entity.setSeries(dto.getSeries());
        entity.setIsin(dto.getIsin());
        entity.setFaceValue(dto.getFaceValue());
        entity.setIndustry(dto.getIndustry());
        entity.setInstrumentType(dto.getInstrumentType());
        return entity;
    }
    
    private void saveEquityEntities(List<EquityDataEntity> equityDataList) {
        if (equityDataList == null || equityDataList.isEmpty()) {
            return;
        }

        List<EquityDataEntity> uniqueEntities = new ArrayList<>();
        Set<String> processedIsins = new HashSet<>();

        for (EquityDataEntity entity : equityDataList) {
            // Skip if ISIN is null or already processed
            if (entity.getIsin() == null || processedIsins.contains(entity.getIsin())) {
                continue;
            }

            // Check if entity with this ISIN already exists in the database
            Optional<EquityDataEntity> existingEntity = equityDataRepository.findByIsin(entity.getIsin());
            
            if (existingEntity.isEmpty()) {
                uniqueEntities.add(entity);
                processedIsins.add(entity.getIsin());
            } else {
                // Optional: Log skipped duplicate
                log.info("Skipping duplicate ISIN: {}", entity.getIsin());
            }
        }

        // Batch save unique entities
        if (!uniqueEntities.isEmpty()) {
            equityDataRepository.saveAll(uniqueEntities);
        }
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            default: return null;
        }
    }
    
    private Double getNumericCellValue(Cell cell) {
        if (cell == null) return null;
        return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : null;
    }
    
    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
} 