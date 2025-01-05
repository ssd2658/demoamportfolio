package org.am.mypotrfolio.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ZerodhaStockPortfolio {

    @JsonProperty("Symbol")
    private String symbol;
    
    @JsonProperty("ISIN")
    private String isin;
    
    @JsonProperty("Sector")
    private String sector;
    
    @JsonProperty("Quantity Available")
    private int quantityAvailable;
    
    @JsonProperty("Quantity Discrepant")
    private int quantityDiscrepant;
    
    @JsonProperty("Quantity Long Term")
    private int quantityLongTerm;
    
    @JsonProperty("Quantity Pledged (Margin)")
    private int quantityPledgedMargin;
    
    @JsonProperty("Quantity Pledged (Loan)")
    private int quantityPledgedLoan;
    
    @JsonProperty("Average Price")
    private double averagePrice;
    
    @JsonProperty("Previous Closing Price")
    private double previousClosingPrice;
}
