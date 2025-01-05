package org.am.mypotrfolio.utils;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.am.mypotrfolio.domain.MutualFunds;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExcelProcessor {

    private final String folderPath = "C:\\Users\\MKU257\\Downloads\\PortfolioMasterData\\MutualFunds";
    public List<File> getAllFiles() throws IOException {
        Path dir = Paths.get(folderPath);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile) // Filter out directories
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    public List<String> getAllFileNames() throws IOException {
        return getAllFiles().stream().map(File::getName).collect(Collectors.toList());
    }

    public List<Path> getAllFilePaths() throws IOException {
        Path dir = Paths.get(folderPath);

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile) // Filter out directories
                    .collect(Collectors.toList());
        }
    }

    public List<MutualFunds> processFiles(FileProcessor processor) throws IOException {
        // Assuming processor is a functional interface to process files
        List<MutualFunds> processedData = processor.processFiles(file -> {
            List<MutualFunds> mutualFunds = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream(file);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

                // Iterate through rows (starting from row 1 if row 0 is the header)
                for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
                    Row rowData = sheet.getRow(rowIndex);

                    if (rowData != null) {  // Ensure the row is not null
                        String amcCode = getStringCellValue(rowData, 0);
                        String isin = getStringCellValue(rowData, 1);
                        String schemeName = getStringCellValue(rowData, 2);
                        String schemeType = getStringCellValue(rowData, 3);

                        MutualFunds fund = MutualFunds.builder()
                                .ISIN(isin)
                                .amcCode(amcCode)
                                .schemeName(schemeName)
                                .schemeType(schemeType)
                                .build();

                        mutualFunds.add(fund);
                    }
                }
                return mutualFunds;

            } catch (Exception e) {
                System.err.println("Error processing Excel file: " + e.getMessage());
                throw new IOException("Error in file processing", e); // Re-throw as IOException
            }
        });

        return processedData;
    }

    // Helper method to get string value from a cell with null-check
    private String getStringCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                case BLANK:
                    return "";
                default:
                    return "";
            }
        }
        return ""; // Return empty string if cell is null
    }

    public interface FileProcessor {
        List<MutualFunds> processFiles(FileProcessorLambda fileProcessorLambda) throws IOException;
    }

    @FunctionalInterface
    public interface FileProcessorLambda {
        List<MutualFunds> processFile(File file) throws IOException;
    }
}
