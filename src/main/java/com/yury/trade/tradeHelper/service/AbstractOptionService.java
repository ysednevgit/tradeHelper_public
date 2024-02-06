package com.yury.trade.tradeHelper.service;

import com.yury.trade.tradeHelper.entity.AbstractOption;
import com.yury.trade.tradeHelper.util.Strategy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AbstractOptionService {

    public AbstractOption getClosestByDelta(final Strategy.Leg leg, final List<? extends AbstractOption> options) {

        double delta = leg.getDelta() / 100;

        AbstractOption result = null;

        for (AbstractOption option : options) {

            if (!leg.getOptionType().equals(option.getOptionType())) {
                continue;
            }

            if (option.getTradingDaysLeft() == 0 && leg.getExpDays() > 0) {
                continue;
            }

            if (result == null) {

                result = option;
                continue;
            }

            double resultDaysDistance = Math.abs(result.getTradingDaysLeft() - leg.getExpDays());

            double daysDistance = Math.abs(option.getTradingDaysLeft() - leg.getExpDays());

            if (daysDistance < resultDaysDistance) {
                result = option;
            }

            double resultDeltaDistance = Math.abs(Math.abs(result.getDelta()) - Math.abs(delta));
            double deltaDistance = Math.abs(Math.abs(option.getDelta()) - Math.abs(delta));

            if ((daysDistance == resultDaysDistance) && (deltaDistance < resultDeltaDistance) && (option.getTradingDaysLeft() == result.getTradingDaysLeft())) {
                result = option;
            }
        }

        return result;
    }


    public AbstractOption getClosestByStrike(final Strategy.Leg leg, final double strike, final List<? extends AbstractOption> options) {

        AbstractOption result = null;

        for (AbstractOption option : options) {

            if (!leg.getOptionType().equals(option.getOptionType())) {
                continue;
            }

            if (result == null) {

                result = option;
                continue;
            }

            double resultDaysDistance = Math.abs(result.getTradingDaysLeft() - leg.getExpDays());

            double resultStrikeDistance = Math.abs(result.getStrike() - strike);
            double strikeDistance = Math.abs(option.getStrike() - strike);

            double daysDistance = Math.abs(option.getTradingDaysLeft() - leg.getExpDays());

            if (daysDistance < resultDaysDistance) {
                result = option;
            }

            if ((daysDistance == resultDaysDistance) &&
                    (strikeDistance <= resultStrikeDistance) &&
                    (option.getTradingDaysLeft() == result.getTradingDaysLeft())) {
                result = option;
            }
        }

        return result;
    }
}
