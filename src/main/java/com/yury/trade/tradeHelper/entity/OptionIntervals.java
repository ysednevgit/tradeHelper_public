package com.yury.trade.tradeHelper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "options_intervals")
public class OptionIntervals extends AbstractOption implements Comparable<OptionIntervals> {

    @Override
    public int compareTo(OptionIntervals other) {
        return this.quoteDate.compareTo(other.quoteDate);
    }

    @Override
    public String toString() {
        return getRoot() + " " + getStrike() + " " + getOptionType().name() + " " + getQuoteDate() + " " + df2.format(getMidPrice() * 100);
    }


}
