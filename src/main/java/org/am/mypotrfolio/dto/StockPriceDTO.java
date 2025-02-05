package org.am.mypotrfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceDTO {
    private String symbol;
    private Double lastPrice;
    private Double previousClose;
    private Double change;
    private Double changePercent;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
}
