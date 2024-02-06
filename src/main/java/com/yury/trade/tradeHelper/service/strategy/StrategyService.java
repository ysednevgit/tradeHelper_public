package com.yury.trade.tradeHelper.service.strategy;

import com.yury.trade.tradeHelper.chart.LineChartDataset;
import com.yury.trade.tradeHelper.entity.Option;
import com.yury.trade.tradeHelper.repository.OptionRepository;
import com.yury.trade.tradeHelper.service.AbstractOptionService;
import com.yury.trade.tradeHelper.service.ChartService;
import com.yury.trade.tradeHelper.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class StrategyService {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private AbstractOptionService optionService;

    @Autowired
    private ChartService chartService;

    @Autowired
    private RatioDiagonalStrategyService ratioDiagonalStrategyService;

    @Autowired
    private LeapsStrategyService leapsStrategyService;

    @Autowired
    private StockHistoryStrategyService stockHistoryStrategyService;

    @Autowired
    private IntradayStrategyService intradayStrategyService;

    @Autowired
    private PutsIncreaseStrategyService putsIncreaseStrategyService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public void runStrategies(String symbol, String startDateString, String endDateString, boolean drawChart, boolean debug) throws ParseException {

        Date startDate = startDateString != null ? sdf.parse(startDateString) : null;
        Date endDate = endDateString != null ? sdf.parse(endDateString) : null;

        List<Strategy> strategies = new StrategyTester().getStrategiesToTest();

        Set<Option.OptionType> optionTypes = getOptionTypes(strategies);

        Map<Date, List<Option>> optionsMap;

        if (optionTypes.size() == 1) {
            optionsMap = toOptionsMap(optionRepository.find(symbol, optionTypes.iterator().next(), startDate, endDate));
        } else {
            optionsMap = toOptionsMap(optionRepository.findByUnderlyingSymbolAndDates(symbol, startDate, endDate));
        }

        List<LineChartDataset> datasets = new ArrayList<>();

//        List<Date> quoteDates = optionRepository.findQuoteDates(symbol, startDate, endDate);

        for (Strategy strategy : new StrategyTester().getStrategiesToTest()) {
            runStrategy(strategy, symbol, optionsMap, datasets);
        }

        if (drawChart) {
            chartService.drawChart(symbol + " " + startDateString + " " + endDateString, datasets);
        }
    }

    private void runStrategy(Strategy strategy,
                             String symbol,
                             Map<Date, List<Option>> optionsMap,
                             List<LineChartDataset> datasets
    ) throws ParseException {

        if (Strategy.StrategyType.RATIO_DIAGONAL.equals(strategy.getStrategyType())) {
            ratioDiagonalStrategyService.runStrategy(strategy, symbol, optionsMap, datasets);
            return;
        } else if (Strategy.StrategyType.STOCK_HISTORY.equals(strategy.getStrategyType())) {
            stockHistoryStrategyService.runStrategy(strategy, symbol, optionsMap, datasets);
            return;
        } else if (Strategy.StrategyType.LEAPS.equals(strategy.getStrategyType())) {
            leapsStrategyService.runStrategy(strategy, symbol, optionsMap, datasets);
            return;
        } else if (Strategy.StrategyType.INTRADAY.equals(strategy.getStrategyType())) {
            intradayStrategyService.runStrategy(strategy, symbol, optionsMap, datasets);
            return;
        } else if (Strategy.StrategyType.PUTS_INCREASE.equals(strategy.getStrategyType())) {
            putsIncreaseStrategyService.runStrategy(strategy, symbol, optionsMap, datasets);
            return;
        }

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

    private Set<Option.OptionType> getOptionTypes(List<Strategy> strategies) {
        Set<Option.OptionType> optionTypes = new HashSet<>();

        for (Strategy strategy : strategies) {
            if (optionTypes.size() == 2) {
                return optionTypes;
            }

            for (Strategy.Leg leg : strategy.getLegs()) {
                optionTypes.add(leg.getOptionType());
            }
        }
        return optionTypes;
    }

    private Map<Date, List<Option>> toOptionsMap(List<Option> options) {
        Map<Date, List<Option>> optionsMap = new TreeMap<>();

        for (Option option : options) {
            if (!optionsMap.containsKey(option.getQuoteDate())) {
                optionsMap.put(option.getQuoteDate(), new ArrayList<>());
            }
            optionsMap.get(option.getQuoteDate()).add(option);
        }
        return optionsMap;
    }

    private boolean shouldAdd(Strategy strategy, Position position, Date date) {

        int dayOfWeek = Utils.getDayOfWeek(date);

        if (strategy.getBuyDays().contains(dayOfWeek)) {
            return true;
        }

        if (strategy.getBuyDays().size() > 0) {
            return false;
        }

        return position.getItems().size() == 0;
    }

    private boolean add(Strategy strategy, Position position, List<Option> options) {

        List<Option> optionsToAdd = new ArrayList<>();

        for (Strategy.Leg leg : strategy.getLegs()) {

            Option option;

            if (leg.getDelta() == 0) {
                double strike = position.getItems().get(0).getOption().getStrike();
                option = (Option) optionService.getClosestByStrike(leg, strike, options);
            } else {
                option = (Option) optionService.getClosestByDelta(leg, options);
            }

            if (!leg.isFlexibleExpDays() && option.getTradingDaysLeft() != leg.getExpDays()) {
                return false;
            }

            optionsToAdd.add(option);
        }

        for (int i = 0; i < strategy.getLegs().size(); i++) {
            position.add(optionsToAdd.get(i), strategy.getLegs().get(i));
        }

        return true;
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

            if (Strategy.ExitStrategy.SHORT_STRIKE.equals(strategy.getExitStrategy())) {
                if (item.getCoeff() < 0 && item.getOption().getActiveUnderlyingPrice() <= item.getOption().getStrike()) {
                    System.out.println("Short strike reached! Exiting");
                    return true;
                }
            }

            int minDelta = item.getLeg().getMinDeltaToExit();
            int maxDelta = item.getLeg().getMaxDeltaToExit();
            int delta = (int) (item.getOption().getDelta() * position.CONTRACT_SIZE);

            if ((minDelta > 0 && delta < minDelta) || (maxDelta > 0 && delta > maxDelta)) {
                System.out.println("Delta limit reached! Exiting. Delta =" + delta);
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