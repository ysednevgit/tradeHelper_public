package com.yury.trade.tradeHelper.service;

import com.yury.trade.tradeHelper.entity.Option;
import com.yury.trade.tradeHelper.entity.OptionIntervals;
import com.yury.trade.tradeHelper.repository.OptionIntervalsRepository;
import com.yury.trade.tradeHelper.repository.OptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Slf4j
public class DataService {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private OptionIntervalsRepository optionIntervalsRepository;

    @Autowired
    private SftpFileService sftpFileService;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void updateData() throws IOException, ParseException {

        sftpFileService.downloadAllFiles();

        File directory = new File("C:\\Work\\CBOE\\files");
        File[] files = directory.listFiles();

        log.info("Found files to process: " + (files.length - 1));

        for (File file : files) {
            if (file.isFile()) {
                if (file.getName().toLowerCase().endsWith(".zip")) {
                    unzipAndParse(file);
                }
            }
        }
    }

    private void unzipAndParse(File zipFile) throws IOException, ParseException {
        ZipFile zip = new ZipFile(zipFile);
        File destDir = new File(zipFile.getParent(), "unzipped");

        if (!destDir.exists()) {
            destDir.mkdir();
        }

        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryFile = new File(destDir, entry.getName());

            if (!entry.isDirectory()) {
                InputStream in = zip.getInputStream(entry);
                Files.copy(in, entryFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                in.close();
                String fileName = entryFile.getName().toLowerCase();
                if (fileName.endsWith(".csv") && fileName.indexOf("OptionsEOD".toLowerCase()) >= 0) {
                    optionRepository.saveAll(parseOptionsCSV(entryFile.getPath()));
                } else if (fileName.endsWith(".csv") && fileName.indexOf("OptionsIntervals".toLowerCase()) >= 0) {
                    optionIntervalsRepository.saveAll(parseOptionsIntervalsCSV(entryFile.getPath()));
                }
            }
        }
        zip.close();
    }

    private List<Option> parseOptionsCSV(String filePath) throws IOException, ParseException {

        log.info("Loading " + filePath);

        boolean needToCheckExistingData = true;

        List<Option> options = new ArrayList<>();

        FileReader fileReader = new FileReader(filePath);
        CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader());

        for (CSVRecord csvRecord : csvParser) {
            Option option = new Option();

            option.setUnderlyingSymbol(csvRecord.get("underlying_symbol"));
            option.setQuoteDate(sdf.parse(csvRecord.get("quote_date")));

            if (needToCheckExistingData) {
                if (optionRepository.countByUnderlyingSymbolAndQuoteDate(option.getUnderlyingSymbol(), option.getQuoteDate()) > 0) {
                    log.info("Already processed. Exiting");
                    csvParser.close();
                    fileReader.close();
                    return options;
                }
                needToCheckExistingData = false;
            }

            option.setRoot(csvRecord.get("root"));
            option.setExpiration(sdf.parse(csvRecord.get("expiration")));
            option.setStrike(Double.parseDouble(csvRecord.get("strike")));
            option.setOptionType(csvRecord.get("option_type"));
            option.setOpen(Double.parseDouble(csvRecord.get("open")));
            option.setHigh(Double.parseDouble(csvRecord.get("high")));
            option.setLow(Double.parseDouble(csvRecord.get("low")));
            option.setClose(Double.parseDouble(csvRecord.get("close")));
            option.setTradeVolume(Integer.parseInt(csvRecord.get("trade_volume")));
            option.setBidSize(Integer.parseInt(csvRecord.get("bid_size_1545")));
            option.setBid(Double.parseDouble(csvRecord.get("bid_1545")));
            option.setAskSize(Integer.parseInt(csvRecord.get("ask_size_1545")));
            option.setAsk(Double.parseDouble(csvRecord.get("ask_1545")));
            option.setUnderlyingBid(Double.parseDouble(csvRecord.get("underlying_bid_1545")));
            option.setUnderlyingAsk(Double.parseDouble(csvRecord.get("underlying_ask_1545")));
            option.setActiveUnderlyingPrice(Double.parseDouble(csvRecord.get("active_underlying_price_1545")));
            option.setImpliedVolatility(Double.parseDouble(csvRecord.get("implied_volatility_1545")));
            option.setDelta(Double.parseDouble(csvRecord.get("delta_1545")));
            option.setGamma(Double.parseDouble(csvRecord.get("gamma_1545")));
            option.setTheta(Double.parseDouble(csvRecord.get("theta_1545")));
            option.setVega(Double.parseDouble(csvRecord.get("vega_1545")));
            option.setRho(Double.parseDouble(csvRecord.get("rho_1545")));
            option.setBidSizeEod(Integer.parseInt(csvRecord.get("bid_size_eod")));
            option.setBidEod(Double.parseDouble(csvRecord.get("bid_eod")));
            option.setAskSizeEod(Integer.parseInt(csvRecord.get("ask_size_eod")));
            option.setAskEod(Double.parseDouble(csvRecord.get("ask_eod")));
            option.setUnderlyingBidEod(Double.parseDouble(csvRecord.get("underlying_bid_eod")));
            option.setUnderlyingAskEod(Double.parseDouble(csvRecord.get("underlying_ask_eod")));
            option.setOpenInterest(Integer.parseInt(csvRecord.get("open_interest")));

            options.add(option);
        }

        log.info("Done.");
        return options;
    }

    private List<OptionIntervals> parseOptionsIntervalsCSV(String filePath) throws IOException, ParseException {

        log.info("Loading " + filePath);

        boolean needToCheckExistingData = true;

        List<OptionIntervals> optionsIntervals = new ArrayList<>();

        FileReader fileReader = new FileReader(filePath);
        CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader());

        for (CSVRecord csvRecord : csvParser) {
            OptionIntervals optionIntervals = new OptionIntervals();

            optionIntervals.setUnderlyingSymbol(csvRecord.get("underlying_symbol"));
            optionIntervals.setQuoteDate(sdfTime.parse(csvRecord.get("quote_datetime")));

            if (needToCheckExistingData) {
                if (optionIntervalsRepository.countByUnderlyingSymbolAndQuoteDate(optionIntervals.getUnderlyingSymbol(),
                        optionIntervals.getQuoteDate()) > 0) {
                    log.info("Already processed. Exiting");
                    csvParser.close();
                    fileReader.close();
                    return optionsIntervals;
                }
                needToCheckExistingData = false;
            }

            optionIntervals.setRoot(csvRecord.get("root"));
            optionIntervals.setExpiration(sdf.parse(csvRecord.get("expiration")));
            optionIntervals.setStrike(Double.parseDouble(csvRecord.get("strike")));
            optionIntervals.setOptionType(csvRecord.get("option_type"));
            optionIntervals.setOpen(Double.parseDouble(csvRecord.get("open")));
            optionIntervals.setHigh(Double.parseDouble(csvRecord.get("high")));
            optionIntervals.setLow(Double.parseDouble(csvRecord.get("low")));
            optionIntervals.setClose(Double.parseDouble(csvRecord.get("close")));
            optionIntervals.setTradeVolume(Integer.parseInt(csvRecord.get("trade_volume")));
            optionIntervals.setBidSize(Integer.parseInt(csvRecord.get("bid_size")));
            optionIntervals.setBid(Double.parseDouble(csvRecord.get("bid")));
            optionIntervals.setAskSize(Integer.parseInt(csvRecord.get("ask_size")));
            optionIntervals.setAsk(Double.parseDouble(csvRecord.get("ask")));
            optionIntervals.setUnderlyingBid(Double.parseDouble(csvRecord.get("underlying_bid")));
            optionIntervals.setUnderlyingAsk(Double.parseDouble(csvRecord.get("underlying_ask")));
            optionIntervals.setActiveUnderlyingPrice(Double.parseDouble(csvRecord.get("active_underlying_price")));
            optionIntervals.setImpliedVolatility(Double.parseDouble(csvRecord.get("implied_volatility")));
            optionIntervals.setDelta(Double.parseDouble(csvRecord.get("delta")));
            optionIntervals.setGamma(Double.parseDouble(csvRecord.get("gamma")));
            optionIntervals.setTheta(Double.parseDouble(csvRecord.get("theta")));
            optionIntervals.setVega(Double.parseDouble(csvRecord.get("vega")));
            optionIntervals.setRho(Double.parseDouble(csvRecord.get("rho")));
            optionIntervals.setOpenInterest(Integer.parseInt(csvRecord.get("open_interest")));

            optionsIntervals.add(optionIntervals);
        }
        log.info("Done.");
        return optionsIntervals;
    }

}