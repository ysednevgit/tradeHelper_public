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
public class RatioDiagonalStrategyService {

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

        Position position = new Position();

        List<Trade> trades = new ArrayList<>();

        LineChartDataset dataset = new LineChartDataset(strategy.getName());

        for (Date date : optionsMap.keySet()) {

            System.out.println(date);

            List<Option> dateOptions = optionsMap.get(date);

            if (position.getItems().size() > 0) {
                position.update(dateOptions);
                System.out.println(position);
            }
            roll(position, dateOptions);

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

                add(strategy, position, dateOptions);

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

    private void roll(Position position, List<Option> options) {

        if (position.getItems().size() == 0) {
            return;
        }

        List<Position.Item> itemsToClose = new ArrayList<>();
        List<Position.Item> itemsToRemove = new ArrayList<>();
        List<Position.Item> itemsToAdd = new ArrayList<>();
        List<Option> optionsToAdd = new ArrayList<>();

        for (Position.Item item : position.getItems()) {

            Strategy.RollingStrategy rollingStrategy = item.getLeg().getRollingStrategy();

            if (item.getOption().getTradingDaysLeft() <= item.getLeg().getMinTradingDaysLeftToExit() && rollingStrategy != null) {

                if (item.getLeg().isDoNotRollIfExpiredOOTM() && ((Option) item.getOption()).expiredOutOfTheMoney()) {
                    System.out.println("No rolling needed");

                    itemsToRemove.add(item);
                    continue;
                }

                Option option = null;
                if (rollingStrategy.equals(Strategy.RollingStrategy.ROLL_SAME_DELTA)) {
                    option = (Option) optionService.getClosestByDelta(item.getLeg(), options);
                } else if (rollingStrategy.equals(Strategy.RollingStrategy.ROLL_SAME_STRIKE)) {
                    option = (Option) optionService.getClosestByStrike(item.getLeg(), item.getOption().getStrike(), options);
                }
                if (option != null) {
                    System.out.println("Rolling..");

                    double rollPrice = option.getMidPrice() - item.getOption().getMidPrice();
                    int longLegsCoeff = (int) (rollPrice / position.getItems().get(0).getOption().getMidPrice());

                    System.out.println("Roll price = " + rollPrice + " LongLegsCoeff = " + longLegsCoeff);

                    if (longLegsCoeff == 0) {
                        option = (Option) optionService.getClosestByStrike(item.getLeg(), item.getOption().getStrike(), options);
                    } else {
                        position.add(position.getItems().get(0), longLegsCoeff);
                    }

                    itemsToAdd.add(item);
                    optionsToAdd.add(option);
                    itemsToClose.add(item);
                }
            }
        }

        for (Position.Item item : itemsToClose) {
            position.closeItem(item);
        }

        for (Position.Item item : itemsToRemove) {
            position.removeItem(item);
        }

        for (int i = 0; i < optionsToAdd.size(); i++) {
            position.add(optionsToAdd.get(i), itemsToAdd.get(i).getLeg());
        }
    }


}
