package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.entity.CompanyEntity;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {
    CompanyEntity findBySymbol(String symbol);
}
