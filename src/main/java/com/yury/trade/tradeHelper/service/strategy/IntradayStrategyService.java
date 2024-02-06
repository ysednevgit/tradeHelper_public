package com.yury.trade.tradeHelper.service.strategy;

import com.yury.trade.tradeHelper.chart.LineChartDataset;
import com.yury.trade.tradeHelper.entity.AbstractOption;
import com.yury.trade.tradeHelper.entity.Option;
import com.yury.trade.tradeHelper.entity.OptionIntervals;
import com.yury.trade.tradeHelper.repository.OptionIntervalsRepository;
import com.yury.trade.tradeHelper.repository.OptionRepository;
import com.yury.trade.tradeHelper.service.AbstractOptionService;
import com.yury.trade.tradeHelper.service.ChartService;
import com.yury.trade.tradeHelper.util.OptionIntervalsPosition;
import com.yury.trade.tradeHelper.util.Strategy;
import com.yury.trade.tradeHelper.util.Trade;
import com.yury.trade.tradeHelper.util.TradeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class IntradayStrategyService {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private OptionIntervalsRepository optionIntervalsRepository;

    @Autowired
    private AbstractOptionService optionService;

    @Autowired
    private ChartService chartService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private Calendar calendar = Calendar.getInstance();

    private Map<Date, List<OptionIntervals>> toOptionsMap(List<OptionIntervals> options) throws ParseException {
        Map<Date, List<OptionIntervals>> optionsMap = new TreeMap<>();

        for (OptionIntervals option : options) {

            Date quoteDate = sdf.parse(sdf.format(option.getQuoteDate()));

            if (!optionsMap.containsKey(quoteDate)) {
                optionsMap.put(quoteDate, new ArrayList<>());
            }
            optionsMap.get(quoteDate).add(option);
        }
        return optionsMap;
    }

    public void runStrategy(Strategy strategy, String symbol, Map<Date, List<Option>> optionsMap, List<LineChartDataset> datasets) throws ParseException {

        int startIndex = 0;
        int endIndex = 6;

        Map<Date, List<OptionIntervals>> optionIntervalsMap = toOptionsMap(optionIntervalsRepository.findAll());

        Map<Date, Integer> balanceMap = new LinkedHashMap<>();

        List<Trade> trades = new ArrayList<>();

        LineChartDataset dataset = new LineChartDataset(strategy.getName());

        int balance = 0;

        for (Date date : optionIntervalsMap.keySet()) {

            System.out.println(date);
            List<OptionIntervals> dateIntradayOptions = optionIntervalsMap.get(date);

            Set<Date> quoteDates = new TreeSet<>();
            for (OptionIntervals optionIntervals : dateIntradayOptions) {
                if (!quoteDates.contains(optionIntervals.getQuoteDate())) {
                    quoteDates.add(optionIntervals.getQuoteDate());
                }
            }

            Date morningQuoteDate = quoteDates.stream().toList().get(startIndex);

            List<OptionIntervals> morningDateOptions = new ArrayList<>();
            for (OptionIntervals optionIntervals : dateIntradayOptions) {
                if (morningQuoteDate.equals(optionIntervals.getQuoteDate())) {
                    morningDateOptions.add(optionIntervals);
                }
            }

            OptionIntervalsPosition position = new OptionIntervalsPosition();

            for (Strategy.Leg leg : strategy.getLegs()) {

                OptionIntervals option = (OptionIntervals) optionService.getClosestByDelta(leg, morningDateOptions);

                List<OptionIntervals> optionIntervalsList = getOptionIntervalsSimilarToOption(option, dateIntradayOptions);

                Collections.sort(optionIntervalsList);
                position.add(optionIntervalsList, leg);
            }

            if (position.getItems().size() > 0) {
                System.out.println(position);
            }

            int profit = (int) (position.getPrice(endIndex) - position.getPrice(startIndex));

            balance += profit;

            balanceMap.put(date, balance);
            dataset.add(balanceMap.get(date));

            System.out.println("Balance = " + balanceMap.get(date) + " Profit = " + profit);
        }
        datasets.add(dataset);

        System.out.println();
        System.out.println("Strategy=" + strategy.getName());
        System.out.println(TradeInfo.getInfo(trades));
    }

    private List<OptionIntervals> getOptionIntervalsSimilarToOption(AbstractOption option, List<OptionIntervals> optionsIntervals) {
        List<OptionIntervals> result = new ArrayList<>();

        for (OptionIntervals optionIntervals : optionsIntervals) {
            if (optionIntervals.similarTo(option)) {
                result.add(optionIntervals);
            }
        }
        return result;
    }

}
