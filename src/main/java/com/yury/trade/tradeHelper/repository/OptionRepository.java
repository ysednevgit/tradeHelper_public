package com.yury.trade.tradeHelper.repository;

import com.yury.trade.tradeHelper.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Integer> {

    @Query("SELECT COUNT(o) FROM Option o WHERE o.underlyingSymbol = :underlyingSymbol AND o.quoteDate = :quoteDate")
    long countByUnderlyingSymbolAndQuoteDate(String underlyingSymbol, Date quoteDate);

    @Query("SELECT o FROM Option o WHERE o.underlyingSymbol = :underlyingSymbol AND o.quoteDate >= :startDate AND o.quoteDate <= :endDate")
    List<Option> findByUnderlyingSymbolAndDates(String underlyingSymbol,
                                                Date startDate,
                                                Date endDate);

    @Query("SELECT o FROM Option o WHERE o.underlyingSymbol = :underlyingSymbol" +
            " AND o.optionType = :optionType" +
            " AND o.quoteDate >= :startDate AND o.quoteDate <= :endDate")
    List<Option> find(String underlyingSymbol,
                      Option.OptionType optionType,
                      Date startDate,
                      Date endDate);

    @Query("SELECT o FROM Option o WHERE o.underlyingSymbol = :underlyingSymbol AND o.quoteDate = :quoteDate")
    List<Option> findByUnderlyingSymbolAndDate(String underlyingSymbol, Date quoteDate);

    @Query("SELECT o FROM Option o WHERE o.underlyingSymbol = :underlyingSymbol " +
            "AND o.quoteDate >= :startDate " +
            "AND o.quoteDate <= :endDate " +
            "AND o.expiration = :expiration " +
            "AND o.strike = :strike AND o.optionType = :optionType")
    List<Option> find(String underlyingSymbol,
                      Option.OptionType optionType,
                      Double strike,
                      Date expiration,
                      Date startDate,
                      Date endDate);

    @Query("SELECT o.quoteDate FROM Option o WHERE o.underlyingSymbol = :underlyingSymbol " +
            "AND o.quoteDate >= :startDate " +
            "AND o.quoteDate <= :endDate")
    List<Date> findQuoteDates(String underlyingSymbol,
                              Date startDate,
                              Date endDate);

}