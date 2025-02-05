package org.am.mypotrfolio.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "stocks")
public class StockEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String symbol;

    private String isin;
    
    @Column(name = "exchange", nullable = false)
    private String exchange;
    
    @Column(name = "instrument_id")
    private String instrumentId;
    
    // Price Information
    @Column(name = "last_price")
    private Double lastPrice;
    
    @Column(name = "previous_close")
    private Double previousClose;
    
    @Column(name = "price_change")
    private Double change;
    
    @Column(name = "change_percent")
    private Double changePercent;
    
    // OHLC
    @Column(name = "open_price")
    private Double openPrice;
    
    @Column(name = "high_price")
    private Double highPrice;
    
    @Column(name = "low_price")
    private Double lowPrice;
    
    @Column(name = "close_price")
    private Double closePrice;
    
    // Volume Information
    private Long volume;
    
    @Column(name = "average_price")
    private Double averagePrice;
    
    // Market Depth Summary
    @Column(name = "total_buy_quantity")
    private Double totalBuyQuantity;
    
    @Column(name = "total_sell_quantity")
    private Double totalSellQuantity;
    
    // Circuit Limits
    @Column(name = "upper_circuit")
    private Double upperCircuitLimit;
    
    @Column(name = "lower_circuit")
    private Double lowerCircuitLimit;
    
    // Timestamps
    @Column(name = "last_update_time")
    private ZonedDateTime lastUpdateTime;
    
    @Column(name = "last_trade_time")
    private ZonedDateTime lastTradeTime;
    
    // Audit fields
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    // Price Changes
    @Column(name = "change_5min")
    private Double change5Min;
    
    @Column(name = "change_10min")
    private Double change10Min;
    
    @Column(name = "change_15min")
    private Double change15Min;
    
    @Column(name = "change_1hour")
    private Double change1Hour;
    
    @Column(name = "change_1day")
    private Double change1Day;
    
    // Closing Prices for Different Timeframes
    @Column(name = "close_price_5min")
    private Double closePrice5Min;
    
    @Column(name = "close_price_10min")
    private Double closePrice10Min;
    
    @Column(name = "close_price_15min")
    private Double closePrice15Min;
    
    @Column(name = "close_price_1hour")
    private Double closePrice1Hour;
    
    @Column(name = "close_price_1day")
    private Double closePrice1Day;
    
    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
} 