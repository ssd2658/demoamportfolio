package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.am.mypotrfolio.utils.NseFileBuilder;
import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.DhanStockPortfolio;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.ZerodhaStockPortfolio;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.mapper.PortfolioMapper;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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


@Service("Zerodha")
@RequiredArgsConstructor
public class ZerodhaService implements PortfolioService{
    private final NseFileBuilder nseFileBuilder;
    private final ResourceLoader resourceLoader;
    private final NseStockRepository nseStockRepository;

    @Override
    @SneakyThrows
    public List<NseStock> processNseStock(MultipartFile file){
         List<Map<String, String>> fileJson = nseFileBuilder.parseExcel(file, "Zerodha");
         ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(fileJson);

        List<ZerodhaStockPortfolio> stocks = objectMapper.readValue(payload, new TypeReference<List<ZerodhaStockPortfolio>>() {});

        List<NseStock> nseStocks = stocks.stream().map(stock -> {
            var zerodhaStock = PortfolioMapper.INSTANCE.toNseStockFromZerodha(stock);
            var nseEntity = NseStockMapper.INSTANCE.mapNseStockEntity(zerodhaStock);
            nseEntity.setBrokerPlatform("Zerodha");
            nseStockRepository.save(nseEntity);
            return zerodhaStock;
        }).collect(Collectors.toList());
        return nseStocks;
    }

    // @Override
    // public List<NseStock> processNseStock(MultipartFile file) {

    //     String nseStockPath = null;
    //     nseStockPath = "holdings-BKJ665.xlsx";
    //      processStock(nseStockPath);
    //     return null;
    // }

    // private List<NseStock> processStock(String nseStockPath) {
    //     List<NseStock>  nseStocks = new ArrayList<>();
    //     nseStockPath = "C:\\Users\\MKU257\\Downloads\\"+nseStockPath;
    //     try (FileInputStream fis = new FileInputStream(nseStockPath);
    //          Workbook workbook = new XSSFWorkbook(fis)) {

    //         Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

    //         // Populate the new column
    //         for (Row row : sheet) {
    //             if (row.getRowNum() >=0 && row.getRowNum()<=22) continue; //
    //             nseStocks.add(getNseStockFromZerodha(row));
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

    // public NseStock getNseStockFromZerodha(Row stock) {
    //     DecimalFormat decimalFormat = new DecimalFormat("0.00");
    //     var investedValue = stock.getCell(4).getNumericCellValue() * stock.getCell(9).getNumericCellValue();
    //     var currentValue = stock.getCell(4).getNumericCellValue() * stock.getCell(10).getNumericCellValue();
    //     return NseStock.builder()
    //             .symbol(stock.getCell(1).getStringCellValue())
    //             .quantity(stock.getCell(4).getNumericCellValue())
    //             .avePrice(stock.getCell(9).getNumericCellValue())
    //             .investedValue(Double.parseDouble(decimalFormat.format(investedValue)))
    //             .currentValue(Double.parseDouble(decimalFormat.format(currentValue)))
    //             .ltp(stock.getCell(10).getNumericCellValue())
    //             //.daysPNL(String.valueOf(stock.getCell(7).getNumericCellValue()))
    //             //.daysPNLInPercentage(decimalFormat.format(stock.getCell(9).getNumericCellValue()))
    //             .overAllPNL(Double.parseDouble(decimalFormat.format(currentValue-investedValue)))
    //             .overAllPNLInPercentage(decimalFormat.format(calculatePercentageReturn(investedValue, currentValue)))
    //             .brokerPlatform("Zerodha")
    //             .isMarginTrade("False")

    //             .build();


    // }

    // public static double calculatePercentageReturn(double initialValue, double finalValue) {
    //     if (initialValue == 0) {
    //         throw new IllegalArgumentException("Initial value cannot be zero.");
    //     }
    //     return ((finalValue - initialValue) / initialValue) * 100;
    // }
}
