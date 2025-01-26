package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.NseStockDetails;
import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.entity.EquityDataEntity;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.am.mypotrfolio.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public interface NseStockRepository extends JpaRepository<NseStockEntity, UUID> {

    // @Query("SELECT new org.am.mypotrfolio.domain.SectorInvestmentDTO(c.sector, SUM(n.investedValue)) " +
    //         "FROM NseStockEntity n " +
    //         "JOIN StockEntity c ON n.symbol = c.symbol " +
    //         "GROUP BY c.sector")
    // List<SectorInvestmentDTO> findTotalInvestedBySector();

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

    default List<NseStockDetails> getInvestedStock() {
        List<NseStockEntity> nseStocks = getAllNseStocks();
        
        return nseStocks.stream().map((NseStockEntity n) -> {
            StockEntity matchingStock = getLastStockEntity(n.getSymbol());

            // Skip mapping if matchingStock is null
            if (matchingStock == null) {
                return NseStockDetails.builder()
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
}
