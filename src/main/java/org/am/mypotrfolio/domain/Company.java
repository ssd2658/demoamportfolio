package org.am.mypotrfolio.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Data
@Getter
@Setter
public class Company {
    String symbol;
    String companyName;
    double marketCapital;
    String sector;
}
