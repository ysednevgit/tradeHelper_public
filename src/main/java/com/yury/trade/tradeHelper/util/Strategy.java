package com.yury.trade.tradeHelper.util;

import com.yury.trade.tradeHelper.entity.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Strategy {

    @Builder.Default
    private String name = "";
    @Builder.Default
    private List<Integer> buyDays = new ArrayList<>();
    @Builder.Default
    private List<Integer> sellDays = new ArrayList<>();
    @Singular
    private List<Leg> legs;
    @Builder.Default
    private int shares = 0;

    private StrategyType strategyType;
    private ExitStrategy exitStrategy;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Leg {
        int coeff;
        @Builder.Default
        Option.OptionType optionType = Option.OptionType.call;
        double delta;
        int expDays;
        RollingStrategy rollingStrategy;
        @Builder.Default
        int minTradingDaysLeftToExit = 0;
        @Builder.Default
        int minDeltaToExit = 0;
        @Builder.Default
        int maxDeltaToExit = 0;
        @Builder.Default
        boolean flexibleExpDays = true;
        @Builder.Default
        boolean doNotRollIfExpiredOOTM = false;

        public Leg() {
        }

        public Leg(int coeff, Option.OptionType optionType, double delta, int expDays) {
            this.coeff = coeff;
            this.optionType = optionType;
            this.delta = delta;
            this.expDays = expDays;
        }

    }

    public enum RollingStrategy {
        ROLL_SAME_STRIKE,
        ROLL_SAME_DELTA
    }

    public enum StrategyType {
        RATIO_DIAGONAL,
        CALENDAR,
        STOCK_HISTORY,
        LEAPS,
        INTRADAY,
        PUTS_INCREASE
    }

    public enum ExitStrategy {
        SHORT_STRIKE,
    }
}