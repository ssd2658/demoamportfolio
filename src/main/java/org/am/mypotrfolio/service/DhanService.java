package org.am.mypotrfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.mapper.PortfolioMapper;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.DhanStockPortfolio;
import org.am.mypotrfolio.utils.NseFileBuilder;
import org.am.mypotrfolio.utils.ObjectUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Primary
@Service("Dhan")
@RequiredArgsConstructor
public class DhanService implements PortfolioService{
    private final NseFileBuilder nseFileBuilder;
    private final ResourceLoader resourceLoader;
    private final NseStockRepository nseStockRepository;

    @Override
    @SneakyThrows
    public List<NseStock> processNseStock(MultipartFile file){
         List<Map<String, String>> fileJson = nseFileBuilder.parseExcel(file, "Dhan");
         ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(fileJson);

        List<DhanStockPortfolio> stocks = objectMapper.readValue(payload, new TypeReference<List<DhanStockPortfolio>>() {});
        Resource resource = resourceLoader.getResource("classpath:stock.json");
        var stockPyalod = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        List<Company> companies = objectMapper.readValue(stockPyalod, new TypeReference<List<Company>>() {});

        List<NseStock> nseStocks = stocks.stream().map(stock -> {
            var dhanStock = PortfolioMapper.INSTANCE.mapNseStock(stock, companies);
            var nseEntity = NseStockMapper.INSTANCE.mapNseStockEntity(dhanStock);
            nseEntity.setBrokerPlatform("Dhan");
            nseEntity.setUserId("XMW248");
            nseStockRepository.save(nseEntity);
            return dhanStock;
        }).collect(Collectors.toList());
        return nseStocks;
    }
}
