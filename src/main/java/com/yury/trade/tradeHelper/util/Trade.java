package com.yury.trade.tradeHelper.util;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class Trade {

    private String stockSymbol;

    private Date startDate;

    private Date endDate;

    private double startStockPrice;

    private double endStockPrice;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private double profit;

    @Override
    public String toString() {

        String endDateStr = endDate != null ? sdf.format(endDate) : "";

        return "Trade{" +
                stockSymbol + '\'' +
                ", startDate=" + sdf.format(startDate) +
                ", endDate=" + endDateStr +
                ", startStockPrice=" + startStockPrice +
                ", endStockPrice=" + endStockPrice +
                ", profit=" + profit +
                '}';
    }
}
