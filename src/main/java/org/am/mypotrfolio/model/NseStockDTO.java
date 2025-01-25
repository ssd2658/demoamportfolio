package org.am.mypotrfolio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NseStockDTO {
    // From NseStock
    private String symbol;
    
    // From EquityData
    private String name;
    private String series;
    private String isin;
    private Double faceValue;
    private String industry;
} 