package com.yury.trade.tradeHelper.service.strategy;

import com.yury.trade.tradeHelper.chart.LineChartDataset;
import com.yury.trade.tradeHelper.entity.Option;
import com.yury.trade.tradeHelper.service.AbstractOptionService;
import com.yury.trade.tradeHelper.service.ChartService;
import com.yury.trade.tradeHelper.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeapsStrategyService {

    @Autowired
    private AbstractOptionService optionService;

    @Autowired
    private ChartService chartService;

    public void runStrategy(Strategy strategy,
                            String symbol,
                            Map<Date, List<Option>> optionsMap,
                            List<LineChartDataset> datasets
    ) {
        Map<Date, Integer> balanceMap = new LinkedHashMap<>();

        List<Position> positions = new ArrayList<>();
        positions.add(new Position());

        List<Trade> trades = new ArrayList<>();

        LineChartDataset dataset = new LineChartDataset(strategy.getName());

        double initialStockPrice = 0;
        int dropNeededToOpenNewPosition = 30;

        for (Date date : optionsMap.keySet()) {

            System.out.println(date);
            List<Option> dateOptions = optionsMap.get(date);

            double activeUnderlyingPrice = dateOptions.get(0).getActiveUnderlyingPrice();

            if (initialStockPrice == 0) {
                initialStockPrice = activeUnderlyingPrice;
            }

            if (activeUnderlyingPrice <= initialStockPrice - dropNeededToOpenNewPosition) {
                positions.add(new Position());
                initialStockPrice = initialStockPrice - dropNeededToOpenNewPosition;
                System.out.println("Opening new position because stock dropped >= " + dropNeededToOpenNewPosition + " from " + initialStockPrice);
            }

            Iterator<Position> positionsIterator = positions.listIterator();

            while (positionsIterator.hasNext()) {
                Position position = positionsIterator.next();

                if (position.getItems().size() > 0) {
                    position.update(dateOptions);
                    System.out.println(position);
                }
                if (shouldClosePosition(strategy, position, date)) {
                    Trade trade = trades.get(trades.size() - 1);
                    trade.setEndDate(date);
                    trade.setEndStockPrice(activeUnderlyingPrice);
                    trade.setProfit((int) position.getBalance() - balanceMap.get(trade.getStartDate()));
                    position.close();

                    System.out.println("Closing position. Profit = " + trade.getProfit());
                    System.out.println();
                    positionsIterator.remove();
                }
                if (shouldAdd(strategy, position, date)) {

                    add(strategy, position, dateOptions);

                    Trade trade = new Trade();
                    trade.setStockSymbol(symbol);
                    trade.setStartDate(new Date(date.getTime()));
                    trade.setStartStockPrice(activeUnderlyingPrice);
                    trades.add(trade);
                    System.out.println(position);
                }

                if (balanceMap.containsKey(date)) {
                    balanceMap.put(date, balanceMap.get(date) + (int) position.getBalance());
                } else {
                    balanceMap.put(date, (int) position.getBalance());
                }
            }
            dataset.add(balanceMap.get(date));

            System.out.println("Balance = " + balanceMap.get(date));
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

    private void add(Strategy strategy, Position position, List<Option> options) {
        for (Strategy.Leg leg : strategy.getLegs()) {

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