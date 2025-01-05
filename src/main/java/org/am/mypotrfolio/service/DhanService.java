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
            nseStockRepository.save(nseEntity);
            return dhanStock;
        }).collect(Collectors.toList());
        return nseStocks;
    }

    
    // public List<NseStock> processDhanGeneratedFile(String nseStockPath) {

    //     List<NseStock>  nseStocks = new ArrayList<>();
    //     nseStockPath = "C:\\Users\\MKU257\\Downloads\\"+nseStockPath;
    //     try (FileInputStream fis = new FileInputStream(nseStockPath);
    //          Workbook workbook = new XSSFWorkbook(fis)) {

    //         Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

    //         // Populate the new column
    //         for (Row row : sheet) {
    //             if (row.getRowNum() == 0) continue; //
    //             nseStocks.add(getNseStockFromDhan(row));
    //         }
    //         nseFileBuilder.writeIntoDatabase(nseStocks);
    //         nseFileBuilder.writeToExcel(nseStocks);
    //         // Save the changes
    //         try (FileOutputStream fos = new FileOutputStream(nseStockPath)) {
    //             workbook.write(fos);
    //         }
    //         System.out.println("Column added successfully!");
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }
    // private List<NseStock> processStock(String nseStockPath) {
    //     List<NseStock>  nseStocks = new ArrayList<>();
    //     List<Company> companies = ObjectUtils.readJsonFile(new TypeReference<List<Company>>() {
    //     }, "stock.json").orElse(new ArrayList<>());

    //     Map<String, String> mapComapny = convertToSymbolMap(companies);

    //     nseStockPath = "C:\\Users\\MKU257\\Downloads\\DhanPortfolio.xlsx";
    //     try (FileInputStream fis = new FileInputStream(nseStockPath);
    //          Workbook workbook = new XSSFWorkbook(fis)) {

    //         Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
    //         int lastColumn = sheet.getRow(0).getLastCellNum(); // Determine where to add the column
    //         int index =0;
    //         while(index < lastColumn) {
    //             if(sheet.getRow(0).getCell(index).getStringCellValue().equalsIgnoreCase("SYMBOL")) {
    //                 //sheet.getRow(0).createCell(lastColumn).setCellValue("Name");
    //                 break;
    //             }

    //             if (sheet.getRow(0).getCell(index).getStringCellValue().equalsIgnoreCase("Name")) {
    //                 sheet.getRow(0).createCell(lastColumn).setCellValue("Symbol");
    //                 break;
    //             }
    //             index++;
    //         }
    //         // Add a header for the new column


    //         // Populate the new column
    //         for (Row row : sheet) {
    //             if (row.getRowNum() == 0) continue; //
    //             Cell cell = row.createCell(lastColumn);
    //             if (!mapComapny.containsKey(row.getCell(0).getStringCellValue())) {
    //                 cell.setCellValue(findKeyByValue(mapComapny, row.getCell(0).getStringCellValue()));
    //             }
    //             else cell.setCellValue(mapComapny.get(row.getCell(0).getStringCellValue()));
    //         }
    //         // Save the changes
    //         try (FileOutputStream fos = new FileOutputStream(nseStockPath)) {
    //             workbook.write(fos);
    //         }
    //         System.out.println("Column added successfully!");
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }


    // public Map<String, String> convertToSymbolMap(List<Company> companies) {
    //     Map<String, String> symbolMap = new HashMap<>();
    //     for (Company company : companies) {
    //         symbolMap.put(company.getSymbol(), company.getCompanyName());
    //     }
    //     return symbolMap;
    // }

    // public static String findKeyByValue(Map<String, String> map, String value) {
    //     for (Map.Entry<String, String> entry : map.entrySet()) {
    //         if (entry.getValue().equals(value)) {
    //             return entry.getKey();
    //         }
    //     }
    //     return null; // Or throw an exception if no match is found
    // }

    // public NseStock getNseStockFromDhan(Row stock) {
    //     DecimalFormat decimalFormat = new DecimalFormat("0.00%");
    //     return NseStock.builder()
    //             .symbol(stock.getCell(8).getStringCellValue())
    //             .quantity(stock.getCell(1).getNumericCellValue())
    //             .avePrice(stock.getCell(2).getNumericCellValue())
    //             .investedValue(stock.getCell(4).getNumericCellValue())
    //             .currentValue(stock.getCell(5).getNumericCellValue())
    //             .ltp(stock.getCell(3).getNumericCellValue())
    //             //.daysPNL(String.valueOf(stock.getCell(7).getNumericCellValue()))
    //             //.daysPNLInPercentage(decimalFormat.format(stock.getCell(9).getNumericCellValue()))
    //             .overAllPNL(stock.getCell(6).getNumericCellValue())
    //             .overAllPNLInPercentage(decimalFormat.format(stock.getCell(7).getNumericCellValue()))
    //             .brokerPlatform("Dhan")
    //             .isMarginTrade("False")
    //             .build();


    // }
}
