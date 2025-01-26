package org.am.mypotrfolio.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class SectorInvestmentDTO {
    private String sector;
    private double totalInvestedAmount;
    private double totalCurrentAmount;
    private double overAllPNL;
    private double totalQuantity;

    public SectorInvestmentDTO(String sector, double totalInvestedAmount) {
        this.sector = sector;
        this.totalInvestedAmount = totalInvestedAmount;
    }

    public SectorInvestmentDTO(String sector, double totalInvestedAmount, double totalQuantity) {
        this.sector = sector;
        this.totalInvestedAmount = totalInvestedAmount;
        this.totalQuantity = totalQuantity;
    }

    public double getPercentageOfPortfolio() {
        return totalInvestedAmount / (totalInvestedAmount + 1) * 100; // Prevent division by zero
    }

    public double getTotalInvestedAmount() {
        return totalInvestedAmount;
    }

    public double getTotalQuantity() {
        return totalQuantity;
    }

    public String getSector() {
        return sector;
    }
}
