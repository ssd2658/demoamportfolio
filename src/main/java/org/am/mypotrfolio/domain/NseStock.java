package org.am.mypotrfolio.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NseStock {
    private String symbol;
    private double quantity;
    private double avePrice;
    private double investedValue;
    private double currentValue;
    private double currentPrice;
    private double profitLoss;

    @JsonIgnore
    private String brokerPlatform;
    @JsonIgnore
    private String tradeType;
    @JsonIgnore
    private String userId;

    // Constructor for JPQL query
    public NseStock(String symbol, double quantity, double avePrice, double investedValue, double currentPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.avePrice = avePrice;
        this.investedValue = investedValue;
        this.currentPrice = currentPrice;
        this.currentValue = quantity * currentPrice;
    }

    public double getTotalInvestment() {
        return quantity * avePrice;
    }

    public double getCurrentValue() {
        return quantity * currentPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getProfitLoss() {
        return getCurrentValue() - getTotalInvestment();
    }
}
