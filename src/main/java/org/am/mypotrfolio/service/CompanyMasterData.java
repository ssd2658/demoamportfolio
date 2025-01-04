package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.mapper.CompanyMapper;
import org.am.mypotrfolio.repo.CompanyRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyMasterData {

    private final CompanyRepository companyRepository;

    public void processCompanyRecords(String nseStockPath) {
        List<Company> companyList = new LinkedList<>();
        nseStockPath = "C:\\Users\\MKU257\\Downloads\\NSE_BSE.xlsx";
        try (FileInputStream fis = new FileInputStream(nseStockPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet
            int lastRowNum = sheet.getLastRowNum();
            int currentRowNum = 0;
            // Populate the new column
            for (Row row : sheet) {
                currentRowNum ++;
                if (row.getRowNum() == 0) continue;
                if (row.getCell(0).getStringCellValue().endsWith(".BO")) continue;//
                getCompany(row, currentRowNum);
                //companyList.add(getCompany(row));
            }

            /*var companyEntity = companyList.stream().map(CompanyMapper.INSTANCE::COMPANY_ENTITY).toList();

            companyRepository.saveAll(companyEntity);*/
            System.out.println("Company Record saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Company getCompany(Row stock, int currentRowNum) {
        System.out.println("Current row num: " + currentRowNum);
        var comapny =  Company.builder()
                .symbol(stock.getCell(0).getStringCellValue().replace(".NS", ""))
                .companyName(stock.getCell(15) == null ? "Not Found" : stock.getCell(8).getStringCellValue())
                .marketCapital(stock.getCell(4).getNumericCellValue())
                .sector(stock.getCell(15) == null ? "Others" : stock.getCell(15).getStringCellValue())
                .build();
        var companyEntity = CompanyMapper.INSTANCE.COMPANY_ENTITY(comapny);
        companyRepository.save(companyEntity);
        return comapny;
    }

}
