package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.entity.NseSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NseSecurityRepository extends JpaRepository<NseSecurityEntity, UUID> {
    @Query("SELECT n FROM NseSecurityEntity n WHERE n.isin = :isin AND n.status = 'Active'")
    Optional<NseSecurityEntity> findByIsin(@Param("isin") String isin);

    @Query("SELECT n FROM NseSecurityEntity n WHERE LOWER(n.securityName) = LOWER(:securityName) AND n.status = 'Active'")
    Optional<NseSecurityEntity> findBySecurityName(@Param("securityName") String securityName);

    @Query("SELECT n FROM NseSecurityEntity n WHERE LOWER(n.securityName) LIKE LOWER(CONCAT('%', :partialName, '%')) AND n.status = 'Active' ORDER BY LENGTH(n.securityName)")
    List<NseSecurityEntity> findBySecurityNameFuzzy(@Param("partialName") String partialName);
}