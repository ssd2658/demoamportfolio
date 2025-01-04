package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.domain.SectorInvestmentDTO;
import org.am.mypotrfolio.entity.MutualFundEntity;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MutualFundRepository extends JpaRepository<MutualFundEntity, String> {

}
