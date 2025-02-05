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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Query("SELECT new org.am.mypotrfolio.domain.NseStockDetails(" +
           "n.symbol, " +
           "n.isin, " +
           "SUM(n.quantity), " +
           "SUM(n.investedValue), " +
           "CASE WHEN SUM(n.quantity) > 0 THEN SUM(n.investedValue) / SUM(n.quantity) ELSE 0 END, " +
           "e.industry, " +
           "e.name, " +
           "COALESCE(s.closePrice, CASE WHEN SUM(n.quantity) > 0 THEN SUM(n.investedValue) / SUM(n.quantity) ELSE 0 END), " +  
           "COALESCE(s.closePrice * SUM(n.quantity), SUM(n.investedValue))) " +  
           "FROM NseStockEntity n " +
           "LEFT JOIN EquityDataEntity e ON n.symbol = e.symbol " +
           "LEFT JOIN StockEntity s ON n.symbol = s.symbol " +
           "AND s.createdAt = (SELECT MAX(s2.createdAt) FROM StockEntity s2 WHERE s2.symbol = n.symbol) " +
           "WHERE n.userId = :userId " +
           "AND n.createdDate IN (" +
           "    SELECT MAX(n2.createdDate) " +
           "    FROM NseStockEntity n2 " +
           "    WHERE n2.userId = n.userId " +
           "    AND n2.brokerPlatform = n.brokerPlatform " +
           "    AND n2.symbol = n.symbol " +
           "    GROUP BY n2.brokerPlatform, n2.symbol" +
           ") " +
           "GROUP BY n.symbol, n.isin, e.industry, e.name, s.closePrice")
    List<NseStockDetails> getAggregatedStocksByUserId(@Param("userId") String userId);

    //     public NseStockDetails(String symbol, String isin, double quantity, double investedValue, 
    //     double avePrice, String brokerPlatforms, String industry, String companyName)

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
