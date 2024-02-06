package com.yury.trade.tradeHelper.repository;

import com.yury.trade.tradeHelper.entity.StockHistory;
import com.yury.trade.tradeHelper.entity.StockHistoryId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface StockHistoryRepository extends CrudRepository<StockHistory, StockHistoryId> {

    @Query("SELECT s FROM StockHistory s WHERE stockHistoryId.symbol = ?1 ORDER BY stockHistoryId.date")
    List<StockHistory> findByStockHistoryIdSymbol(String symbol);

    @Query("SELECT s FROM StockHistory s WHERE stockHistoryId.symbol = ?1 AND stockHistoryId.date >= ?2  ORDER BY stockHistoryId.date")
    List<StockHistory> findByStockHistoryIdSymbolAndDate(String symbol, Date date);

}
