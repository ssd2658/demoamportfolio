package org.am.mypotrfolio.repo;

import org.am.mypotrfolio.entity.MutualFundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MutualFundRepository extends JpaRepository<MutualFundEntity, UUID> {

}
