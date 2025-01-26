package org.am.mypotrfolio.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NseStockDetails {
    private String symbol;
    private String companyName;  
    private String industry;     
    private double quantity;
    private double avePrice;
    private double investedValue;
    private double currentValue;
    private double currentPrice;
    private double profitLoss;
    private double openPrice;
    private double percentChange;
    private double returnChange;
    private UUID id;

    @JsonIgnore
    private String brokerPlatform;
    @JsonIgnore
    private String tradeType;
    @JsonIgnore
    private String userId;

    // Constructor for JPQL query
    public NseStockDetails(String symbol, double quantity, double avePrice, double investedValue, Double currentPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.avePrice = avePrice;
        this.investedValue = investedValue;
        this.currentPrice = currentPrice != null ? currentPrice : avePrice;
        this.currentValue = quantity * this.currentPrice;
    }

    // Constructor for JPQL query
    public NseStockDetails(String symbol, double quantity, double avePrice, double investedValue, Double currentPrice, Double openPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.avePrice = avePrice;
        this.investedValue = investedValue;
        this.currentPrice = currentPrice != null ? currentPrice : avePrice;
        this.currentValue = quantity * this.currentPrice;
        this.openPrice = openPrice != null ? openPrice : this.currentPrice;
    }

    public double getTotalInvestment() {
        return Double.parseDouble(String.format("%.2f", quantity * avePrice));
    }

    public double getCurrentValue() {
        return Double.parseDouble(String.format("%.2f", quantity * currentPrice));
    }

    public double getCurrentPrice() {
        return Double.parseDouble(String.format("%.2f", currentPrice));
    }

    public double getProfitLoss() {
        return Double.parseDouble(String.format("%.2f", getCurrentValue() - getTotalInvestment()));
    }

    public double getOpenPrice() {
        return Double.parseDouble(String.format("%.2f", openPrice));
    }

    public double getPercentChange() {
        return Double.parseDouble(String.format("%.2f", percentChange));
    }

    public double getReturnChange() {
        return Double.parseDouble(String.format("%.2f", returnChange));
    }

    public void setPercentChange(double percentChange) {
        this.percentChange = percentChange;
    }

    public void setReturnChange(double returnChange) {
        this.returnChange = returnChange;
    }
}
