package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.NseStockDetails;
import org.am.mypotrfolio.domain.Portfolio;
import org.am.mypotrfolio.dto.StockPriceDTO;

import org.am.mypotrfolio.enums.FilterBy;
import org.am.mypotrfolio.mapper.CompanyMapper;
import org.am.mypotrfolio.mapper.NseStockMapper;
import org.am.mypotrfolio.repo.CompanyRepository;
import org.am.mypotrfolio.repo.NseStockRepository;
import org.am.mypotrfolio.service.StockService;
import org.am.mypotrfolio.utils.ExcelHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.am.mypotrfolio.model.Constant.AMPORTFOLIO_FILE;
import static org.am.mypotrfolio.model.Constant.COMPANY_FILE;

@Service
@RequiredArgsConstructor
public class TestService {
    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    private final NseStockRepository nseStockRepository;
    private final StockService stockService;
    private final CompanyRepository companyRepository;
    private final ExcelHelper excelHelper;

    public Map<String, NseStock> getNseStocks(FilterBy filterBy, Integer maxCount) {
        // Fetch stocks based on the filter
        List<NseStock> stocks = nseStockRepository.findAll().stream()
                .map(NseStockMapper.INSTANCE::mapNseStock)
                .toList();

        // Sort and limit stocks based on the filter
        Comparator<NseStock> comparator = switch (filterBy) {
            case QUANTITY -> Comparator.comparing(NseStock::getQuantity).reversed();
            case SYMBOL -> Comparator.comparing(NseStock::getSymbol);
            case INVESTED_VALUE -> Comparator.comparing(NseStock::getInvestedValue).reversed();
        };

        return stocks.stream()
            .sorted(comparator)
            .limit(maxCount)
            .collect(Collectors.toMap(
                NseStock::getSymbol, 
                Function.identity(), 
                (v1, v2) -> v1, 
                LinkedHashMap::new
            ));
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

    public List<NseStockDetails> getAllStocks() {
        return nseStockRepository.getInvestedStock();
    }

    public Portfolio getAllStocksByUserId(String userId) {
        log.info("Fetching portfolio data for user: {}", userId);
        try {
            List<NseStockDetails> stocks = nseStockRepository.getAggregatedStocksByUserId(userId);
            log.info("Found {} stocks for user {}", stocks.size(), userId);

            // Calculate time-based changes for each stock
            stocks.forEach(stock -> {
                if (stock.getCurrentPrice() > 0) {
                    calculateTimeBasedChanges(stock);
                }
            });

            // Calculate portfolio totals
            double totalInvestment = stocks.stream()
                    .mapToDouble(NseStockDetails::getInvestedValue)
                    .sum();

            double totalCurrentValue = stocks.stream()
                    .mapToDouble(stock -> stock.getCurrentPrice() * stock.getQuantity())
                    .sum();

            // Calculate sector allocations
            Map<String, Portfolio.SectorAllocation> sectorAllocations = calculateSectorAllocations(stocks, totalInvestment);

            // Get top performers and losers
            List<NseStockDetails> sortedByReturn = stocks.stream()
                    .filter(stock -> stock.getCurrentPrice() > 0)
                    .sorted((a, b) -> {
                        double aReturn = ((a.getCurrentPrice() * a.getQuantity() - a.getInvestedValue()) / a.getInvestedValue()) * 100;
                        double bReturn = ((b.getCurrentPrice() * b.getQuantity() - b.getInvestedValue()) / b.getInvestedValue()) * 100;
                        return Double.compare(bReturn, aReturn);
                    })
                    .collect(Collectors.toList());

            List<NseStockDetails> topPerformers = sortedByReturn.stream()
                    .limit(5)
                    .collect(Collectors.toList());

            List<NseStockDetails> topLosers = sortedByReturn.stream()
                    .skip(Math.max(0, sortedByReturn.size() - 5))
                    .collect(Collectors.toList());

            // Build and return portfolio
            Portfolio portfolio = Portfolio.builder()
                    .stocks(stocks)
                    .totalInvestment(totalInvestment)
                    .totalCurrentValue(totalCurrentValue)
                    .totalProfitLoss(totalCurrentValue - totalInvestment)
                    .totalReturnPercentage(totalInvestment > 0 ? ((totalCurrentValue - totalInvestment) / totalInvestment) * 100 : 0)
                    .topPerformers(topPerformers)
                    .topLosers(topLosers)
                    .totalStocks(stocks.size())
                    .totalIndustries((int) stocks.stream().map(NseStockDetails::getIndustry).distinct().count())
                    .sectorAllocations(sectorAllocations)
                    .build();

            log.info("Successfully built portfolio for user {}. Total Investment: {}, Total Current Value: {}", 
                userId, totalInvestment, totalCurrentValue);
            return portfolio;

        } catch (Exception e) {
            log.error("Error fetching portfolio data for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch portfolio data", e);
        }
    }

    public List<NseStockDetails> getAllStocks(String brokerPlatform) {
        return getStocksByBrokerPlatform(brokerPlatform);
    }

    public List<NseStockDetails> getStocksByBrokerPlatform(String brokerPlatform) {
         List<NseStockDetails> stockDetails = nseStockRepository.getInvestedStock();
        //.stream()
        //     .filter(entity -> entity.getBrokerPlatform() != null && 
        //            entity.getBrokerPlatform().equalsIgnoreCase(brokerPlatform))
        //     .collect(Collectors.toList());
        
        // Enrich stock details with current value and other details
        return nseStockRepository.enrichNseStockDetailsList(stockDetails);
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

    private void calculateTimeBasedChanges(NseStockDetails stock) {
        // Get historical prices
        StockPriceDTO currentStock = stockService.getLastStockPrice(stock.getIsin());
        if (currentStock != null) {
            calculateOneDayChange(stock, currentStock);
            calculateOneMonthChange(stock);
            calculateOneYearChange(stock);
        }
    }

    private Map<String, Portfolio.SectorAllocation> calculateSectorAllocations(List<NseStockDetails> stocks, double totalInvestment) {
        Map<String, List<NseStockDetails>> sectorGroups = stocks.stream()
                .filter(stock -> stock.getIndustry() != null && !stock.getIndustry().isEmpty())
                .collect(Collectors.groupingBy(NseStockDetails::getIndustry));

        return sectorGroups.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        List<NseStockDetails> sectorStocks = entry.getValue();
                        double sectorInvestment = sectorStocks.stream()
                                .mapToDouble(NseStockDetails::getInvestedValue)
                                .sum();
                        double sectorCurrentValue = sectorStocks.stream()
                                .mapToDouble(stock -> stock.getCurrentPrice() * stock.getQuantity())
                                .sum();
                        
                        return Portfolio.SectorAllocation.builder()
                                .sector(entry.getKey())
                                .investedAmount(sectorInvestment)
                                .currentValue(sectorCurrentValue)
                                .allocationPercentage((sectorInvestment / totalInvestment) * 100)
                                .numberOfStocks(sectorStocks.size())
                                .stocks(sectorStocks)
                                .build();
                    }
                ));
    }

    private void calculateOneDayChange(NseStockDetails stock, StockPriceDTO currentStock) {
        double previousDayPrice = currentStock.getPreviousClose() != null ? 
            currentStock.getPreviousClose() : stock.getCurrentPrice();
        stock.setOneDayPreviousPrice(previousDayPrice);
        stock.setOneDayProfitLoss((stock.getCurrentPrice() - previousDayPrice) * stock.getQuantity());
        stock.setOneDayReturnPercentage(previousDayPrice > 0 ? 
            ((stock.getCurrentPrice() - previousDayPrice) / previousDayPrice) * 100 : 0);
    }

    private void calculateOneMonthChange(NseStockDetails stock) {
        LocalDateTime oneMonthAgo = LocalDateTime.now(ZoneId.systemDefault()).minusMonths(1);
        StockPriceDTO oneMonthPrice = stockService.getHistoricalPrice(stock.getIsin(), oneMonthAgo);
        if (oneMonthPrice != null) {
            double oneMonthPreviousPrice = oneMonthPrice.getLastPrice();
            stock.setOneMonthPreviousPrice(oneMonthPreviousPrice);
            stock.setOneMonthProfitLoss((stock.getCurrentPrice() - oneMonthPreviousPrice) * stock.getQuantity());
            stock.setOneMonthReturnPercentage(((stock.getCurrentPrice() - oneMonthPreviousPrice) / oneMonthPreviousPrice) * 100);
        }
    }

    private void calculateOneYearChange(NseStockDetails stock) {
        LocalDateTime oneYearAgo = LocalDateTime.now(ZoneId.systemDefault()).minusYears(1);
        StockPriceDTO oneYearPrice = stockService.getHistoricalPrice(stock.getIsin(), oneYearAgo);
        if (oneYearPrice != null) {
            double oneYearPreviousPrice = oneYearPrice.getLastPrice();
            stock.setOneYearPreviousPrice(oneYearPreviousPrice);
            stock.setOneYearProfitLoss((stock.getCurrentPrice() - oneYearPreviousPrice) * stock.getQuantity());
            stock.setOneYearReturnPercentage(((stock.getCurrentPrice() - oneYearPreviousPrice) / oneYearPreviousPrice) * 100);
        }
    }
}
