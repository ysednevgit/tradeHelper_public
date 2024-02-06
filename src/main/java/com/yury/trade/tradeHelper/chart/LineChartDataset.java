package com.yury.trade.tradeHelper.chart;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class LineChartDataset {

    private String name;
    private Map<Integer, Integer> data = new LinkedHashMap<>();

    public LineChartDataset(String name) {
        this.name = name;
    }

    public void add(Integer item) {
        data.put(data.size() + 1, item);
    }

}
