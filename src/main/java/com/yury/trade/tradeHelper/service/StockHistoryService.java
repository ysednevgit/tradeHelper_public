package com.yury.trade.tradeHelper.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yury.trade.tradeHelper.entity.StockHistory;
import com.yury.trade.tradeHelper.entity.StockHistoryId;
import com.yury.trade.tradeHelper.repository.StockHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockHistoryService {

    @Autowired
    private TradierService tradierService;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    ObjectMapper objectMapper;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return objectMapper;
    }

    public String addStockHistory(String start, String end) throws IOException {

        List<String> stockSymbols = new ArrayList<>();
        stockSymbols.add("QQQ");

        for (String stockSymbol : stockSymbols) {
            List<StockHistory> stockHistories = getStockHistories(stockSymbol, start, end);

            stockHistoryRepository.saveAll(stockHistories);

            String info = "Saved stock history for " + stockSymbol + " from " + start + " to " + end + " " + stockHistories.size() + " records";

            System.out.println(info);
        }

        System.out.println("Done.");

        return "Success";
    }

    private List<StockHistory> getStockHistories(final String stockSymbol, final String start, final String end) throws IOException {
        List<StockHistory> stockHistories = new ArrayList<>();

        JsonNode jsonNode = tradierService.history(stockSymbol, start, end);

        if (jsonNode.get("history") == null || jsonNode.get("history").get("day") == null || !(jsonNode.get("history").get("day") instanceof ArrayNode)) {
            return stockHistories;
        }
        ArrayNode arrayNode = (ArrayNode) jsonNode.get("history").get("day");

        for (int i = 0; i < arrayNode.size(); i++) {
            try {
                stockHistories.add(getStockHistory(arrayNode.get(i), stockSymbol));
            } catch (ParseException e) {
                System.out.println("Unable to parse " + arrayNode.get(i));
            }
        }
        return stockHistories;
    }

    private StockHistory getStockHistory(JsonNode node, String stockSymbol) throws ParseException {
        StockHistory stockHistory = getObjectMapper().convertValue(node, StockHistory.class);
        StockHistoryId id = new StockHistoryId();

        id.setSymbol(stockSymbol);
        id.setDate(simpleDateFormat.parse(node.get("date").textValue()));
        stockHistory.setStockHistoryId(id);
        return stockHistory;
    }
}
