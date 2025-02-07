package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.DhanStockPortfolio;
import org.am.mypotrfolio.domain.MStockPortfolio;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.mapper.PortfolioMapper;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.utils.IsinLookupUtil;
import org.am.mypotrfolio.utils.NseFileBuilder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service("MStock")
@ComponentScan
@RequiredArgsConstructor
public class MStockService implements PortfolioService {

    private final NseFileBuilder nseFileBuilder;
    private final ResourceLoader resourceLoader;
    private final IsinLookupUtil isinLookupUtil;
    private final NseStockRepository nseStockRepository;
    private static final Logger log = LoggerFactory.getLogger(MStockService.class);

    @Override
    @SneakyThrows
    public List<NseStock> processNseStock(MultipartFile file){
         List<Map<String, String>> fileJson = nseFileBuilder.parseExcel(file, "MStock");
         ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String payload = objectMapper.writeValueAsString(fileJson);

        List<MStockPortfolio> stocks = objectMapper.readValue(payload, new TypeReference<List<MStockPortfolio>>() {});
        List<NseStock> nseStocks = new ArrayList<>();
        List<NseStockEntity> batchEntities = new ArrayList<>();
        List<String> unprocessedStocks = new ArrayList<>();

        int batchSize = 10;
        AtomicInteger totalProcessed = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger currentIndex = new AtomicInteger(0);

        log.info("Starting to process {} stocks in batches of {}", stocks.size(), batchSize);

        for (MStockPortfolio stock : stocks) {
            currentIndex.incrementAndGet();
            try {
                if (stock.getIsin() == null || stock.getIsin().trim().isEmpty()) {
                    var foundIsinData = isinLookupUtil.findIsinBySecurityName(stock.getSymbol());
                    if (foundIsinData.isPresent()) {
                        var securityData = foundIsinData.get();
                        stock.setIsin(securityData.get("isin"));
                        stock.setSecurityId(securityData.get("security_id"));
                    } else {
                        unprocessedStocks.add(String.format("Stock: %s - Reason: Missing ISIN (Position: %d/%d)", 
                            stock.getSymbol(), currentIndex.get(), stocks.size()));
                        failureCount.incrementAndGet();
                        continue;
                    }
                }
                
                var mStock = PortfolioMapper.INSTANCE.toNseStock(stock);
                var nseEntity = NseStockMapper.INSTANCE.mapNseStockEntity(mStock);
                nseEntity.setBrokerPlatform("MStock");
                nseEntity.setUserId("MKU257");
                
                batchEntities.add(nseEntity);
                nseStocks.add(mStock);

                // Process batch when size reaches batchSize or it's the last stock
                if (batchEntities.size() >= batchSize || currentIndex.get() == stocks.size()) {
                    try {
                        nseStockRepository.saveAll(batchEntities);
                        successCount.addAndGet(batchEntities.size());
                        totalProcessed.addAndGet(batchEntities.size());
                    } catch (Exception e) {
                        // Individual retry for failed batch
                        for (NseStockEntity entity : batchEntities) {
                            try {
                                nseStockRepository.save(entity);
                                successCount.incrementAndGet();
                                totalProcessed.incrementAndGet();
                            } catch (Exception retryEx) {
                                unprocessedStocks.add(String.format("Stock: %s - Reason: Save failed - %s (Position: %d/%d)", 
                                    entity.getSymbol(), retryEx.getMessage(), 
                                    currentIndex.get() - batchEntities.size() + (batchEntities.indexOf(entity) + 1), 
                                    stocks.size()));
                                failureCount.incrementAndGet();
                            }
                        }
                    }
                    batchEntities.clear();
                }

            } catch (Exception e) {
                unprocessedStocks.add(String.format("Stock: %s - Reason: %s (Position: %d/%d)", 
                    stock.getSymbol(), e.getMessage(), currentIndex.get(), stocks.size()));
                failureCount.incrementAndGet();
            }
        }

        // Log summary and unprocessed stocks
        int unaccountedStocks = stocks.size() - (totalProcessed.get() + failureCount.get());
        
        log.info("Processing summary:");
        log.info("Total stocks: {} | Processed: {} | Failed: {} | Unaccounted: {}", 
            stocks.size(), successCount.get(), failureCount.get(), unaccountedStocks);

        if (!unprocessedStocks.isEmpty() || unaccountedStocks > 0) {
            log.error("Failed to process the following stocks:");
            unprocessedStocks.forEach(stock -> log.error(stock));
            
            if (unaccountedStocks > 0) {
                log.error("Additionally, {} stocks were unaccounted for in the processing", unaccountedStocks);
            }
        }

        return nseStocks;
    }
}
