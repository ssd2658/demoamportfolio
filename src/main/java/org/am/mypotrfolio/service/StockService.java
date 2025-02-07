package org.am.mypotrfolio.service;

import lombok.RequiredArgsConstructor;
import org.am.mypotrfolio.dto.StockPriceDTO;
import org.am.mypotrfolio.mapper.StockPriceMapper;
import org.am.mypotrfolio.repo.StockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class StockService {
    
    private final StockRepository stockRepository;
    private final StockPriceMapper stockPriceMapper;

    public StockPriceDTO getLastStockPrice(String isin) {
        return stockRepository.findLatestByIsin(isin)
                .map(stockPriceMapper::mapFromEntity)
                .orElse(null);
    }

    public StockPriceDTO getHistoricalPrice(String isin, LocalDateTime startDate) {
        ZonedDateTime zonedStartDate = startDate.atZone(ZoneId.systemDefault());
        return stockRepository.findHistoricalPrices(isin, zonedStartDate)
                .map(stockPriceMapper::mapFromEntity)
                .orElse(null);
    }
}
