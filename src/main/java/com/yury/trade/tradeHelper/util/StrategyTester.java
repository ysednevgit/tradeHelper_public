package com.yury.trade.tradeHelper.util;

import com.yury.trade.tradeHelper.entity.AbstractOption.OptionType;
import com.yury.trade.tradeHelper.entity.Option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrategyTester {

    public List<Strategy> getStrategiesToTest() {

        List<Strategy> strategies = new ArrayList<>();

        strategies.add(Strategy.builder()
                .name("1/1/-3 40 day").
                leg(Strategy.Leg.builder().coeff(1).delta(50).expDays(40).build()).
                leg(Strategy.Leg.builder().coeff(1).delta(50).expDays(5).optionType(OptionType.put).build()).
                leg(Strategy.Leg.builder().coeff(-3).delta(15).expDays(5).optionType(OptionType.put).build()).
                exitStrategy(Strategy.ExitStrategy.SHORT_STRIKE).
                buyDays(Arrays.asList(1)).
                sellDays(Arrays.asList(5)).
                build());

        strategies.add(Strategy.builder()
                .name("1/2/-5 40 day").
                leg(Strategy.Leg.builder().coeff(1).delta(50).expDays(40).build()).
                leg(Strategy.Leg.builder().coeff(2).delta(30).expDays(5).optionType(OptionType.put).build()).
                leg(Strategy.Leg.builder().coeff(-5).delta(15).expDays(5).optionType(OptionType.put).build()).
                exitStrategy(Strategy.ExitStrategy.SHORT_STRIKE).
                buyDays(Arrays.asList(1)).
                sellDays(Arrays.asList(5)).
                build());

        return strategies;
    }

    public List<Strategy> getAllStrategiesToTest() {

        List<Strategy> strategies = new ArrayList<>();

        strategies.add(Strategy.builder()
                .name("1 call").
                leg(Strategy.Leg.builder().coeff(1).delta(70).expDays(500).minTradingDaysLeftToExit(100).build()).
                build());

        strategies.add(Strategy.builder()
                .name("3/2").
                leg(Strategy.Leg.builder().coeff(3).delta(60).expDays(300).minTradingDaysLeftToExit(100).build()).
                leg(Strategy.Leg.builder().coeff(-2).delta(70).expDays(5).rollingStrategy(Strategy.RollingStrategy.ROLL_SAME_DELTA).build()).
                build());

        strategies.add(Strategy.builder()
                .name("3/2 30").
                leg(Strategy.Leg.builder().coeff(3).delta(60).expDays(300).minTradingDaysLeftToExit(100).build()).
                leg(Strategy.Leg.builder().coeff(-2).delta(70).expDays(30).rollingStrategy(Strategy.RollingStrategy.ROLL_SAME_DELTA).build()).
                build());

        strategies.add(Strategy.builder()
                .name("3/1").
                leg(Strategy.Leg.builder().coeff(4).delta(40).expDays(300).minTradingDaysLeftToExit(100).build()).
                leg(Strategy.Leg.builder().coeff(-1).delta(80).expDays(5).rollingStrategy(Strategy.RollingStrategy.ROLL_SAME_DELTA).build()).
                build());

        strategies.add(Strategy.builder()
                .name("4/1").
                leg(Strategy.Leg.builder().coeff(4).delta(30).expDays(300).minTradingDaysLeftToExit(100).build()).
                leg(Strategy.Leg.builder().coeff(-1).delta(80).expDays(5).rollingStrategy(Strategy.RollingStrategy.ROLL_SAME_DELTA).build()).
                build());

        strategies.add(Strategy.builder()
                .name("-6 15 delta puts").
                leg(Strategy.Leg.builder().coeff(-6).delta(15).optionType(Option.OptionType.put).expDays(3).minTradingDaysLeftToExit(1).build()).
                build());

        strategies.add(Strategy.builder()
                .name("3/3/-15").
                leg(Strategy.Leg.builder().coeff(3).delta(50).expDays(2).build()).
                leg(Strategy.Leg.builder().coeff(3).delta(50).optionType(Option.OptionType.put).expDays(2).build()).
                leg(Strategy.Leg.builder().coeff(-15).delta(15).optionType(Option.OptionType.put).expDays(3).build()).
                build());

        //ratio puts sell
        strategies.add(Strategy.builder()
                .name("1/-5").
                leg(Strategy.Leg.builder().coeff(1).delta(40).expDays(2).build()).
                leg(Strategy.Leg.builder().coeff(-5).delta(15).optionType(Option.OptionType.put).expDays(3).build()).
                build());

        strategies.add(Strategy.builder()
                .name("1/-3").
                leg(Strategy.Leg.builder().coeff(1).delta(30).expDays(2).build()).
                leg(Strategy.Leg.builder().coeff(-3).delta(15).optionType(Option.OptionType.put).expDays(3).build()).
                build());

        strategies.add(Strategy.builder()
                .name("1/-2").
                leg(Strategy.Leg.builder().coeff(1).delta(20).expDays(2).build()).
                leg(Strategy.Leg.builder().coeff(-2).delta(15).optionType(Option.OptionType.put).expDays(3).build()).
                build());

        return strategies;
    }

}
