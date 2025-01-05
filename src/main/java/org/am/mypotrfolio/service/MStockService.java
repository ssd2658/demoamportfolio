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
            nseStockRepository.save(nseEntity);
            return mStock;
        }).collect(Collectors.toList());
        return nseStocks;
    }

    // @Override
    // public List<NseStock> processNseStock(MultipartFile file) {

    //     String nseStockPath = null;
    //     List<NseStock>  nseStocks = new ArrayList<>();
    //     nseStockPath = "C:\\Users\\MKU257\\Downloads\\MStock.xlsx";
    //     try (FileInputStream fis = new FileInputStream(nseStockPath);
    //          Workbook workbook = new XSSFWorkbook(fis)) {

    //         Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

    //         // Populate the new column
    //         for (Row row : sheet) {
    //             if (row.getRowNum() == 0) continue; //
    //             nseStocks.add(getNseStockFromMstock(row));
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

    // private NseStock getNseStockFromMstock(Row stock) {
    //     DecimalFormat decimalFormat = new DecimalFormat("0.00%");
    //     return NseStock.builder()
    //             .symbol(stock.getCell(0).getStringCellValue())
    //             .quantity(stock.getCell(1).getNumericCellValue())
    //             .avePrice(stock.getCell(2).getNumericCellValue())
    //             .investedValue(stock.getCell(4).getNumericCellValue())
    //             .currentValue(stock.getCell(5).getNumericCellValue())
    //             .ltp(stock.getCell(3).getNumericCellValue())
    //             .daysPNL(stock.getCell(7).getNumericCellValue())
    //             .daysPNLInPercentage(decimalFormat.format(stock.getCell(9).getNumericCellValue()))
    //             .overAllPNL(stock.getCell(6).getNumericCellValue())
    //             .overAllPNLInPercentage(decimalFormat.format(stock.getCell(8).getNumericCellValue()))
    //             .brokerPlatform("MStock")
    //             .isMarginTrade("True")
    //             .build();


    // }
}
