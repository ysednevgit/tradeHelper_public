package com.yury.trade.tradeHelper.util;

import com.yury.trade.tradeHelper.entity.OptionIntervals;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OptionIntervalsPosition {

    private final int contractSize = 100;

    private List<Item> items = new ArrayList<>();

    public void add(List<OptionIntervals> options, Strategy.Leg leg) {

        items.add(new Item(options, leg));
    }

    public List<Item> getItems() {
        return items;
    }

    public double getPrice(int index) {
        double positionPrice = 0;

        for (Item item : items) {
            positionPrice += item.getCoeff() * item.getOptions().get(index).getMidPrice();
        }
        return positionPrice * contractSize;
    }

    @Data
    @AllArgsConstructor
    public static class Item {
        private List<OptionIntervals> options;
        private Strategy.Leg leg;

        public int getCoeff() {
            return getLeg().getCoeff();
        }

        @Override
        public String toString() {
            return getCoeff() + " " + options;
        }
    }

    @Override
    public String toString() {

        return "" + items;
    }

}

