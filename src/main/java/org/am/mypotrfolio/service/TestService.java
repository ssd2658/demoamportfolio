package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.mapper.CompanyMapper;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.repo.CompanyRepository;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.utils.ExcelHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.am.mypotrfolio.model.Constant.AMPORTFOLIO_FILE;
import static org.am.mypotrfolio.model.Constant.COMPANY_FILE;

@Service
@RequiredArgsConstructor
public class TestService {
    private final NseStockRepository nseStockRepository;
    private final CompanyRepository companyRepository;
    private final ExcelHelper excelHelper;


    public Map<String, NseStock> getNseStocks(String filterBy,  int limit) {
        if(filterBy.equalsIgnoreCase("quantity"))
            return sortByQuantity(getAggregatedStocks());

        if(filterBy.equalsIgnoreCase("symbol"))
            return getAggregatedStocks();

        if(filterBy.equalsIgnoreCase("investedvalue"))
            return sortByInvestment(getAggregatedStocks());

        // if(filterBy.equalsIgnoreCase("currentvalue"))
        //     return sortByCurrentValue(getAggregatedStocks());

        // if(filterBy.equalsIgnoreCase("pnl"))
        //     return sortByOverAllPNL(getAggregatedStocks());

        return null;
    }

    public Map<String, NseStock> sortByInvestment(Map<String, NseStock> stockMap) {
        return stockMap.entrySet()
                .stream()
                // Sort by investment value in ascending order
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(NseStock::getInvestedValue)))
                // Collect the sorted entries back into a LinkedHashMap to maintain the order
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function (not needed here)
                        LinkedHashMap::new // Use LinkedHashMap to maintain insertion order
                ));
    }

    // public Map<String, NseStock> sortByCurrentValue(Map<String, NseStock> stockMap) {
    //     return stockMap.entrySet()
    //             .stream()
    //             // Sort by investment value in ascending order
    //             .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(NseStock::getCurrentValue)))
    //             // Collect the sorted entries back into a LinkedHashMap to maintain the order
    //             .collect(Collectors.toMap(
    //                     Map.Entry::getKey,
    //                     Map.Entry::getValue,
    //                     (e1, e2) -> e1, // Merge function (not needed here)
    //                     LinkedHashMap::new // Use LinkedHashMap to maintain insertion order
    //             ));
    // }

    // public Map<String, NseStock> sortByOverAllPNL(Map<String, NseStock> stockMap) {
    //     return stockMap.entrySet()
    //             .stream()
    //             // Sort by investment value in ascending order
    //             .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(NseStock::getOverAllPNL)))
    //             // Collect the sorted entries back into a LinkedHashMap to maintain the order
    //             .collect(Collectors.toMap(
    //                     Map.Entry::getKey,
    //                     Map.Entry::getValue,
    //                     (e1, e2) -> e1, // Merge function (not needed here)
    //                     LinkedHashMap::new // Use LinkedHashMap to maintain insertion order
    //             ));
    // }

    public Map<String, NseStock> sortByQuantity(Map<String, NseStock> stockMap) {
        return stockMap.entrySet()
                .stream()
                // Sort by investment value in ascending order
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(NseStock::getQuantity)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function (not needed here)
                        LinkedHashMap::new // Use LinkedHashMap to maintain insertion order
                ));
    }

    public Map<String, NseStock> getAggregatedStocks() {
        // Fetch all stocks from the database
        List<NseStock> stocks = nseStockRepository.findAll().stream()
                .map(NseStockMapper.INSTANCE::mapNseStock)
                .toList();

        // Group by composite key (symbol, brokerPlatform) and aggregate
         return stocks.stream()
                .collect(Collectors.groupingBy(
                        NseStock::getSymbol,
                        Collectors.collectingAndThen(Collectors.toList(), this::aggregateStocks)
                ))
                 .entrySet()
                 .stream()
                 .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                 .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,  LinkedHashMap::new ));
    }

    private NseStock aggregateStocks(List<NseStock> stocks) {
        String symbol = stocks.get(0).getSymbol();
        //String brokerPlatform = stocks.get(0).getBrokerPlatform();

        double totalQuantity = stocks.stream().mapToDouble(NseStock::getQuantity).sum();
        double totalInvestedValue = stocks.stream().mapToDouble(NseStock::getInvestedValue).sum();
        //double totalCurrentValue = stocks.stream().mapToDouble(NseStock::getCurrentValue).sum();
        double averagePrice = stocks.stream()
                .mapToDouble(stock -> stock.getAvePrice() * stock.getQuantity())
                .sum() / totalQuantity;

        // Create and return an aggregated result object
        return NseStock.builder()
                .symbol(symbol)
                .investedValue(totalInvestedValue)
                //.currentValue(totalCurrentValue)
                .avePrice(averagePrice)
                .quantity(totalQuantity)
                //.overAllPNL(totalCurrentValue-totalInvestedValue)
                .build();

    }

    @SneakyThrows
    public ByteArrayResource generateRoutingListExcel() throws IOException {

        ByteArrayResource resource;
        try (XSSFWorkbook workbookTemplate = new XSSFWorkbook(new ClassPathResource(COMPANY_FILE).getInputStream())) {
            List<Company> companies = companyRepository.findAll().stream().map(CompanyMapper.INSTANCE::COMPANY).toList();

            //XSSFWorkbook workbookWithTaskList = excelHelper.getWorkbook(workbookTemplate, companies);
            XSSFWorkbook workbookWithTaskList = excelHelper.writeToExcel(workbookTemplate, companies);
            workbookTemplate.unLock();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbookWithTaskList.write(outputStream);
                resource = new ByteArrayResource(outputStream.toByteArray());

            }
        }
        return resource;
    }

    @SneakyThrows
    public ByteArrayResource generatePortfolioListExcel() throws IOException {

        ByteArrayResource resource;
        try (XSSFWorkbook workbookTemplate = new XSSFWorkbook(new ClassPathResource(AMPORTFOLIO_FILE).getInputStream())) {
            Map<String, NseStock> stocks = getAggregatedStocks();


            XSSFWorkbook workbookWithTaskList = excelHelper.writeToPortfolioExcel(workbookTemplate, stocks);
            workbookTemplate.unLock();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbookWithTaskList.write(outputStream);
                resource = new ByteArrayResource(outputStream.toByteArray());

            }
        }
        return resource;
    }
}
