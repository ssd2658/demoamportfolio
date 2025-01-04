package org.am.mypotrfolio.mapper;

import org.am.mypotrfolio.entity.CompanyEntity;
import org.am.mypotrfolio.domain.Company;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);

    Company COMPANY(CompanyEntity companyEntity);

    CompanyEntity COMPANY_ENTITY(Company company);
}
