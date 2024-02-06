package com.yury.trade.tradeHelper.service;

import com.yury.trade.tradeHelper.chart.LineChartBuilder;
import com.yury.trade.tradeHelper.chart.LineChartDataset;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class ChartService {

    public void drawChart(final String symbol, final java.util.List<LineChartDataset> lineChartDatasets) {

        if (!GraphicsEnvironment.isHeadless()) {
            EventQueue.invokeLater(() -> {

                var ex = new LineChartBuilder(symbol, lineChartDatasets);
                ex.setVisible(true);
            });
        }
    }

}
