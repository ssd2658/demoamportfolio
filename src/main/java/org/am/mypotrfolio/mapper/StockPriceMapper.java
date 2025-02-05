package org.am.mypotrfolio.mapper;

import org.am.mypotrfolio.dto.StockPriceDTO;
import org.am.mypotrfolio.entity.StockEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class StockPriceMapper implements RowMapper<StockPriceDTO> {
    
    @Override
    public StockPriceDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return StockPriceDTO.builder()
            .symbol(rs.getString("symbol"))
            .lastPrice(rs.getDouble("last_price"))
            .previousClose(rs.getDouble("previous_close"))
            .change(rs.getDouble("change"))
            .changePercent(rs.getDouble("change_percent"))
            .openPrice(rs.getDouble("open_price"))
            .highPrice(rs.getDouble("high_price"))
            .lowPrice(rs.getDouble("low_price"))
            .build();
    }

    public StockPriceDTO mapFromEntity(StockEntity entity) {
        if (entity == null) return null;
        return StockPriceDTO.builder()
            .symbol(entity.getSymbol())
            .lastPrice(entity.getLastPrice())
            .previousClose(entity.getPreviousClose())
            .change(entity.getChange())
            .changePercent(entity.getChangePercent())
            .openPrice(entity.getOpenPrice())
            .highPrice(entity.getHighPrice())
            .lowPrice(entity.getLowPrice())
            .build();
    }
}
