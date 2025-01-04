package org.am.mypotrfolio.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Holding {
    private String schemeName;
    private String amc;
    private String category;
    private String subCategory;
    private String folioNo;
    private String source;
    private double units;
    private double investedValue;
    private double currentValue;
    private double returns;
    private double xirr;
}
