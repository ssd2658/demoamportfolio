package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.dto.StockPriceDTO;
import org.am.mypotrfolio.entity.StockEntity;
import org.am.mypotrfolio.mapper.StockPriceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, UUID> {
    
    @Query("SELECT s FROM StockEntity s WHERE s.isin = :isin AND s.createdAt = " +
           "(SELECT MAX(s2.createdAt) FROM StockEntity s2 WHERE s2.isin = :isin)")
    Optional<StockEntity> findLatestByIsin(@Param("isin") String isin);
    
    @Query(value = "WITH time_diff AS (" +
           "  SELECT *, " +
           "    EXTRACT(EPOCH FROM (created_at - :startDate)) as diff_seconds " +
           "  FROM stocks " +
           "  WHERE isin = :isin" +
           ") " +
           "SELECT * FROM time_diff " +
           "ORDER BY ABS(diff_seconds) " +
           "LIMIT 1", nativeQuery = true)
    Optional<StockEntity> findHistoricalPrices(@Param("isin") String isin, 
                                          @Param("startDate") ZonedDateTime startDate);
}
