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
    @JsonIgnore
    private String brokerPlatform;
    @JsonIgnore
    private String isMarginTrade;
    @JsonIgnore
    private String userId;
}
