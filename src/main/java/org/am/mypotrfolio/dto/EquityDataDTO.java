package org.am.mypotrfolio.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquityDataDTO {
    private String symbol;
    private String name;
    private Double marketCap;
    private String series;
    private String isin;
    private Double faceValue;
    private String industry;
    private String instrumentType;
} 