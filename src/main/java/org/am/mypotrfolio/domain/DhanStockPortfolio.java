package org.am.mypotrfolio.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
public class DhanStockPortfolio {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Quantity")
    private int quantity;

    @JsonProperty("Avg Price")
    private double avgPrice;

    @JsonProperty("Last Traded")
    private double lastTraded;

    @JsonProperty("Investment")
    private double investment;

    @JsonProperty("Current Value")
    private double currentValue;

    @JsonProperty("P&L")
    private double profitLoss;

    @JsonProperty("P&L %")
    private double profitLossPercentage;
}
