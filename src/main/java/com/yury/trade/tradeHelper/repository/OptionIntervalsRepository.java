package com.yury.trade.tradeHelper.repository;

import com.yury.trade.tradeHelper.entity.Option;
import com.yury.trade.tradeHelper.entity.OptionIntervals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface OptionIntervalsRepository extends JpaRepository<OptionIntervals, Integer> {

    @Query("SELECT COUNT(o) FROM OptionIntervals o WHERE o.underlyingSymbol = :underlyingSymbol AND o.quoteDate = :quoteDate")
    long countByUnderlyingSymbolAndQuoteDate(String underlyingSymbol, Date quoteDate);

    @Query("SELECT o FROM OptionIntervals o WHERE " +
            "(:underlyingSymbol IS NULL OR o.underlyingSymbol = COALESCE(:underlyingSymbol, o.underlyingSymbol)) " +
            "AND (:startDate IS NULL OR o.quoteDate >= COALESCE(:startDate, o.quoteDate)) " +
            "AND (:endDate IS NULL OR o.quoteDate <= COALESCE(:endDate, o.quoteDate))")
    List<OptionIntervals> findByUnderlyingSymbolAndDates(@Param("underlyingSymbol") String underlyingSymbol,
                                                @Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate);

    @Query("SELECT o FROM OptionIntervals o " +
            "WHERE o.underlyingSymbol = :underlyingSymbol " +
            "AND o.strike = :strike " +
            "AND o.optionType = :optionType " +
            "AND o.quoteDate BETWEEN :startDate AND :endDate " +
            "AND o.expiration BETWEEN :startDate AND :endDate")
    List<OptionIntervals> find(@Param("underlyingSymbol") String underlyingSymbol,
                               @Param("strike") double strike,
                               @Param("optionType") Option.OptionType optionType,
                               @Param("startDate") Date startDate,
                               @Param("endDate") Date endDate);
}