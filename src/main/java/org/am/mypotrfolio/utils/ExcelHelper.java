package org.am.mypotrfolio.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.mapper.CompanyMapper;
import org.am.mypotrfolio.repo.CompanyRepository;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.am.mypotrfolio.model.Constant.EXCEL_PORTFOLIO_SHEET_NAME;
import static org.am.mypotrfolio.model.Constant.EXCEL_SHEET_NAME;

@Component
@RequiredArgsConstructor
public class ExcelHelper {

    private final ObjectMapper objectMapper;
    private final CompanyRepository companyRepository;

    public XSSFWorkbook getWorkbook(XSSFWorkbook workbook, List<Company> companies) {


        XSSFSheet sheet = workbook.getSheet(EXCEL_SHEET_NAME);
        AtomicInteger columnIndex = new AtomicInteger(0);
        AtomicInteger rowIndex = new AtomicInteger(0);


        List<Map<String, Object>> companyMapList = companies.stream()
                .map(dto -> objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {
                }))
                .toList();

        Map<String, Object> commonAttributesWithValues = extractCommonAttributeValues(companies.get(0));
        mapValuesToCell(columnIndex, rowIndex, sheet, commonAttributesWithValues, companyMapList);
        return workbook;
    }

    private void setCenteredCellStyle(Cell cell) {
        CellStyle style = cell.getCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(style);
    }

    private void setCellValue(Cell cell, Object value) {
        if (value != null) {
            if (value instanceof String stringValue) {
                cell.setCellValue(stringValue);
            } else if (value instanceof Integer intValue) {
                cell.setCellValue(intValue);
            } else if (value instanceof Boolean booleanValue) {
                cell.setCellValue(booleanValue);
            } else if (value instanceof Double doubleValue) {
                cell.setCellValue(doubleValue);
            }
        } else {
            cell.setCellValue((String) null);
        }
        setCenteredCellStyle(cell);
    }

    private Map<String, Object> extractCommonAttributeValues(Company company) {
        Map<String, Object> commonValues = new LinkedHashMap<>();
        commonValues.put("Symbol", company.getSymbol());
        commonValues.put("CompanyName", company.getCompanyName());
        commonValues.put("MarketCap", company.getMarketCapital());
        commonValues.put("Sector", company.getSector());
        return commonValues;
    }


    public static XSSFWorkbook writeToExcel(XSSFWorkbook workbook, List<Company> companies) throws IOException {
        // Create a new workbook and a sheet named 'company'
        XSSFSheet sheet = workbook.getSheet(EXCEL_SHEET_NAME);


        // Create the header row
        /*Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Symbol");
        headerRow.createCell(1).setCellValue("CompanyName");
        headerRow.createCell(2).setCellValue("Sector");
        headerRow.createCell(3).setCellValue("MarketCap");*/

        // Iterate over the company list and write data to subsequent rows
        int rowNum = 1;
        for (Company company : companies) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(company.getSymbol());
            row.createCell(1).setCellValue(company.getCompanyName());
            row.createCell(2).setCellValue(company.getSector());
            row.createCell(3).setCellValue(company.getMarketCapital());
        }

        // Adjust column width to fit content
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        /*// Write the workbook to a file
        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }*/
        return workbook;
    }

    public XSSFWorkbook writeToPortfolioExcel(XSSFWorkbook workbook, Map<String, NseStock> stocks) throws IOException {
        // Create a new workbook and a sheet named 'company'
        XSSFSheet sheet = workbook.getSheet(EXCEL_PORTFOLIO_SHEET_NAME);

        // Iterate over the company list and write data to subsequent rows
        int rowNum = 1;


        for (Map.Entry<String, NseStock> entry : stocks.entrySet()) {
            String symbol = entry.getKey();  // Extract the symbol (key)
            NseStock nseStock = entry.getValue();

            var sector = Optional.ofNullable(companyRepository.findBySymbol(symbol))
                    .map(CompanyMapper.INSTANCE::COMPANY)
                    .map(Company::getSector)
                    .orElse("");

            var companyName = Optional.ofNullable(companyRepository.findBySymbol(symbol))
                    .map(CompanyMapper.INSTANCE::COMPANY)
                    .map(Company::getCompanyName)
                    .orElse("");

             // Extract the Company object (value)
            // Create a new row for each entry in the map
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(symbol);  // Write symbol in the first column
            row.createCell(1).setCellValue(nseStock.getInvestedValue());  // Write company name
            //row.createCell(2).setCellValue(nseStock.getCurrentValue());  // Write sector
            row.createCell(3).setCellValue(nseStock.getAvePrice());
            row.createCell(4).setCellValue(nseStock.getQuantity());
            //row.createCell(5).setCellValue(nseStock.getOverAllPNL());
            row.createCell(6).setCellValue(sector);
            row.createCell(7).setCellValue(companyName);

            // Write market cap
        }

        // Adjust column width to fit content
        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    private void mapValuesToCell(AtomicInteger columnIndex, AtomicInteger rowIndex, XSSFSheet sheet, Map<String, Object> commonValues, List<Map<String, Object>> routesMapList) {

        commonValues.forEach((key, value) -> {
            Row dataRow = sheet.getRow(rowIndex.getAndIncrement());
            Cell cell = dataRow.createCell(1);
            setCellValue(cell, value);
        });
        rowIndex.set(6);
        routesMapList.forEach(routeMap -> {
            Row dataRow = sheet.createRow(rowIndex.getAndIncrement());
            columnIndex.set(0);

            routeMap.entrySet().stream()
                    .filter(entry -> !commonValues.containsKey(entry.getKey()))
                    .forEach(entry -> {
                        Cell cell = dataRow.createCell(columnIndex.get());
                        var value = entry.getValue();
                        setCellValue(cell, value);
                        columnIndex.getAndIncrement();
                    });
        });
        Row headerRow = sheet.getRow(5);
        int columnCount = headerRow.getLastCellNum();
        IntStream.range(0, columnCount).mapToObj(headerRow::getCell).filter(Objects::nonNull).forEach(this::setCenteredCellStyle);

        IntStream.range(0, columnCount).forEach(sheet::autoSizeColumn);
    }
}

