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

    @Query(value = 
           "SELECT n.symbol, " +
           "       n.isin, " +
           "       SUM(n.quantity) as quantity, " +
           "       SUM(n.invested_value) as invested_value, " +
           "       CASE WHEN SUM(n.quantity) > 0 THEN SUM(n.invested_value) / SUM(n.quantity) ELSE 0 END as avg_price, " +
           "       STRING_AGG(DISTINCT n.broker_platform, ',') as broker_platforms, " +
           "       COALESCE(e.industry, '') as industry, " +
           "       COALESCE(e.name, '') as company_name, " +
           "       COALESCE(s.close_price, 0.0) as current_price, " +
           "       COALESCE(s.close_price * SUM(n.quantity), SUM(n.invested_value)) as current_value " +
           "FROM nse_stock n " +
           "LEFT JOIN equity_data e ON n.symbol = e.symbol " +
           "LEFT JOIN stocks s ON n.symbol = s.symbol " +
           "AND s.created_at = (SELECT MAX(s2.created_at) FROM stocks s2 WHERE s2.symbol = n.symbol) " +
           "WHERE n.user_id = :userId " +
           "AND n.created_date IN ( " +
           "    SELECT MAX(n2.created_date) " +
           "    FROM nse_stock n2 " +
           "    WHERE n2.user_id = n.user_id " +
           "    AND n2.broker_platform = n.broker_platform " +
           "    AND n2.symbol = n.symbol " +
           "    GROUP BY n2.broker_platform, n2.symbol " +
           ") " +
           "GROUP BY n.symbol, n.isin, e.industry, e.name, s.close_price",
           nativeQuery = true)
    List<Object[]> getAggregatedStocksByUserIdNative(@Param("userId") String userId);

    default List<NseStockDetails> getAggregatedStocksByUserId(String userId) {
        List<Object[]> results = getAggregatedStocksByUserIdNative(userId);
        return results.stream()
            .map(row -> new NseStockDetails(
                (String) row[0],                    // symbol
                (String) row[1],                    // isin
                ((Number) row[2]).doubleValue(),    // quantity
                ((Number) row[3]).doubleValue(),    // invested_value
                ((Number) row[4]).doubleValue(),    // avg_price
                (String) row[5],                    // broker_platforms
                (String) row[6],                    // industry
                (String) row[7],                    // company_name
                ((Number) row[8]).doubleValue(),    // current_price
                ((Number) row[9]).doubleValue()     // current_value
            ))
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
