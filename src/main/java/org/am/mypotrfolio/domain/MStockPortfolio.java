package org.am.mypotrfolio.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MStockPortfolio {
    @JsonProperty("Symbol")
    private String symbol;

    @JsonProperty("Quantity")
    private double quantity;

    @JsonProperty("Avg Cost")
    private double avgPrice;

    @JsonProperty("isin")
    private String isin;

    @JsonProperty("securityId")
    private String securityId;

    @JsonProperty("Invested Value")
    private double investedValue;

}
