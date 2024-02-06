package com.yury.trade.tradeHelper.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yury.trade.tradeHelper.util.Utils;
import jakarta.persistence.*;
import lombok.Data;
import org.apache.commons.math3.util.Precision;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@MappedSuperclass
public abstract class AbstractOption implements Serializable {

    @Transient
    static DecimalFormat df2 = new DecimalFormat("###.##");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String underlyingSymbol; //QQQ

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    Date quoteDate;//"2023-01-20",

    String root; //QQQ

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    Date expiration; //"2023-10-20",

    Double strike; //160

    @Enumerated(EnumType.STRING)
    OptionType optionType; //C

    Double open; //199.4
    Double high; //199.4
    Double low; //194.3
    Double close; //194.48
    Integer tradeVolume; //54
    Integer bidSize; //25
    Double bid; //194.86
    Integer askSize; //15
    Double ask; //194.96
    Double underlyingBid; //354.94
    Double underlyingAsk; //354.95
    Double activeUnderlyingPrice;//354.945
    Double impliedVolatility;
    Double delta;
    Double gamma;
    Double theta;
    Double vega;
    Double rho;
    Integer openInterest;

    public void setOptionType(String optionType) {
        if (optionType.equalsIgnoreCase(OptionType.call.getDescription())) {
            this.optionType = OptionType.call;
        } else if (optionType.equalsIgnoreCase(OptionType.put.getDescription())) {
            this.optionType = OptionType.put;
        } else {
            throw new IllegalArgumentException("Invalid option type: " + optionType);
        }
    }

    public enum OptionType {
        call("C"),
        put("P");

        String description;

        OptionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public double getMidPrice() {
        return Precision.round(bid + (ask - bid) / 2, 2);
    }

    public int getTradingDaysLeft() {
        return calculateTradingDays(Utils.convertToLocalDate(getQuoteDate()), Utils.convertToLocalDate(getExpiration()), Utils.HOLIDAYS);
    }

    public boolean similarTo(AbstractOption option) {
        boolean isSymbolSimilar = this.underlyingSymbol.equals(option.getUnderlyingSymbol());
        boolean isExpDateSimilar = this.expiration.equals(option.getExpiration());
        boolean isStrikeSimilar = Math.abs(this.strike - option.getStrike()) < 0.25;
        boolean isOptionTypeSimilar = this.optionType.equals(option.getOptionType());

        return isSymbolSimilar && isExpDateSimilar && isStrikeSimilar && isOptionTypeSimilar;
    }

    int calculateTradingDays(LocalDate quoteDate, LocalDate expirationDate, List<LocalDate> holidays) {
        int tradingDays = 0;
        LocalDate currentDate = quoteDate.plusDays(1); // Start from the next day after the quote date

        while (!currentDate.isAfter(expirationDate)) {
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(currentDate)) {
                tradingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return tradingDays;
    }

}