package com.yury.trade.tradeHelper.request;

import lombok.Data;

@Data
public class RunStrategyRequest {
    private String symbol;
    private String startDate;
    private String endDate;
    private boolean drawChart;
    private boolean debug;
}
