package org.am.mypotrfolio.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrokerPortfolioSummary {
    private String brokerPlatform;
    private double totalInvested;
    private double currentValue;
    private double profitLoss;
    private double percentageChange;
}
