package org.am.mypotrfolio.repo;

import java.util.UUID;

import org.am.mypotrfolio.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {
    CompanyEntity findBySymbol(String symbol);
}
