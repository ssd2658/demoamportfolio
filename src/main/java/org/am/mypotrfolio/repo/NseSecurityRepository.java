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

    @Query("SELECT n FROM NseSecurityEntity n WHERE n.securityId = :securityId")
    Optional<NseSecurityEntity> findBySecurityId(@Param("securityId") String securityId);

    @Query("""
            SELECT n FROM NseSecurityEntity n 
            WHERE (
                :searchParam = n.isin OR 
                :searchParam = n.securityId OR 
                LOWER(n.securityName) LIKE LOWER(CONCAT('%', :searchParam, '%'))
            )
            AND n.status = 'Active'
            ORDER BY 
                CASE 
                    WHEN n.isin = :searchParam THEN 1
                    WHEN n.securityId = :searchParam THEN 2
                    WHEN LOWER(n.securityName) = LOWER(:searchParam) THEN 3
                    ELSE 4
                END,
                LENGTH(n.securityName)
            """)
    List<NseSecurityEntity> findSecurityBySearchParam(@Param("searchParam") String searchParam);

    default Optional<NseSecurityEntity> findBestMatchBySearchParam(String searchParam) {
        if (searchParam == null || searchParam.trim().isEmpty()) {
            return Optional.empty();
        }
        List<NseSecurityEntity> matches = findSecurityBySearchParam(searchParam.trim());
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
    }
}