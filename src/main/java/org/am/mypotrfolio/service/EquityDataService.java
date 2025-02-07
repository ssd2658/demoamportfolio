package org.am.mypotrfolio.service;

import jakarta.persistence.EntityManager;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.dto.EquityDataDTO;
import org.am.mypotrfolio.dto.NseSecurityDto;
import org.am.mypotrfolio.entity.EquityDataEntity;
import org.am.mypotrfolio.entity.NseSecurityEntity;
import org.am.mypotrfolio.mapper.CompanyMapper;
import org.am.mypotrfolio.repo.EquityDataRepository;
import org.am.mypotrfolio.repo.NseSecurityRepository;
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
    private final NseSecurityRepository nseSecurityRepository;
    
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

    public List<List<NseSecurityDto>> processNseSecurityExcelFile(MultipartFile file) throws IOException {
        List<List<NseSecurityDto>> batches = new ArrayList<>();
        List<NseSecurityDto> currentBatch = new ArrayList<>();
        final int BATCH_SIZE = 100;
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            int currentRowNum = 0;
            for (Row row : sheet) {
                currentRowNum++;
                if (row.getRowNum() == 0) continue;
                //if (row.getCell(0).getStringCellValue().endsWith(".BO")) continue;
                
                var securityData = getNseSecurityData(row, currentRowNum);
                if (securityData != null) {
                    currentBatch.add(securityData);
                }
                
                // When batch is full, process it and start a new batch
                if (currentBatch.size() >= BATCH_SIZE) {
                    saveNseSecurityData(currentBatch);
                    batches.add(new ArrayList<>(currentBatch));
                    currentBatch.clear();
                }
            }
        }
        
        // Process any remaining records in the last batch
        if (!currentBatch.isEmpty()) {
            saveNseSecurityData(currentBatch);
            batches.add(currentBatch);
        }
        
        log.info("Processed total {} batches with approximately {} records", 
            batches.size(), batches.size() * BATCH_SIZE);
        return batches;
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
    private void saveNseSecurityData(List<NseSecurityDto> batchData) {
        log.info("Processing batch of {} records", batchData.size());
        
        List<NseSecurityEntity> entities = batchData.stream()
            .map(this::convertToNseEntity)
            .collect(Collectors.toList());

        saveNseSecurityEntities(entities);
    }

    private NseSecurityEntity convertToNseEntity(NseSecurityDto dto) {
        NseSecurityEntity entity = new NseSecurityEntity();
        entity.setSecurityId(dto.getSecurityId());
        entity.setSecurityName(dto.getSecurityName());
        entity.setStatus(dto.getStatus());
        entity.setSeries(dto.getSeries());
        entity.setIsin(dto.getIsin());
        entity.setFaceValue(dto.getFaceValue());
        entity.setIndustry(dto.getIndustry());
        entity.setInstrumentType(dto.getInstrumentType());
        entity.setSectorName(dto.getSectorName());
        entity.setIndustryNewName(dto.getIndustryNewName());
        entity.setIGroupName(dto.getIndustryGroupName());
        entity.setISubGroupName(dto.getIndustrySubGroupName());
        return entity;
    }

    @Transactional
    private void saveEquityData(List<EquityDataDTO> equityDataList) {
        log.info("Starting to save {} equity records", equityDataList.size());
        
        List<EquityDataEntity> entities = equityDataList.stream()
            .map(this::convertToEntity)
            .collect(Collectors.toList());

        saveEquityEntities(entities);
    }

    private NseSecurityDto getNseSecurityData(Row stock, int currentRowNum) {
        try {
            return NseSecurityDto.builder()
                    .securityId(getCellStringValue(stock.getCell(2), "SECURITY_ID", currentRowNum))
                    .securityName(getCellStringValue(stock.getCell(3), "SECURITY_NAME", currentRowNum))
                    .sectorName(getCellStringValue(stock.getCell(10), "SECTOR_NAME", currentRowNum))
                    .faceValue(stock.getCell(6) == null ? 0 : getNumericCellValue(stock.getCell(6)))
                    .industry(getCellStringValue(stock.getCell(8), "INDUSTRY", currentRowNum, "Others"))
                    .isin(getCellStringValue(stock.getCell(7), "ISIN", currentRowNum, "XXXXXX" + currentRowNum))
                    .instrumentType(getCellStringValue(stock.getCell(9), "INSTRUMENT_TYPE", currentRowNum, "Others"))
                    .industryNewName(getCellStringValue(stock.getCell(10), "INDUSTRY_NEW_NAME", currentRowNum, "Others"))
                    .industryGroupName(getCellStringValue(stock.getCell(11), "INDUSTRY_GROUP_NAME", currentRowNum, "Others"))
                    .industrySubGroupName(getCellStringValue(stock.getCell(12), "INDUSTRY_SUB_GROUP_NAME", currentRowNum, "Others"))
                    .status(getCellStringValue(stock.getCell(4), "STATUS", currentRowNum, "Delisted"))
                    .build();
        } catch (Exception e) {
            log.error("Error processing row {}: {}. Error: {}", currentRowNum, e.getMessage(), e);
            return null;
        }
    }

    private String getCellStringValue(Cell cell, String fieldName, int rowNum) {
        return getCellStringValue(cell, fieldName, rowNum, null);
    }

    private String getCellStringValue(Cell cell, String fieldName, int rowNum, String defaultValue) {
        try {
            if (cell == null) {
                if (defaultValue != null) {
                    log.warn("Row {}: {} is null, using default value: {}", rowNum, fieldName, defaultValue);
                    return defaultValue;
                }
                throw new IllegalArgumentException(fieldName + " cell is null");
            }
            return cell.getStringCellValue();
        } catch (Exception e) {
            if (defaultValue != null) {
                log.warn("Row {}: Error getting {} value: {}. Using default: {}", rowNum, fieldName, e.getMessage(), defaultValue);
                return defaultValue;
            }
            throw new IllegalArgumentException("Error getting " + fieldName + " value: " + e.getMessage());
        }
    }

    private void saveNseSecurityEntities(List<NseSecurityEntity> uniqueEntities) {
        if (uniqueEntities == null || uniqueEntities.isEmpty()) {
            return;
        }

        //List<NseSecurityEntity> uniqueEntities = new ArrayList<>();
        //Set<String> processedIsins = new HashSet<>();
        final int BATCH_SIZE = 100;

        // First collect all unique entities
        // for (NseSecurityEntity entity : equityDataList) {
        //     if (entity.getIsin() == null || processedIsins.contains(entity.getIsin())) {
        //         continue;
        //     }

        //     Optional<NseSecurityEntity> existingEntity = nseSecurityRepository.findByIsin(entity.getIsin());
            
        //     if (existingEntity.isEmpty()) {
        //         uniqueEntities.add(entity);
        //         processedIsins.add(entity.getIsin());
        //     } else {
        //         log.info("Skipping duplicate ISIN: {}", entity.getIsin());
        //     }
        // }

        // Process in batches of BATCH_SIZE
        for (int i = 0; i < uniqueEntities.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, uniqueEntities.size());
            List<NseSecurityEntity> batch = uniqueEntities.subList(i, endIndex);
            
            if (!batch.isEmpty()) {
                nseSecurityRepository.saveAll(batch);
                log.info("Saved batch of {} records. Progress: {}/{}", 
                    batch.size(), endIndex, uniqueEntities.size());
            }
        }
    }

    private EquityDataDTO getEquityData(Row stock, int currentRowNum) {
        System.out.println("Current row num: " + currentRowNum);
        var equityData =  EquityDataDTO.builder()
                .symbol(stock.getCell(0).getStringCellValue().replace(".NS", ""))
                .name(stock.getCell(15) == null ? "Not Found" : stock.getCell(8).getStringCellValue())
                .marketCap(stock.getCell(4).getNumericCellValue())
                .industry(stock.getCell(15) == null ? "Others" : stock.getCell(15).getStringCellValue())
                .isin(stock.getCell(7) != null ? stock.getCell(7).getStringCellValue() : "XXXXXX"+currentRowNum)
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