package com.yury.trade.tradeHelper.util;

import com.yury.trade.tradeHelper.entity.AbstractOption;
import com.yury.trade.tradeHelper.entity.Option;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Position {

    public final int CONTRACT_SIZE = 100;

    private static final DecimalFormat df2 = new DecimalFormat("###.##");

    private double positionDelta = 0;
    private double positionTheta = 0;
    private double positionGamma = 0;

    private double adjustments;

    private int shares = 0;
    private double sharePrice;
    private String stockSymbol;

    private double initialStockPrice;

    private List<Item> items = new ArrayList<>();

    public void add(AbstractOption option, Strategy.Leg leg) {

        if (initialStockPrice == 0) {
            initialStockPrice = option.getActiveUnderlyingPrice();
        }

        int coeff = leg.getCoeff();

        items.add(new Item(option, leg));
        adjustments -= coeff * option.getMidPrice() * CONTRACT_SIZE;
        //$1 per option extra fee
        adjustments -= Math.abs(coeff);
        calc();

        if (stockSymbol == null) {
            stockSymbol = option.getUnderlyingSymbol();
        }
        sharePrice = option.getActiveUnderlyingPrice();
        System.out.println("Added " + coeff + " " + option);
    }

    public void add(Item item, int coeffToAdd) {

        adjustments -= coeffToAdd * item.getOption().getMidPrice() * CONTRACT_SIZE;
        //$1 per option extra fee
        adjustments -= Math.abs(coeffToAdd);

        item.getLeg().setCoeff(item.getCoeff() + coeffToAdd);

        calc();

        if (coeffToAdd > 0) {
            System.out.println("Added " + coeffToAdd + " " + item.getOption());
        } else {
            System.out.println("Removed " + coeffToAdd + " " + item.getOption());
        }
    }

    public double getInitialStockPrice() {
        return initialStockPrice;
    }

    public double getPositionDelta() {
        return positionDelta;
    }

    public List<Item> getItems() {
        return items;
    }

    public double getBalance() {
        return getPrice() + adjustments;
    }

    public double getPrice() {
        double positionPrice = 0;

        for (Item item : items) {
            positionPrice += item.getCoeff() * item.getOption().getMidPrice();
        }
        return positionPrice * CONTRACT_SIZE;
    }

    public void update(List<Option> options) {
        for (Item item : items) {
            for (Option option : options) {
                if (item.getOption().similarTo(option)) {
                    item.setOption(option);
                    break;
                }
            }
        }
        calc();
        sharePrice = options.get(0).getActiveUnderlyingPrice();
    }

    public void close() {
        for (Item item : items) {
            System.out.println("Closed " + item);
            adjustments += item.getCoeff() * item.getOption().getMidPrice() * CONTRACT_SIZE;
            //$1 per option extra fee
            adjustments -= Math.abs(item.getCoeff());
        }
        items.clear();
        calc();
    }

    public void closeItem(Item item) {
        System.out.println("Closed " + item);
        adjustments += item.getCoeff() * item.getOption().getMidPrice() * CONTRACT_SIZE;
        //$1 per option extra fee
        adjustments -= Math.abs(item.getCoeff());
        removeItem(item);
    }

    public void removeItem(Item item) {
        getItems().remove(item);
        calc();
    }

    @Override
    public String toString() {

        return stockSymbol + "=" + sharePrice +
                " delta=" + df2.format(CONTRACT_SIZE * positionDelta) +
                ", theta=" + df2.format(CONTRACT_SIZE * positionTheta) +
                ", gamma=" + df2.format(CONTRACT_SIZE * positionGamma) +
                ", $" + df2.format(getPrice()) +
                ", " + items +
                "} ";
    }

    private void calc() {
        positionDelta = 0;
        positionTheta = 0;
        positionGamma = 0;

        for (Item item : items) {
            AbstractOption option = item.getOption();
            int coeff = item.getCoeff();

            positionTheta += option.getTheta() * coeff;
            positionGamma += option.getGamma() * coeff;
            positionDelta += option.getDelta() * coeff + shares / CONTRACT_SIZE;
        }
    }

    @Data
    public static class Item {
        private AbstractOption option;
        private Strategy.Leg leg;

        public int getCoeff() {
            return getLeg().getCoeff();
        }

        @Override
        public String toString() {
            return getCoeff() + " " + option;
        }

        public Item(AbstractOption option, Strategy.Leg leg) {
            this.option = option;
            this.leg = leg;
        }
    }

}
