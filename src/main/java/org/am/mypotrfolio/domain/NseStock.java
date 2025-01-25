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
public class NseStock {
    private String symbol;
    private double quantity;
    private double avePrice;
    private double investedValue;
    private double currentValue;
    private double currentPrice;
    private double profitLoss;
    private double openPrice;
    private UUID id;

    @JsonIgnore
    private String brokerPlatform;
    @JsonIgnore
    private String tradeType;
    @JsonIgnore
    private String userId;
}
