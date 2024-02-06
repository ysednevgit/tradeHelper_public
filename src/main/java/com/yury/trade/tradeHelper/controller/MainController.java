package com.yury.trade.tradeHelper.controller;

import com.yury.trade.tradeHelper.request.RunStrategyRequest;
import com.yury.trade.tradeHelper.service.DataService;
import com.yury.trade.tradeHelper.service.StockHistoryService;
import com.yury.trade.tradeHelper.service.strategy.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/trade")
public class MainController {

    @Autowired
    private DataService dataService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private StockHistoryService stockHistoryService;

    @PostMapping("/data/update")
    public ResponseEntity<String> updateData() throws Exception {
        dataService.updateData();
        return ResponseEntity.ok("Data Loaded Successfully ");
    }

    @PostMapping("/data/add_stock_history")
    public ResponseEntity<String> addStockHistory(@RequestParam(value = "start") String start,
                                                  @RequestParam(value = "end") String end) throws IOException {

        String message = stockHistoryService.addStockHistory(start, end);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/strategy/run")
    public ResponseEntity<String> runStrategies(@RequestBody RunStrategyRequest request) throws Exception {
        strategyService.runStrategies(
                request.getSymbol(),
                request.getStartDate(),
                request.getEndDate(),
                request.isDrawChart(),
                request.isDebug());

        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

}
