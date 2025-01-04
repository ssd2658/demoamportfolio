package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NseStockRepository extends JpaRepository<NseStockEntity, String> {

    @Query("SELECT new org.am.mypotrfolio.domain.SectorInvestmentDTO(c.sector, SUM(n.investedValue), SUM(n.currentValue), SUM(n.overAllPNL)) " +
            "FROM NseStockEntity n " +
            "JOIN CompanyEntity c ON n.symbol = c.symbol " +
            "GROUP BY c.sector")
    List<SectorInvestmentDTO> findTotalInvestedBySector();
}