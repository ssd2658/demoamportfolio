package org.am.mypotrfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.utils.ObjectUtils;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.domain.Company;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NseStockService {
    private final NseStockRepository nseStockRepository;

    public NseStock test()  {
        var nsestock = NseStock.builder().symbol("AASS").build();
       var nseStockEntity =  NseStockMapper.INSTANCE.mapNseStockEntity(nsestock);
       nseStockRepository.save(nseStockEntity);
       return nsestock;
    }

    public NseStock processNseStock(String nseStockPath) throws IOException {

        List<Company> companies = ObjectUtils.readJsonFile(new TypeReference<List<Company>>() {
        }, "stock.json").orElse(new ArrayList<>());

        Map<String, String> mapComapny = convertToSymbolMap(companies);

        nseStockPath = "C:\\Users\\MKU257\\Downloads\\MW-NIFTY-100-18-Dec-2024.xlsx";
        //nseStockPath = "C:\\Users\\MKU257\\Downloads\\Dhan Porfolio.xlsx";
        try (FileInputStream fis = new FileInputStream(nseStockPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            int lastColumn = sheet.getRow(0).getLastCellNum(); // Determine where to add the column
            int index =0;
            while(index < lastColumn) {
                if(sheet.getRow(0).getCell(index++).getStringCellValue().equalsIgnoreCase("SYMBOL")) {
                    sheet.getRow(0).createCell(lastColumn).setCellValue("Name");
                    break;
                }

                if (sheet.getRow(0).getCell(index++).getStringCellValue().equalsIgnoreCase("Name")) {
                    sheet.getRow(0).createCell(lastColumn).setCellValue("Symbol");
                    break;
                }
            }
            // Add a header for the new column


            // Populate the new column
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; //
                Cell cell = row.createCell(lastColumn);
                if (!mapComapny.containsKey(row.getCell(0).getStringCellValue())) {
                    cell.setCellValue(findKeyByValue(mapComapny, row.getCell(0).getStringCellValue()));
                }
                else cell.setCellValue(mapComapny.get(row.getCell(0).getStringCellValue()));
            }

            // Save the changes
            try (FileOutputStream fos = new FileOutputStream(nseStockPath)) {
                workbook.write(fos);
            }
            System.out.println("Column added successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> convertToSymbolMap(List<Company> companies) {
        Map<String, String> symbolMap = new HashMap<>();
        for (Company company : companies) {
            symbolMap.put(company.getSymbol(), company.getSymbol());
        }
        return symbolMap;
    }

    public static String findKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null; // Or throw an exception if no match is found
    }
}
