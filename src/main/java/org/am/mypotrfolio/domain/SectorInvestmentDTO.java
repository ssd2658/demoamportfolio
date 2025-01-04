package org.am.mypotrfolio.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectorInvestmentDTO {
    private String sector;
    private double totalInvestedAmount;
    private double totalCurrentAmount;
    private double overAllPNL;
}
