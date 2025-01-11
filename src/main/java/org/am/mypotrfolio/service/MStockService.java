package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.DhanStockPortfolio;
import org.am.mypotrfolio.domain.MStockPortfolio;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.mapper.PortfolioMapper;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.utils.NseFileBuilder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.stream.Collectors;

@Service("MStock")
@ComponentScan
@RequiredArgsConstructor
public class MStockService implements PortfolioService {

    private final NseFileBuilder nseFileBuilder;
    private final ResourceLoader resourceLoader;
    private final NseStockRepository nseStockRepository;

    @Override
    @SneakyThrows
    public List<NseStock> processNseStock(MultipartFile file){
         List<Map<String, String>> fileJson = nseFileBuilder.parseExcel(file, "MStock");
         ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(fileJson);

        List<MStockPortfolio> stocks = objectMapper.readValue(payload, new TypeReference<List<MStockPortfolio>>() {});
    

        List<NseStock> nseStocks = stocks.stream().map(stock -> {
            var mStock = PortfolioMapper.INSTANCE.toNseStock(stock);
            var nseEntity = NseStockMapper.INSTANCE.mapNseStockEntity(mStock);
            nseEntity.setBrokerPlatform("MStock");
            nseEntity.setUserId("MUNISH");
            nseStockRepository.save(nseEntity);
            return mStock;
        }).collect(Collectors.toList());
        return nseStocks;
    }
}
