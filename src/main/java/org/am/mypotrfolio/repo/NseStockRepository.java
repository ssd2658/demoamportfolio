package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.NseStockDetails;
import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.entity.EquityDataEntity;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.am.mypotrfolio.entity.StockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public interface NseStockRepository extends JpaRepository<NseStockEntity, UUID> {

    @Query("SELECT new org.am.mypotrfolio.domain.SectorInvestmentDTO(e.industry, SUM(n.investedValue), SUM(n.quantity)) " +
            "FROM NseStockEntity n " +
            "JOIN EquityDataEntity e ON n.symbol = e.symbol " +
            "WHERE n.brokerPlatform = 'Zerodha' " +
            "GROUP BY e.industry")
    List<SectorInvestmentDTO> findTotalInvestedBySector();

    @Query("SELECT new org.am.mypotrfolio.domain.SectorInvestmentDTO(e.industry, SUM(n.investedValue), SUM(n.quantity)) " +
            "FROM NseStockEntity n " +
            "JOIN EquityDataEntity e ON n.symbol = e.symbol " +
            "WHERE n.brokerPlatform = 'Zerodha' " +
            "GROUP BY e.industry")
    Page<SectorInvestmentDTO> findTotalInvestedBySector(Pageable pageable);

    @Query("SELECT n FROM NseStockEntity n " +
            "WHERE n.brokerPlatform = 'Zerodha' " +
            "AND n.createdDate = (SELECT MAX(m.createdDate) FROM NseStockEntity m WHERE m.symbol = n.symbol)")
    List<NseStockEntity> getAllNseStocks(@Param("brokerPlatform") String brokerPlatform);

    @Query("SELECT n FROM NseStockEntity n")
    List<NseStockEntity> getAllNseStocks();

    @Query("SELECT s FROM StockEntity s")
    List<StockEntity> getAllStockEntities();

    @Query("SELECT s FROM EquityDataEntity s WHERE s.symbol = :symbol")
    Optional<EquityDataEntity> findBySymbol(String symbol);

    @Query("SELECT s FROM StockEntity s WHERE s.symbol = :symbol ORDER BY s.lastUpdateTime DESC")
    List<StockEntity> getLastStockEntities(String symbol);

    default StockEntity getLastStockEntity(String symbol) {
        List<StockEntity> stockEntities = getLastStockEntities(symbol);
        return stockEntities.isEmpty() ? null : stockEntities.get(0);
    }

    default List<NseStockDetails> getInvestedStock(String brokerPlatform) {
        List<NseStockEntity> nseStocks = getAllNseStocks(brokerPlatform);
        return getNseStockDetails(nseStocks);
    }
    default List<NseStockDetails> getInvestedStock() {
        List<NseStockEntity> nseStocks = getAllNseStocks();
        return getNseStockDetails(nseStocks);
    }

    default List<NseStockDetails> getNseStockDetails(List<NseStockEntity> nseStocks) {
        return nseStocks.stream().map((NseStockEntity n) -> {
            StockEntity matchingStock = getLastStockEntity(n.getSymbol());

            // Skip mapping if matchingStock is null
            if (matchingStock == null) {
                return NseStockDetails.builder()
                .brokerPlatform(n.getBrokerPlatform()==null ? "Default" : n.getBrokerPlatform())
                    .symbol(n.getSymbol())
                    .quantity(n.getQuantity())
                    .avePrice(n.getAvePrice())
                    .investedValue(n.getInvestedValue())
                    .currentPrice(n.getAvePrice())
                    .profitLoss(0.0)
                    .percentChange(0.0)
                    .returnChange(0.0)
                    .build();
            }

            Double closePrice = matchingStock.getClosePrice() != null ? matchingStock.getClosePrice() : n.getAvePrice();
            Double openPrice = matchingStock.getOpenPrice() != null ? matchingStock.getOpenPrice() : n.getAvePrice();
            Double previousClose = matchingStock.getPreviousClose() != null ? matchingStock.getPreviousClose() : n.getAvePrice();

            // Calculate Current Value
            double currentValue = closePrice * n.getQuantity();

            // Calculate Profit/Loss
            double profitLoss = currentValue - n.getInvestedValue();

            // Calculate Total Percentage Change
            double totalPercentChange = ((currentValue - n.getInvestedValue()) / n.getInvestedValue()) * 100;

            // Calculate Daily Percentage Change and Return Change

            double dailyChange = 0.0;
            if (previousClose != null && previousClose != 0) {
                //dailyPercentChange = ((closePrice - previousClose) / previousClose) * 100;
                dailyChange = currentValue - (n.getQuantity()*openPrice);
            }

            return NseStockDetails.builder()
                .symbol(n.getSymbol())
                .quantity(n.getQuantity())
                .avePrice(n.getAvePrice())
                .investedValue(n.getInvestedValue())
                .currentPrice(closePrice)
                .openPrice(openPrice)
                .profitLoss(profitLoss)
                .percentChange(totalPercentChange)
                .returnChange(dailyChange)
                .build();
        }).map(this::enrichNseStockDetails)
        .collect(Collectors.toList());
    }

    // New method to populate NseStockDetails with additional information
    default NseStockDetails enrichNseStockDetails(NseStockDetails stockDetails) {
        findBySymbol(stockDetails.getSymbol()).ifPresent(equityData -> {
            stockDetails.setIndustry(equityData.getIndustry());
            stockDetails.setCompanyName(equityData.getName());
        });
        return stockDetails;
    }

    // Update existing methods to use enrichment
    default List<NseStockDetails> enrichNseStockDetailsList(List<NseStockDetails> stockDetailsList) {
        return stockDetailsList.stream()
            .map(this::enrichNseStockDetails)
            .collect(Collectors.toList());
    }

    @Query("SELECT new org.am.mypotrfolio.domain.SectorInvestmentDTO(" +
            "e.industry, " +
            "SUM(n.investedValue)) " +
            "FROM NseStockEntity n " +
            "JOIN EquityDataEntity e ON n.symbol = e.symbol " +
            "WHERE LOWER(n.brokerPlatform) = LOWER(:brokerPlatform) " +
            "GROUP BY e.industry")
    List<SectorInvestmentDTO> getSectorInvestments(@Param("brokerPlatform") String brokerPlatform);

    @Query(value = "WITH latest_stocks AS (" +
           "    SELECT symbol, close_price " +
           "    FROM (SELECT symbol, close_price, " +
           "                 ROW_NUMBER() OVER (PARTITION BY symbol ORDER BY created_at DESC) as rn " +
           "          FROM stocks) s " +
           "    WHERE rn = 1 " +
           "), " +
           "latest_nse_stocks AS (" +
           "    SELECT n2.symbol, n2.broker_platform, n2.created_date " +
           "    FROM (SELECT symbol, broker_platform, created_date, " +
           "                 ROW_NUMBER() OVER (PARTITION BY symbol, broker_platform ORDER BY created_date DESC) as rn " +
           "          FROM nse_stock " +
           "          WHERE user_id = :userId) n2 " +
           "    WHERE rn = 1" +
           ") " +
           "SELECT n.symbol, n.isin, " +
           "SUM(n.quantity) as total_quantity, " +
           "SUM(n.invested_value) as total_invested, " +
           "SUM(n.invested_value) / SUM(n.quantity) as avg_price, " +
           "STRING_AGG(DISTINCT n.broker_platform, ', ') as broker_platforms, " +
           "e.industry, e.name, s.close_price, " +
           "STRING_AGG(CONCAT(n.broker_platform, ':', CAST(n.quantity as varchar)), '; ') as broker_quantities " +
           "FROM nse_stock n " +
           "INNER JOIN latest_nse_stocks lns ON n.symbol = lns.symbol " +
           "    AND n.broker_platform = lns.broker_platform " +
           "    AND n.created_date = lns.created_date " +
           "LEFT JOIN equity_data e ON n.symbol = e.symbol " +
           "LEFT JOIN latest_stocks s ON n.symbol = s.symbol " +
           "WHERE n.user_id = :userId " +
           "GROUP BY n.symbol, n.isin, e.industry, e.name, s.close_price",
           nativeQuery = true)
    List<Object[]> getAggregatedStocksByUserIdNative(@Param("userId") String userId);

    default List<NseStockDetails> getAggregatedStocksByUserId(String userId) {
        List<Object[]> results = getAggregatedStocksByUserIdNative(userId);
        return results.stream()
            .map(row -> {
                NseStockDetails details = new NseStockDetails(
                    (String) row[0],                    // symbol
                    (String) row[1],                    // isin
                    ((Number) row[2]).doubleValue(),    // total_quantity
                    ((Number) row[3]).doubleValue(),    // total_invested_value
                    ((Number) row[4]).doubleValue(),    // avg_price
                    (String) row[5],                    // broker_platforms
                    (String) row[6],                    // industry
                    (String) row[7],                    // company_name
                    row[8] != null ? ((Number) row[8]).doubleValue() : 0.0  // current_price
                );
                
                // Parse and set broker-wise quantities
                String brokerQuantities = (String) row[9];
                if (brokerQuantities != null) {
                    details.setBrokerQuantities(
                        Stream.of(brokerQuantities.split(";"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toMap(
                                s -> s.split(":")[0].trim(),
                                s -> Double.parseDouble(s.split(":")[1].trim()),
                                (v1, v2) -> v1,
                                java.util.LinkedHashMap::new
                            ))
                    );
                }
                
                return details;
            })
            .collect(Collectors.toList());
    }

    default List<NseStockDetails> enrichStockDetailsWithEquityData(List<NseStockDetails> stockDetails) {
        return stockDetails.stream()
                .map(stock -> {
                    Optional<EquityDataEntity> equityData = findBySymbol(stock.getSymbol());
                    equityData.ifPresent(equity -> {
                        stock.setIndustry(equity.getIndustry());
                        stock.setCompanyName(equity.getName());
                    });
                    return stock;
                })
                .collect(Collectors.toList());
    }
}
