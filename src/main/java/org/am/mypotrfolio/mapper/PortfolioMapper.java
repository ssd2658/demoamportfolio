package org.am.mypotrfolio.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.am.mypotrfolio.domain.Company;
import org.am.mypotrfolio.domain.DhanStockPortfolio;
import org.am.mypotrfolio.domain.MStockPortfolio;
import org.am.mypotrfolio.domain.NseStock;
import org.am.mypotrfolio.domain.ZerodhaStockPortfolio;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {
    PortfolioMapper INSTANCE = Mappers.getMapper(PortfolioMapper.class);

    @Mapping(source = "symbol", target = "symbol")
    @Mapping(source = "quantityAvailable", target = "quantity")
    @Mapping(source = "averagePrice", target = "avePrice")
    @Mapping(expression = "java(stockInfo.getQuantityAvailable() * stockInfo.getAveragePrice())", target = "investedValue")
    NseStock toNseStockFromZerodha(ZerodhaStockPortfolio stockInfo);

    @Mapping(source = "symbol", target = "symbol")
    @Mapping(source = "isin", target = "isin")
    @Mapping(source = "quantity", target = "quantity", qualifiedByName = "stringToDouble")
    @Mapping(source = "avgPrice", target = "avePrice", qualifiedByName = "stringToDouble")
    @Mapping(source = "investedValue", target = "investedValue", qualifiedByName = "stringToDouble")
    NseStock toNseStock(MStockPortfolio stockPortfolio);

    @Mapping(source = "securityId", target = "symbol")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "avgPrice", target = "avePrice")
    @Mapping(expression = "java(stock.getQuantity() * stock.getAvgPrice())", target = "investedValue")
    @Mapping(source = "isin", target = "isin")
    NseStock toNseStockFromDhan(DhanStockPortfolio stock);

    @Mapping(source = "securityId", target = "symbol")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "avgPrice", target = "avePrice")
    @Mapping(expression = "java(stock.getQuantity() * stock.getAvgPrice())", target = "investedValue")
    NseStock mapNseStock(DhanStockPortfolio stock);

    @Named("stringToDouble")
    default double stringToDouble(String value) {
        return value == null || value.isEmpty() ? 0.0 : Double.parseDouble(value);
    }

    @Named("getISIN")
    default String getISIN(String name) {
        return findKeyByValue(convertToSymbolMap(companies), name);
    }

    @Named("getSymbol")
    default String getSymbol(String name) {
        return findKeyByValue(convertToSymbolMap(companies), name);
    }

    default Map<String, String> convertToSymbolMap(List<Company> companies) {
        Map<String, String> symbolMap = new HashMap<>();
        companies.forEach(company -> symbolMap.put(company.getSymbol(), company.getCompanyName()));
        return symbolMap;
    }

    default String findKeyByValue(Map<String, String> map, String value) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
