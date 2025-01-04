package org.am.mypotrfolio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "mutual_fund_statements")
public class MutualFundStatementsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String ISIN;
    private String brokerPlatform;
    private double quantity;
    private double avePrice;
    private double investedValue;
    private double currentValue;
    private double ltp;
    private double overAllPNL;
    private double daysPNL;
    private String daysPNLInPercentage;
    private String overAllPNLInPercentage;
    private String isMarginTrade;
    private String userId;
    private LocalDateTime createdDate;
}