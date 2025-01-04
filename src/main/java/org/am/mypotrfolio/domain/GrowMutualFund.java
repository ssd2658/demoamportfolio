package org.am.mypotrfolio.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GrowMutualFund {
    // Personal Details (optional, as they are not consistently shown in the image)
    private User user;
    // Holding Summary
    private double totalInvestments;
    private double currentPortfolioValue;
    private double profitLoss;
    private double profitLossPercentage;
    private double xirr;

    // Holdings (List of individual holdings)
    private List<Holding> holdings;
}
