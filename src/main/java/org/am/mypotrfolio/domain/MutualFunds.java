package org.am.mypotrfolio.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MutualFunds {

    private String ISIN;
    private String amcCode;
    private String schemeName;
    private String schemeType;
    private String subCategory;
    private double marketCap;
}
