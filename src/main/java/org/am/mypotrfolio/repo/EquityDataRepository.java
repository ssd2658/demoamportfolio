package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.entity.EquityDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquityDataRepository extends JpaRepository<EquityDataEntity, UUID> {
    Optional<EquityDataEntity> findByIsin(String isin);
    Optional<EquityDataEntity> findBySymbol(String symbol);
} 