package org.am.mypotrfolio.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    private List<NseStockDetails> stocks;
    private double totalInvestment;
    private double totalCurrentValue;
    private double totalProfitLoss;
    private double totalReturnPercentage;
    
    // Time-based changes
    private double oneDayProfitLoss;
    private double oneDayReturnPercentage;
    private double oneMonthProfitLoss;
    private double oneMonthReturnPercentage;
    private double oneYearProfitLoss;
    private double oneYearReturnPercentage;
    
    // Top performers and losers
    private List<NseStockDetails> topPerformers;  // Top 5 stocks by return
    private List<NseStockDetails> topLosers;      // Bottom 5 stocks by return
    
    private int totalStocks;
    private int totalIndustries;
}
