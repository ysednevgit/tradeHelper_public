package com.yury.trade.tradeHelper.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class StockHistory {

    @EmbeddedId
    StockHistoryId stockHistoryId;

    Double open;
    Double high;
    Double low;
    Double close;
    Long volume;
}