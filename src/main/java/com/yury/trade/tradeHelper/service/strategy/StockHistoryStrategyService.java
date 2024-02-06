package com.yury.trade.tradeHelper.service.strategy;

import com.yury.trade.tradeHelper.chart.LineChartDataset;
import com.yury.trade.tradeHelper.entity.Option;
import com.yury.trade.tradeHelper.entity.StockHistory;
import com.yury.trade.tradeHelper.repository.StockHistoryRepository;
import com.yury.trade.tradeHelper.service.ChartService;
import com.yury.trade.tradeHelper.service.AbstractOptionService;
import com.yury.trade.tradeHelper.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StockHistoryStrategyService {

    @Autowired
    private AbstractOptionService optionService;
    @Autowired
    private ChartService chartService;
    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    public void runStrategy(Strategy strategy,
                            String symbol,
                            Map<Date, List<Option>> optionsMap,
                            List<LineChartDataset> datasets
    ) {
        Map<Date, Integer> balanceMap = new LinkedHashMap<>();

        Position position = new Position();

        List<Trade> trades = new ArrayList<>();

        LineChartDataset dataset = new LineChartDataset(strategy.getName());

        List<StockHistory> stockHistories = stockHistoryRepository.findByStockHistoryIdSymbol(symbol);

        Map<Date, StockHistory> stockHistoryMap = new LinkedHashMap<>();
        for (StockHistory stockHistory : stockHistories) {
            stockHistoryMap.put(stockHistory.getStockHistoryId().getDate(), stockHistory);
        }

        StockHistory previousStockHistory = null;

        for (Date date : optionsMap.keySet()) {

            System.out.println(date);
            List<Option> dateOptions = optionsMap.get(date);

            if (position.getItems().size() > 0) {
                position.update(dateOptions);
                System.out.println(position);
            }

            if (shouldClosePosition(strategy, position, date)) {
                Trade trade = trades.get(trades.size() - 1);
                trade.setEndDate(date);
                trade.setEndStockPrice(dateOptions.get(0).getActiveUnderlyingPrice());
                trade.setProfit((int) position.getBalance() - balanceMap.get(trade.getStartDate()));
                position.close();

                System.out.println("Closing position. Profit = " + trade.getProfit());
                System.out.println();
            }
            if (shouldAdd(strategy, position, date)) {

                add(stockHistoryMap.get(date), previousStockHistory, position, dateOptions);

                Trade trade = new Trade();
                trade.setStockSymbol(symbol);
                trade.setStartDate(new Date(date.getTime()));
                trade.setStartStockPrice(dateOptions.get(0).getActiveUnderlyingPrice());
                trades.add(trade);
                System.out.println(position);
            }

            balanceMap.put(date, (int) position.getBalance());
            dataset.add(balanceMap.get(date));

            System.out.println("Balance = " + balanceMap.get(date));

            previousStockHistory = stockHistoryMap.get(date);
        }
        datasets.add(dataset);

        System.out.println();
        System.out.println("Strategy=" + strategy.getName());
        System.out.println(TradeInfo.getInfo(trades));
    }

    private boolean shouldAdd(Strategy strategy, Position position, Date date) {

        int dayOfWeek = Utils.getDayOfWeek(date);

        if (strategy.getBuyDays().contains(dayOfWeek)) {
            return true;
        }
        return position.getItems().size() == 0;
    }

    private void add(StockHistory stockHistory, StockHistory previousStockHistory, Position position, List<Option> options) {

        if (previousStockHistory == null) {
            return;
        }

        int minDiffNeeded = 0;

        Strategy.Leg leg = null;

        int coeff = 1;
        int delta = 60;
        int expDays = 1;

        if (stockHistory.getClose() - previousStockHistory.getClose() > minDiffNeeded) {
            leg = new Strategy.Leg(coeff, Option.OptionType.call, delta, expDays);
        } else if (stockHistory.getClose() - previousStockHistory.getClose() < -minDiffNeeded) {
            leg = new Strategy.Leg(coeff, Option.OptionType.put, delta, expDays);
        }

        if (leg != null) {
            Option option = (Option) optionService.getClosestByDelta(leg, options);
            position.add(option, leg);
        }
    }

    private boolean shouldClosePosition(Strategy strategy, Position position, Date date) {

        if (position.getItems().size() == 0) {
            return false;
        }

        int dayOfWeek = Utils.getDayOfWeek(date);

        if (strategy.getSellDays().contains(dayOfWeek)) {
            return true;
        }

        for (Position.Item item : position.getItems()) {
            if (item.getOption().getTradingDaysLeft() <= item.getLeg().getMinTradingDaysLeftToExit()) {
                return true;
            }
        }
        return false;
    }

}
