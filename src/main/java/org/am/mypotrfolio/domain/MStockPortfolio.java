package org.am.mypotrfolio.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MStockPortfolio {
    @JsonProperty("Symbol")
    private String symbol;

    @JsonProperty("Quantity")
    private double quantity;

    @JsonProperty("Avg Price")
    private double avgPrice;

    @JsonProperty("LTP")
    private double ltp;

    @JsonProperty("Invested Value")
    private double investedValue;

    @JsonProperty("Current Value")
    private double currentValue;

    @JsonProperty("Overall P/L")
    private double overallPL;

    @JsonProperty("Day's P/L")
    private double daysPL;

    @JsonProperty("Overall P/L %")
    private double overallPLPercentage;

    @JsonProperty("Day's P/L %")
    private double daysPLPercentage;
}
