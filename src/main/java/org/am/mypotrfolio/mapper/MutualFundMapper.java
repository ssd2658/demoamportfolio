package org.am.mypotrfolio.mapper;

import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.MutualFunds;
import org.am.mypotrfolio.entity.CompanyEntity;
import org.am.mypotrfolio.entity.MutualFundEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MutualFundMapper {

    MutualFundMapper INSTANCE = Mappers.getMapper(MutualFundMapper.class);

    MutualFunds MUTUAL_FUNDS(MutualFundEntity mutualFundEntity);

    MutualFundEntity MUTUAL_FUND_ENTITY(MutualFunds mutualFunds);
}
