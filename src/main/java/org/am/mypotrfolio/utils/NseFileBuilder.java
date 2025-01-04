package org.am.mypotrfolio.utils;

import lombok.RequiredArgsConstructor;
import org.am.mypotrfolio.domain.MutualFunds;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NseFileBuilder {
    private final NseStockRepository nseStockRepository;

    public void writeToExcel(List<NseStock> data) {

        String path = "C:\\Users\\MKU257\\Downloads\\BidLines.xlsx";
        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);// Get the first sheet
            int cellIndex;
            // Write data rows
            int rowIndex = 1;
            int lastCellIndex = headerRow.getLastCellNum();
            for (NseStock stock : data) {
                Row row = sheet.createRow(rowIndex++);
                for ( cellIndex = 0; cellIndex < lastCellIndex; cellIndex++ ) {
                    Cell cell = row.createCell(cellIndex);
                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("Symbol"))
                        cell.setCellValue(stock.getSymbol());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("Quantity"))
                        cell.setCellValue(stock.getQuantity());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("AvgPrice"))
                        cell.setCellValue(stock.getAvePrice());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("InvestedValue"))
                        cell.setCellValue(stock.getInvestedValue());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("CurrentValue"))
                        cell.setCellValue(stock.getCurrentValue());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("LTP"))
                        cell.setCellValue(stock.getLtp());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("BrokerPlateform"))
                        cell.setCellValue(stock.getBrokerPlatform());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("Days PNL (InPercentage)"))
                        cell.setCellValue(stock.getDaysPNLInPercentage());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("Overall PNL( In Percentage)"))
                        cell.setCellValue(stock.getOverAllPNLInPercentage());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("OverAllPNL"))
                        cell.setCellValue(stock.getOverAllPNL());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("DaysPNL"))
                        cell.setCellValue(stock.getDaysPNL());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("MarginTrade"))
                        cell.setCellValue("Y");

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("BrokerPlateform"))
                        cell.setCellValue(stock.getBrokerPlatform());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("AccountID"))
                        cell.setCellValue("XXXX");

                }
            }

            try (FileOutputStream fos = new FileOutputStream(path)) {
                workbook.write(fos);
            }
            System.out.println("Processed File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIntoDatabase(List<NseStock> data) {
        data.stream()
                .map(NseStockMapper.INSTANCE::mapNseStockEntity)
                .forEach(nseStockRepository::save);

    }

    public void writeToMutualExcel(List<MutualFunds> data) {

        String path = "C:\\Users\\MKU257\\Downloads\\MutualFunds.xlsx";
        try (FileInputStream fis = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);// Get the first sheet
            int cellIndex;
            // Write data rows
            int rowIndex = 1;
            int lastCellIndex = headerRow.getLastCellNum();
            for (MutualFunds stock : data) {
                Row row = sheet.createRow(rowIndex++);
                for ( cellIndex = 0; cellIndex < lastCellIndex; cellIndex++ ) {
                    Cell cell = row.createCell(cellIndex);
                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("ISIN"))
                        cell.setCellValue(stock.getISIN());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("schemeName"))
                        cell.setCellValue(stock.getSchemeName());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("schemeType"))
                        cell.setCellValue(stock.getSchemeType());

                    if(headerRow.getCell(cellIndex).getStringCellValue().equalsIgnoreCase("amcCode"))
                        cell.setCellValue(stock.getAmcCode());
                }
            }

            try (FileOutputStream fos = new FileOutputStream(path)) {
                workbook.write(fos);
            }
            System.out.println("Processed File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
