package org.am.mypotrfolio.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class NseStock {
    private String symbol;
    private double quantity;
    private double avePrice;
    private double investedValue;
    private double currentPrice;
    private double buyPrice;
    @JsonIgnore
    private String brokerPlatform;
    @JsonIgnore
    private String isMarginTrade;
    @JsonIgnore
    private String userId;

    public double getTotalInvestment() {
        return quantity * avePrice;
    }

    public double getCurrentValue() {
        return quantity * avePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getBuyPrice() {
        return avePrice;
    }

    public double getAvePrice() {
        return avePrice;
    }

    public double getProfitLoss() {
        return (currentPrice - avePrice) * quantity;
    }
}
