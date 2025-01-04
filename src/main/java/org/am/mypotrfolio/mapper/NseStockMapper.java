package org.am.mypotrfolio.mapper;

import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.entity.NseStockEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NseStockMapper {

    NseStockMapper INSTANCE = Mappers.getMapper(NseStockMapper.class);

    NseStock mapNseStock(NseStockEntity nseStockEntity);

    NseStockEntity mapNseStockEntity(NseStock nseStock);
}
