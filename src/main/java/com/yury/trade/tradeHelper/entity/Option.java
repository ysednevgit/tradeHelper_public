package com.yury.trade.tradeHelper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "options")
public class Option extends AbstractOption {

    private Integer bidSizeEod;
    private Double bidEod;
    private Integer askSizeEod;
    private Double askEod;
    private Double underlyingBidEod;
    private Double underlyingAskEod;

    public boolean expiredOutOfTheMoney() {

        if (getTradingDaysLeft() > 0) {
            return false;
        }

        if (getOptionType().equals(OptionType.call) && getStrike() > getUnderlyingBidEod()) {
            return true;
        }

        if (getOptionType().equals(OptionType.put) && getStrike() < getUnderlyingAskEod()) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return getRoot() + " " + getStrike() + " " + getOptionType().name() + " " + getTradingDaysLeft() + " trade days left $" + df2.format(getMidPrice() * 100);
    }

}

