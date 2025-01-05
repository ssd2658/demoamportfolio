package org.am.mypotrfolio.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
public class Company {
    String symbol;
    String companyName;
    double marketCapital;
    String sector;
}
