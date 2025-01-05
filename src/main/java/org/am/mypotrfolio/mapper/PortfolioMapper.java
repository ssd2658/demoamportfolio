package org.am.mypotrfolio.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.DhanStockPortfolio;
import org.am.mypotrfolio.domain.MStockPortfolio;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import lombok.val;
import lombok.var;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {

    PortfolioMapper INSTANCE = Mappers.getMapper(PortfolioMapper.class);

    @Mapping(source = "symbol", target = "symbol")
    @Mapping(source = "quantity", target = "quantity", qualifiedByName = "stringToDouble")
    @Mapping(source = "avgPrice", target = "avePrice", qualifiedByName = "stringToDouble")
    @Mapping(source = "investedValue", target = "investedValue", qualifiedByName = "stringToDouble")
    @Mapping(source = "currentValue", target = "currentValue", qualifiedByName = "stringToDouble")
    @Mapping(source = "overallPL", target = "overAllPNL", qualifiedByName = "stringToDouble")
    @Mapping(source = "daysPL", target = "daysPNL", qualifiedByName = "stringToDouble")
    NseStock toNseStock(MStockPortfolio stockPortfolio);

    @Mapping(source = "name", target = "symbol", qualifiedByName = "getSymbol")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "avgPrice", target = "avePrice")
    @Mapping(source = "investment", target = "investedValue")
    @Mapping(source = "currentValue", target = "currentValue")
    @Mapping(source = "profitLoss", target = "overAllPNL")
    NseStock mapNseStock(DhanStockPortfolio dhanStockPortfolio, @Context List<Company> companies);

    @Named("stringToDouble")
    default double stringToDouble(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(value);
    }

    @Named("getSymbol")
    default String getSymbol(String name, @Context List<Company> companies){
        var company =  convertToSymbolMap(companies);
        return findKeyByValue(company, name);
    }

    default Map<String, String> convertToSymbolMap(List<Company> companies) {
        Map<String, String> symbolMap = new HashMap<>();
        for (Company company : companies) {
            symbolMap.put(company.getSymbol(), company.getCompanyName());
        }
        return symbolMap;
    }

    default String findKeyByValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null; // Or throw an exception if no match is found
    }
}