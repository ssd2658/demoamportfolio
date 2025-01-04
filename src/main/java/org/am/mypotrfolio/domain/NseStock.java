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
    private double currentValue;
    @JsonIgnore
    private double ltp;
    private double overAllPNL;

    @JsonIgnore
    private double daysPNL;
    @JsonIgnore
    private String daysPNLInPercentage;
    @JsonIgnore
    private String overAllPNLInPercentage;
    @JsonIgnore
    private String brokerPlatform;
    @JsonIgnore
    private String isMarginTrade;
    @JsonIgnore
    private String userId;
}
